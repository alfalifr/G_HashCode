package _2021

import _2021.model.Delivery
import _2021.model.Menu
import _2021.model.Pizza
import _2021.model.SingleDelivery
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.check.notNull
import sidev.lib.collection.asMutableList

enum class Construct(val code: String, val fullName: String) {
    _INIT("_0", "No sorting"),
    LM("lm", "Least Member"),
    LM_GI("lm_gi", "Least Member Great Ingredient"),
    LM_GPC("lm_gpc", "Least Member Great Pizza Count"),
    GM("gm", "Great Member"),
    LI("li", "Least Ingredient"),
    GM_LI("gm_li", "Great Member Least Ingredient"),
    GM_GI("gm_gi", "Great Member Great Ingredient"),
    GM_GWI("gm_gwi", "Great Member Great Weighted Ingredient"),
    GM_GPC("gm_gpc", "Great Member Great Pizza Count"),
    ;

    companion object {
        fun lm(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sort()
            return assignDelivery(menu, countList).apply {
                tag.construct = LM
            }
        }
        fun lm_gi(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortByDescending { it.ingredients.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sort()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = LM_GI
            }
        }
        fun lm_gpc(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortByDescending { it.ids.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sort()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = LM_GPC
            }
        }
        fun gm(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sortDescending()
            return assignDelivery(menu, countList).apply {
                tag.construct = GM
            }
        }
        fun li(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortBy { it.ingredients.size }
            val countList = Util.toLinear(memberCountMap)
            return assignDelivery(menu, countList, false).apply {
                tag.construct = LI
            }
        }
        fun gm_li(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortBy { it.ingredients.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sortDescending()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = GM_LI
            }
        }
        fun gm_gi(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortByDescending { it.ingredients.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sortDescending()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = GM_GI
            }
        }
        fun gm_gwi(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortByDescending { it.ingredients.size * it.ids.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sortDescending()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = GM_GWI
            }
        }
        fun gm_gpc(menu: Menu, memberCountMap: Map<Int, Int>): Delivery {
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= menu.clone_(false)
            menu.pizzas.sortByDescending { it.ids.size }
            val countList = Util.toLinear(memberCountMap).asMutableList()
            countList.sortDescending()
            return assignDelivery(menu, countList, false).apply {
                tag.construct = GM_GPC
            }
        }

        fun assignDelivery(menu: Menu, memberCountMap: Map<Int, Int>): Delivery =
            assignDelivery(menu, Util.toLinear(memberCountMap))
        /**
         * [memberCountList] -> list of number member
         */
        fun assignDelivery(menu: Menu, memberCountList: List<Int>, cloneMenuFirst: Boolean = true): Delivery {
            //prine("memberCountList=======")
            //prine(memberCountList.joinToString("\n"))
            @Suppress(SuppressLiteral.NAME_SHADOWING)
            val menu= if(cloneMenuFirst) menu.clone_(false) else menu
            val deliv = Delivery()
            for((i, count) in memberCountList.withIndex()){
                val pizzas = mutableListOf<Pizza>()
                if(menu.pizzas.size >= count){
                    var o= 0
                    for(u in 0 until count){
                        val pb= menu.pizzas[o]
                        pb.getNext().notNull {
                            pizzas += it
                        }
                        if(pb.isEmpty())
                            menu.pizzas.removeAt(o)
                        else
                            o++
                    }
                    deliv.order += SingleDelivery(i, count, pizzas)
                }
            }
            return deliv
        }
    }
}