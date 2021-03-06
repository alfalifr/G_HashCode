package _2021.optimize

import _2021.Config
import _2021.Util
import _2021.model.Delivery
import _2021.model.Menu
import _2021.model.PizzaMove
import sidev.lib.collection.notNullIterator
import sidev.lib.math.random.DistributedRandom
import sidev.lib.math.random.distRandomOf
import sidev.lib.math.random.randomBoolean
import java.math.BigInteger

/**
 * Kelas heuristik level tinggi yang berfungsi untuk melakukan iterasi
 * meng-invoke [LowLevel] untuk melakukan aksinya dan meng-invoke [Evaluation]
 * untuk menentukan apakah solusi yang dihasilkan [LowLevel] bisa diterima atau tidak.
 */
sealed class HighLevel(val code: String, val maxN: Int, val evaluation: Evaluation) {
    val lowLevelDist: DistributedRandom<LowLevel> = distRandomOf()
    abstract val optimizationTag: Optimize
    val isEvaluationTabu= evaluation is Evaluation.TABU

    abstract fun optimize(
        init: Delivery,
        menu: Menu,
        iterations: Int = Config.DEFAULT_ITERATIONS
    ): Pair<Delivery, BigInteger>?
    operator fun invoke(
        init: Delivery,
        menu: Menu,
        iterations: Int = Config.DEFAULT_ITERATIONS
    ): Pair<Delivery, BigInteger>? =
        optimize(init, menu, iterations)

    //@Suppress(SuppressLiteral.UNCHECKED_CAST)
    fun Array<PizzaMove>.newMoveIterator(): Iterator<PizzaMove> =
        if(isEvaluationTabu) notNullIterator() else iterator()

    protected fun nextLowLevel(coefficient: Double): LowLevel {
        //randomBoolean(1 / (1 + exp(1.0 / lowLevelDist.distSum)))
        return if(!lowLevelDist.isEmpty()
            && randomBoolean(1 / (1 + coefficient / lowLevelDist.distSum)) // Ada kemungkinan dapet lowLevel baru meskipun udah di assign yg lama.
        ) lowLevelDist.next()
        else initLowLevel()
    }
    protected fun initLowLevel(): LowLevel = LowLevel.getRandom(maxN)
    protected fun accept(lowLevel: LowLevel){
        lowLevelDist.add(lowLevel)
    }
    protected fun remove(lowLevel: LowLevel){
        lowLevelDist.add(lowLevel, -1)
    }
    override fun toString(): String = this::class.simpleName!!
    override fun equals(other: Any?): Boolean = other is HighLevel && toString() == other.toString()
    override fun hashCode(): Int = toString().hashCode()

    class ROULLETE_WHEEL(maxN: Int = 3, evaluation: Evaluation = Evaluation.BETTER)
        : HighLevel("rw", maxN, evaluation) {
        override val optimizationTag: Optimize = when(evaluation){
            Evaluation.BETTER -> Optimize.RW_HC
            is Evaluation.SIMULATED_ANNEALING -> Optimize.RW_SA
            is Evaluation.GREAT_DELUGE -> Optimize.RW_GD
            is Evaluation.TABU -> Optimize.RW_TA
        }

        override fun optimize(init: Delivery, menu: Menu, iterations: Int): Pair<Delivery, BigInteger>?{
            //val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
            var acceptRes= false
            val opt= init.clone_()
            val coefficient= iterations * 10 / 100.0  // probabilitas diambil-ulangnya lowLevel yang sama mencapai 50% saat iterasi mencapai 10% dari panjang total.
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                val lowLevel= nextLowLevel(coefficient)
                val moves= lowLevel(i, opt, menu)
                if(moves != null && evaluation(opt, moves)){
                    for(move in moves.newMoveIterator()){
                        //resDistMat.setPositionMatrix(move)
                        opt.moveById(move.id, move.from, move.to,)
                    }
                    lowLevelDist.add(lowLevel, 5)
                    acceptRes= true
                } else {
                    lowLevelDist.add(lowLevel, -1)
                }
            }
            return if(acceptRes) {
                val finalPenalty= Util.calculateScore(opt)
                opt.apply {
                    tag.optimization= optimizationTag
                } to finalPenalty
            } else null
        }
    }

    class TABU(
        maxN: Int = 3,
        tabuLowLevelSize: Int = 5,
        evaluation: Evaluation = Evaluation.BETTER
    ): HighLevel("tabu", maxN, evaluation){
        private val tabuLowLevels = arrayOfNulls<LowLevel>(tabuLowLevelSize)
        private var tabuLowLevelPointer= 0
        override val optimizationTag: Optimize = when(evaluation){
            Evaluation.BETTER -> Optimize.TA_HC
            is Evaluation.SIMULATED_ANNEALING -> Optimize.TA_SA
            is Evaluation.GREAT_DELUGE -> Optimize.TA_GD
            is Evaluation.TABU -> Optimize.TA_TA
        }
        fun insertAsTabu(lowLevel: LowLevel){
            tabuLowLevels[tabuLowLevelPointer]= lowLevel
            tabuLowLevelPointer= (tabuLowLevelPointer + 1) % tabuLowLevels.size
        }

        override fun optimize(init: Delivery, menu: Menu, iterations: Int): Pair<Delivery, BigInteger>? {
            //val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            //var resPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
            var acceptRes= false
            val opt= init.clone_()
            val coefficient= iterations * 40 / 100.0  // probabilitas diambil-ulangnya lowLevel yang sama mencapai 50% saat iterasi mencapai 40% dari panjang total.
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                var lowLevel: LowLevel
                do {
                    lowLevel = nextLowLevel(coefficient)
                } while(lowLevel in tabuLowLevels)
                val moves= lowLevel(i, opt, menu)
                if(moves != null && evaluation(opt, moves)){
                    for(move in moves.newMoveIterator()){
                        //resDistMat.setPositionMatrix(move)
                        opt.moveById(move.id, move.from, move.to)
                    }
                    accept(lowLevel)
                    acceptRes= true
                } else {
                    insertAsTabu(lowLevel)
                }
            }
            return if(acceptRes) {
                val finalPenalty= Util.calculateScore(opt)
                opt.apply {
                    tag.optimization= optimizationTag
                } to finalPenalty
            } else null
        }
    }

    class LINEAR(
        val lowLevel: LowLevel,
        //maxN: Int = 3, Gak penting karena udah ada [lowLevel].
        evaluation: Evaluation = Evaluation.BETTER
    ): HighLevel("lin", -1, evaluation){
        override val optimizationTag: Optimize = when(evaluation){
            Evaluation.BETTER -> when(lowLevel){
                LowLevel.MOVE -> Optimize.HC_MOVE
                LowLevel.SWAP -> Optimize.HC_SWAP
                is LowLevel.MOVE_N -> Optimize.HC_MOVEn
                is LowLevel.SWAP_N -> Optimize.HC_SWAPn
            }
            is Evaluation.SIMULATED_ANNEALING -> when(lowLevel){
                LowLevel.MOVE -> Optimize.SA_MOVE
                LowLevel.SWAP -> Optimize.SA_SWAP
                is LowLevel.MOVE_N -> Optimize.SA_MOVEn
                is LowLevel.SWAP_N -> Optimize.SA_SWAPn
            }
            is Evaluation.GREAT_DELUGE -> when(lowLevel){
                LowLevel.MOVE -> Optimize.GD_MOVE
                LowLevel.SWAP -> Optimize.GD_SWAP
                is LowLevel.MOVE_N -> Optimize.GD_MOVEn
                is LowLevel.SWAP_N -> Optimize.GD_SWAPn
            }
            is Evaluation.TABU -> when(lowLevel){
                LowLevel.MOVE -> Optimize.TA_MOVE
                LowLevel.SWAP -> Optimize.TA_SWAP
                is LowLevel.MOVE_N -> Optimize.TA_MOVEn
                is LowLevel.SWAP_N -> Optimize.TA_SWAPn
            }
        }

        override fun optimize(init: Delivery, menu: Menu, iterations: Int): Pair<Delivery, BigInteger>? {
            //val resDistMat= Util.getFullDistanceMatrix(init, courseAdjacencyMatrix)
            var acceptRes= false
            val opt= init.clone_()
            val trimAfter= lowLevel != LowLevel.SWAP
                    && lowLevel !is LowLevel.SWAP_N
                    && lowLevel !is LowLevel.MOVE_N
            for(i in 0 until iterations) {
                //val sch= resSch?.clone_() ?: init.clone_()
                val moves= lowLevel(i, opt, menu)
                //val penalty= Util.getPenalty(sch, courseAdjacencyMatrix, studentCount)
                if(moves != null && evaluation(opt, moves)){
                    for(move in moves.newMoveIterator()){
                        //resDistMat.setPositionMatrix(move)
                        opt.moveById(move.id, move.from, move.to)
                    }
                    acceptRes= true
                } //else { resPenalty.value= resPenalty.value }
            }
            return if(acceptRes) {
                val finalPenalty= Util.calculateScore(opt)
                opt.apply {
                    tag.optimization= optimizationTag
                } to finalPenalty
            } else null
        }
    }
}