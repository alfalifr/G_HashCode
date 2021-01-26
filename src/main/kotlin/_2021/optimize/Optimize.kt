package _2021.optimize

import _2021.Config
import _2021.Util
import _2021.model.Delivery
import _2021.model.Menu
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.exception.IllegalArgExc
import java.math.BigInteger

/**
 * Entry point convenient untuk melakukan optimasi terhadap initial solution.
 */
enum class Optimize(val code: String /*val evaluation: Evaluation? = null,*/ /*vararg val lowLevels: LowLevel*/) {
    NOT_YET("_0"),
    HC_MOVE("hc_m"),
    SA_MOVE("sa_m"),
    GD_MOVE("gd_m"),
    TA_MOVE("ta_m"),
    HC_MOVEn("hc_mN"),
    SA_MOVEn("sa_mN"),
    GD_MOVEn("gd_mN"),
    TA_MOVEn("ta_mN"),
    HC_SWAP("hc_s"),
    SA_SWAP("sa_s"),
    GD_SWAP("gd_s"),
    TA_SWAP("ta_s"),
    HC_SWAPn("hc_sN"),
    SA_SWAPn("sa_sN"),
    GD_SWAPn("gd_sN"),
    TA_SWAPn("ta_sN"),
    RW_HC("rw_hc"),
    RW_SA("rw_sa"),
    RW_GD("rw_gd"),
    RW_TA("rw_ta"),
    TA_HC("ta_hc"),
    TA_SA("ta_sa"),
    TA_GD("ta_gd"),
    TA_TA("ta_ta"),
    ;

    companion object {
        operator fun get(code: String): Optimize = enumValues<Optimize>().find {
            it.code == code
        } ?: throw IllegalArgExc(
            paramExcepted = arrayOf("code"),
            detailMsg = "Enum `Optimize` tidak punya entry dengan `code` ($code)"
        )

        fun lin_move(
            init: Delivery,
            menu: Menu,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = HighLevel.LINEAR(LowLevel.MOVE, evaluation).optimize(
            init, menu, iterations
        )

        fun lin_moveN(
            init: Delivery,
            menu: Menu,
            n: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = HighLevel.LINEAR(LowLevel.MOVE_N(n), evaluation).optimize(
            init, menu, iterations
        )

        fun lin_swap(
            init: Delivery,
            menu: Menu,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = HighLevel.LINEAR(LowLevel.SWAP, evaluation).optimize(
            init, menu, iterations
        )

        fun lin_swapN(
            init: Delivery,
            menu: Menu,
            n: Int,
            evaluation: Evaluation,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = HighLevel.LINEAR(LowLevel.SWAP_N(n), evaluation).optimize(
            init, menu, iterations
        )

        fun lin_move_hc(
            init: Delivery,
            menu: Menu,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = lin_move(
            init, menu, Evaluation.BETTER, iterations
        )

        fun lin_move_sa(
            init: Delivery,
            menu: Menu,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? = lin_move(
            init, menu,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_move_gd(
            init: Delivery,
            menu: Menu,
            initLevel: Long = -1,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.calculateScore(init, false)
                initPenalty.toLong() + (initPenalty.toLong() * Config.DEFAULT_LEVEL_INIT_PERCENTAGE).toLong()
            }
            return lin_move(
                init, menu,
                Evaluation.GREAT_DELUGE(initLevel, decayRate),
                iterations
            )
        }

        fun lin_move_ta(
            init: Delivery,
            menu: Menu,
            tabuMoveSize: Int = -1,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
                else (init.pizzaCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return lin_move(
                init, menu,
                Evaluation.TABU(tabuMoveSize),
                iterations
            )
        }

        fun lin_moveN_hc(
            init: Delivery,
            menu: Menu,
            n: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS
        ): Pair<Delivery, BigInteger>? = lin_moveN(
            init, menu, n, Evaluation.BETTER, iterations
        )

        fun lin_moveN_sa(
            init: Delivery,
            menu: Menu,
            n: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? = lin_moveN(
            init, menu, n,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_moveN_gd(
            init: Delivery,
            menu: Menu,
            n: Int,
            initLevel: Long = -1,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? {

            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.calculateScore(init, false)
                initPenalty.toLong() + (initPenalty.toLong() * Config.DEFAULT_LEVEL_INIT_PERCENTAGE).toLong()
            }
            return lin_moveN(
                init, menu, n,
                Evaluation.GREAT_DELUGE(initLevel, decayRate),
                iterations
            )
        }

        fun lin_moveN_ta(
            init: Delivery,
            menu: Menu,
            n: Int,
            tabuMoveSize: Int = -1,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
            else (init.pizzaCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return lin_moveN(
                init, menu, n,
                Evaluation.TABU(tabuMoveSize),
                iterations
            )
        }
/*
        fun lin_swap_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swap(
            init, courseAdjacencyMatrix, studentCount, Evaluation.BETTER, iterations
        )

        fun lin_swap_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swap(
            init, courseAdjacencyMatrix, studentCount,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_swap_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return lin_swap(
                init, courseAdjacencyMatrix, studentCount,
                Evaluation.GREAT_DELUGE(initLevel, decayRate),
                iterations
            )
        }

        fun lin_swap_ta(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            tabuMoveSize: Int = -1,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
            else (init.courseCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return lin_swap(
                init, courseAdjacencyMatrix, studentCount,
                Evaluation.TABU(tabuMoveSize),
                iterations
            )
        }

        fun lin_swapN_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swapN(
            init, courseAdjacencyMatrix, studentCount, n,
            Evaluation.BETTER, iterations
        )

        fun lin_swapN_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = lin_swapN(
            init, courseAdjacencyMatrix, studentCount, n,
            Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate),
            iterations
        )

        fun lin_swapN_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return lin_swapN(
                init, courseAdjacencyMatrix, studentCount, n,
                Evaluation.GREAT_DELUGE(initLevel, decayRate),
                iterations
            )
        }

        fun lin_swapN_ta(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            n: Int,
            tabuMoveSize: Int = -1,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
            else (init.courseCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return lin_swapN(
                init, courseAdjacencyMatrix, studentCount, n,
                Evaluation.TABU(tabuMoveSize),
                iterations
            )
        }
 */
        fun rw(
            init: Delivery,
            menu: Menu,
            evaluation: Evaluation,
            maxN: Int = 3,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Delivery, BigInteger>? = HighLevel.ROULLETE_WHEEL(maxN, evaluation).optimize(
            init, menu, iterations
        )
/*
        fun rw_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.ROULLETE_WHEEL(maxN, Evaluation.BETTER).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun rw_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.ROULLETE_WHEEL(
            maxN, Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate)
        ).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun rw_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return HighLevel.ROULLETE_WHEEL(
                maxN, Evaluation.GREAT_DELUGE(initLevel, decayRate)
            ).optimize(
                init, courseAdjacencyMatrix, studentCount, iterations
            )
        }

        fun rw_ta(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            tabuMoveSize: Int = 20,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
            else (init.courseCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return HighLevel.ROULLETE_WHEEL(
                maxN, Evaluation.TABU(tabuMoveSize)
            ).optimize(
                init, courseAdjacencyMatrix, studentCount, iterations
            )
        }


        fun tabu_hc(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            tabuLowLevelSize: Int = 5,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.TABU(maxN, tabuLowLevelSize, Evaluation.BETTER).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun tabu_sa(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            tabuLowLevelSize: Int = 5,
            initTemperature: Double = Config.DEFAULT_TEMPERATURE_INIT,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? = HighLevel.TABU(
            maxN, tabuLowLevelSize, Evaluation.SIMULATED_ANNEALING(initTemperature, decayRate)
        ).optimize(
            init, courseAdjacencyMatrix, studentCount, iterations
        )

        fun tabu_gd(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            tabuLowLevelSize: Int = 5,
            initLevel: Double = -1.0,
            decayRate: Double = Config.DEFAULT_DECAY_RATE,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val initLevel= if(initLevel > 0) initLevel else {
                val initPenalty= Util.getPenalty(init, courseAdjacencyMatrix, studentCount)
                initPenalty + initPenalty * Config.DEFAULT_LEVEL_INIT_PERCENTAGE
            }
            return HighLevel.TABU(
                maxN, tabuLowLevelSize, Evaluation.GREAT_DELUGE(initLevel, decayRate)
            ).optimize(
                init, courseAdjacencyMatrix, studentCount, iterations
            )
        }

        fun tabu_ta(
            init: Schedule,
            courseAdjacencyMatrix: Array<IntArray>,
            studentCount: Int,
            maxN: Int,
            tabuLowLevelSize: Int = 5,
            tabuMoveSize: Int = 20,
            iterations: Int = Config.DEFAULT_ITERATIONS,
        ): Pair<Schedule, Double>? {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val tabuMoveSize= if(tabuMoveSize > 0) tabuMoveSize
            else (init.courseCount * Config.DEFAULT_TABU_MOVE_PERCENTAGE).toInt()

            return HighLevel.TABU(
                maxN, tabuLowLevelSize, Evaluation.TABU(tabuMoveSize)
            ).optimize(
                init, courseAdjacencyMatrix, studentCount, iterations
            )
        }
 */
    }
}