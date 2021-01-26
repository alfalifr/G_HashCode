package _2021.optimize

import _2021.model.Delivery
import _2021.model.Pizza
import _2021.model.PizzaMove
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.math.random.randomBoolean
import java.math.BigInteger
import kotlin.math.abs
import kotlin.math.exp

/**
 * Kelas algoritma fungsi evaluasi penalty. Kelas ini berfungsi untuk menentukan
 * apakah solusi baru dapat diterima atau tidak.
 */
sealed class Evaluation {
    abstract fun evaluate(delivery: Delivery, moves: Array<PizzaMove>): Boolean
    override fun toString(): String = this::class.simpleName!!

    object BETTER : Evaluation() {
        override fun evaluate(delivery: Delivery, moves: Array<PizzaMove>): Boolean {
            val (prevPenaltySum, currentPenaltySum) = getIngredientChangeCount(delivery, moves)
            return currentPenaltySum > prevPenaltySum
        }
    }
    class SIMULATED_ANNEALING(
        initTemperature: Double, decayRate: Double
    ): Evaluation(){
        val decayRate: Double = if(decayRate < 1) decayRate else 1 / decayRate
        var currTemperature: Double = initTemperature
            private set
        override fun evaluate(delivery: Delivery, moves: Array<PizzaMove>): Boolean {
            val (prevPenaltySum, currentPenaltySum) = getIngredientChangeCount(delivery, moves)

            val accept= if(currentPenaltySum > prevPenaltySum) true
            else {
                //val acc = 1 / (1 + exp((currentPenaltySum - prevPenaltySum) / currTemperature))
                val acc = exp(-abs(currentPenaltySum - prevPenaltySum) / currTemperature)
                randomBoolean(acc)
            }
            currTemperature -= currTemperature * decayRate
            return accept
        }
    }
    class GREAT_DELUGE(
        initLevel: Long, decayRate: Double
    ): Evaluation(){
        val decayRate: Double = if(decayRate < 1) decayRate else 1 / decayRate
        var currLevel: Long = initLevel
            private set
        override fun evaluate(delivery: Delivery, moves: Array<PizzaMove>): Boolean {
            val (prevPenaltySum, currentPenaltySum) = getIngredientChangeCount(delivery, moves)

            val accept= if(currentPenaltySum > prevPenaltySum) true
            else currentPenaltySum >= currLevel
            currLevel -= (currLevel * decayRate).toLong()
            return accept
        }
    }
    class TABU(tabuMoveSize: Int = 20): Evaluation(){
        private val tabuMoves= arrayOfNulls<PizzaMove>(tabuMoveSize)
        private var tabuMovePointer= 0
        override fun evaluate(delivery: Delivery, moves: Array<PizzaMove>): Boolean {
            //val cutIndex= mutableListOf<Int>()
            var cutSize= 0
            for(i in moves.indices){
                val move= moves[i]
                if(tabuMoves.any { it?.id == move.id && it.to == move.to }){
                    @Suppress(SuppressLiteral.UNCHECKED_CAST)
                    (moves as Array<PizzaMove?>)[i]= null
                    //cutIndex.add(i)
                    cutSize++
                }
            }
            val newMoves= if(cutSize == 0) moves else {
                var diff= 0
                //val moves= (moves as Array<CourseMove?>)
                Array(moves.size - cutSize){
                    var move= moves[it + diff]
                    while(move == null)
                        move= moves[it + (++diff)]
                    move
                }
            }
            val (prevPenaltySum, currentPenaltySum) = getIngredientChangeCount(delivery, moves)

            val size= newMoves.size
            for(move in newMoves) {
                tabuMoves[tabuMovePointer]= move
                tabuMovePointer= (tabuMovePointer + 1) % size
            }
            return currentPenaltySum < prevPenaltySum
        }
    }

    companion object {
        /**
         * Return the count of unique ingredient between previous and after move.
         */
        fun getIngredientChangeCount(
            delivery: Delivery, //prevDistanceMatrix: DistanceMatrix, //Array<Array<Pair<Int, Int>>>,
            moves: Array<PizzaMove>
        ): Pair<Int, Int> {
            var prevPenaltySum= 0
            var currentPenaltySum= 0

            //1. Simulasi semua pizza setelah dipindahkan
            val movedPizza= mutableMapOf<Int, MutableList<Pizza>>() //Key nya adalah teamId
            for(move in moves){
                val pizzaId= move.id

                val addedPizaList= movedPizza[move.to] ?: mutableListOf<Pizza>().also {
                    movedPizza[move.to]= it
                }
                addedPizaList += delivery.getSingleDeliveryByTeam(move.from)!!
                    .pizzas.find { it.id == pizzaId }!!

                val removedPizaList= movedPizza[move.from] ?: mutableListOf<Pizza>().also {
                    movedPizza[move.from]= it
                }
                removedPizaList.removeIf { it.id == pizzaId }
            }
            //2. Hitung total jml ingredient unik sebelum dan sesudah.
            for((teamId, pizzas) in movedPizza){
                val singleDel= delivery.order.find { it.teamId == teamId }!!
                val currIng= mutableSetOf<Int>()
                for(p in pizzas)
                    currIng += p.ingredients.toSet()

                prevPenaltySum += singleDel.uniqueIngredients.size
                currentPenaltySum += currIng.size
            }
            return prevPenaltySum to currentPenaltySum
        }
    }

    /**
     * [penaltyChange] mamakai [CourseMove] sebagai index karena tidak memperhatikan
     * perubahan pada timeslot yang ditinggalkan. Fungsi ini hanya memperhatikan timeslot tujuan
     * karena sifat timeslot asal mirip dengan timeslot tujuan. Fungsi ini hanya menghitung
     * selisih antar 2 timeslot tujuan secara absolut.
     */
    operator fun invoke(
        delivery: Delivery, //prevDistanceMatrix: DistanceMatrix, //Array<Array<Pair<Int, Int>>>,
        moves: Array<PizzaMove>
    ): Boolean = evaluate(delivery, moves)
}