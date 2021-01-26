package _2021.optimize

import _2021.model.Delivery
import _2021.model.Menu
import _2021.model.PizzaMove
import sidev.lib.exception.IllegalArgExc
import sidev.lib.progression.domain
import sidev.lib.progression.range

/**
 * Kelas heuristik level rendah yang digunakan untuk merubah susunan
 * course pada tiap timeslot pada tiap iterasi.
 */
sealed class LowLevel(val code: String) {
    companion object {
        val all: List<LowLevel> by lazy { listOf(MOVE, SWAP, MOVE_N.Default, SWAP_N.Default) }
        fun getRandom(maxN: Int = 3): LowLevel {
            if(maxN < 1)
                throw IllegalArgExc(
                    paramExcepted = arrayOf("maxN"),
                    detailMsg = "Param `maxN` ($maxN) < 1"
                )
            val rand= all.random()
            return when(rand){
                is MOVE_N -> if(maxN >= 2) MOVE_N((2..maxN).random()) else MOVE
                is SWAP_N -> if(maxN >= 2) SWAP_N((2..maxN).random()) else SWAP
                else -> rand
            }
        }
    }

    abstract fun calculate(i: Int, currentDelivery: Delivery, menu: Menu): Array<PizzaMove>?
    operator fun invoke(i: Int, currentDelivery: Delivery, menu: Menu): Array<PizzaMove>? =
        calculate(i, currentDelivery, menu)

    override fun toString(): String = this::class.simpleName!!
    override fun equals(other: Any?): Boolean = other is LowLevel && toString() == other.toString()
    override fun hashCode(): Int = toString().hashCode()

    object MOVE: LowLevel("m"){
        override fun calculate(
            i: Int,
            currentDelivery: Delivery,
            menu: Menu
        ): Array<PizzaMove>? {
            val range= 0 until currentDelivery.order.size

            var res: Array<PizzaMove>?= null
            var loop= true
            var u= -1
            while(loop){
                if(++u >= 7) break
                try {
                    val fromTeam= range.random()
                    //val fromTimeslot= currentDelivery.getTimeslotAssert(fromTeam)
                    var toTeam= fromTeam //range.random()

                    if(range.range > 0)
                        while(toTeam == fromTeam)
                            toTeam= range.random()

                    val movedPizzaId= currentDelivery.getSingleDeliveryByTeam(fromTeam)!!.pizzas.random().id

                    res= if(currentDelivery.checkConflictInTeam(
                            toTeam, menu, movedPizzaId
                        )) {
                        //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                        arrayOf(PizzaMove(movedPizzaId, fromTeam, toTeam))
                    } else null
                    loop= false
                } catch(e: NoSuchElementException){
                    // Abaikan
                }
            }
            return res
        }
    }
    open class MOVE_N private constructor(val n: Int): LowLevel("mN_$n"){
        internal object Default: MOVE_N(-1)
        companion object {
            operator fun invoke(n: Int): MOVE_N = MOVE_N(n)
        }
        override fun toString(): String = "MOVE_$n"
        override fun calculate(
            i: Int,
            currentDelivery: Delivery,
            menu: Menu
        ): Array<PizzaMove>? {
            val range= 0 until currentDelivery.order.size
            val rangeSize= range.domain

            val fromTeamList= mutableListOf<Int>()
            val moveList= mutableListOf<PizzaMove>()
            //val toTimeslotNoList= mutableListOf<Int>()

            for_@ for(u in 0 until n){
                var loop= true
                var o= -1
                while_@ while(loop){
                    if(++o >= 7) break
                    try {
                        val rangeSizeItr= rangeSize - u
                        var fromTeam: Int
                        do {
                            fromTeam= range.random()
                        } while(rangeSizeItr > 0 && fromTeam in fromTeamList)

                        var toTeam= fromTeam
                        while(rangeSize > 0 && toTeam == fromTeam){
                            toTeam= range.random()
                        }

                        //val fromTimeslot= currentSchedule.getTimeslotAssert(fromTeam)
                        val fromPizzaList= currentDelivery.getSingleDeliveryByTeam(fromTeam)!!.pizzas
                        var movedPizzaId= fromPizzaList.random().id
                        val fromPizzaListRemainSize= fromPizzaList.size - u
                        while(moveList.any { it.id == movedPizzaId }){
                            if(fromPizzaListRemainSize <= 0)
                                continue@while_
                            movedPizzaId= fromPizzaList.random().id
                        }

                        if(currentDelivery.checkConflictInTeam(
                                toTeam, menu, movedPizzaId
                            )) {
                            //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                            moveList += PizzaMove(movedPizzaId, fromTeam, toTeam)
                        } else {
                            continue@while_
                        }
                        fromTeamList += fromTeam
                        loop= false
                    } catch(e: NoSuchElementException){
                        // Abaikan
                    }
                }
            }
            return if(moveList.size == n) moveList.toTypedArray() else null
        }
    }

    object SWAP: LowLevel("s"){
        override fun calculate(
            i: Int,
            currentDelivery: Delivery,
            menu: Menu
        ): Array<PizzaMove>? {
            val range= 0 until currentDelivery.order.size
            val fromTeam= range.random()
            //val fromTimeslot= currentSchedule.getTimeslotAssert(fromTeam)
            val toTeam= range.random()
            //val toTimeslot= currentSchedule.getTimeslotAssert(toTeam)
            val movedSrcPizzaId= currentDelivery.getSingleDeliveryByTeam(fromTeam)!!.pizzas.random().id //currentSchedule[fromTimeslot]!!.random().id
            val movedDestPizzaId= currentDelivery.getSingleDeliveryByTeam(toTeam)!!.pizzas.random().id

            val srcTimeslotValid = currentDelivery.checkConflictInTeam(
                fromTeam, menu, movedDestPizzaId
            ){
                it != movedSrcPizzaId
            }
            val destTimeslotValid = currentDelivery.checkConflictInTeam(
                toTeam, menu, movedSrcPizzaId
            ){
                it != movedDestPizzaId
            }

            //prine("Optimize.swap() fromTimeslotNo=$fromTimeslotNo toTimeslotNo=$toTimeslotNo movedSrcCourseId=$movedSrcCourseId movedDestCourseId=$movedDestCourseId srcTimeslotValid=$srcTimeslotValid destTimeslotValid=$destTimeslotValid")

            return if(srcTimeslotValid && destTimeslotValid) {
                arrayOf(
                    PizzaMove(movedSrcPizzaId, fromTeam, toTeam),
                    PizzaMove(movedDestPizzaId, toTeam, fromTeam),
                )
            } else null
        }
    }
    open class SWAP_N private constructor(val n: Int): LowLevel("sN_$n"){
        internal object Default: SWAP_N(-1)
        companion object {
            operator fun invoke(n: Int): SWAP_N = SWAP_N(n)
        }
        override fun toString(): String = "SWAP_$n"
        override fun calculate(
            i: Int,
            currentDelivery: Delivery,
            menu: Menu
        ): Array<PizzaMove>? {
            val range= 0 until currentDelivery.order.size
            val rangeSize= range.domain

            val fromTeamList= mutableListOf<Int>()
            val moveList= mutableListOf<PizzaMove>()

            //prine("Optimize.swap() fromTimeslotNo=$fromTimeslotNo toTimeslotNo=$toTimeslotNo movedSrcCourseId=$movedSrcCourseId movedDestCourseId=$movedDestCourseId srcTimeslotValid=$srcTimeslotValid destTimeslotValid=$destTimeslotValid")

            val lastIndex= n-1
            for_@ for(u in 0 until n){
                val rangeSizeItr= rangeSize - u
                val prevMove= if(moveList.isNotEmpty()) moveList[moveList.lastIndex] else null

                var loop= true
                var o= -1
                while_@ while(loop){
                    if(++o >= 7) break
                    //prine("SWAPn u= $u o= $o moveList.size= ${moveList.size}")
                    try {
                        var fromTeam: Int
                        if(u < lastIndex || prevMove == null){
                            do {
                                fromTeam= range.random()
                            } while(rangeSizeItr > 0 && fromTeam in fromTeamList)
                        } else {
                            fromTeam= moveList[0].to
                        }

                        var toTeam= prevMove?.from ?: fromTeam
                        if(prevMove == null){
                            while(rangeSize > 0 && toTeam == fromTeam){
                                toTeam= range.random()
                            }
                        }

                        //val fromTimeslot= currentSchedule.getTimeslotAssert(fromTeam)
                        val fromPizzaList= currentDelivery.getSingleDeliveryByTeam(fromTeam)!!.pizzas //currentSchedule[fromTimeslot]!!
//                        val toTimeslot= currentSchedule.getTimeslot(fromTimeslotNo)

                        var movedPizzaId= currentDelivery.getSingleDeliveryByTeam(fromTeam)!!.pizzas.random().id //currentSchedule[fromTimeslot]!!.random().id
//                        val movedDestCourseId= currentSchedule[toTimeslot]!!.random().id

                        val fromPizzaListRemainSize= fromPizzaList.size - u
                        while(moveList.any { it.id == movedPizzaId }){
                            if(fromPizzaListRemainSize <= 0)
                                continue@while_
                            movedPizzaId= fromPizzaList.random().id
                        }

                        val prevPizzaId= prevMove?.id
/*
                        when {
                            u < lastIndex -> prevMove?.id
                            moveList.isNotEmpty() -> moveList[0].id
                            else -> null
                        }
 */
                        val timeslotValid = currentDelivery.checkConflictInTeam(
                            toTeam, menu, movedPizzaId
                        ){
                            it != prevPizzaId
                        }

                        if(timeslotValid) {
                            //val penalty= Util.getPenaltyComponentAt(movedCourseId, courseAdjacencyMatrix, currentSchedule)
                            moveList += PizzaMove(movedPizzaId, fromTeam, toTeam)
                        } else {
                            continue@while_
                        }
                        fromTeamList += fromTeam
                        loop= false
                    } catch(e: NoSuchElementException){
                        // Abaikan
                    }
                }
            }
            return if(moveList.size == n) moveList.toTypedArray() else null
        }
    }
}