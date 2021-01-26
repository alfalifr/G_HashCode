package _2021.model

import sidev.lib.collection.copy
import sidev.lib.structure.data.Cloneable

data class Menu(
    //val pizzaKey: HashSet<Pizza>,
    val ingredientNumber: MutableMap<String, Int> = mutableMapOf(),
    val pizzas: MutableList<PizzaBase> = mutableListOf(),
    //val pizzaCount: MutableMap<Pizza, Int> = mutableMapOf(),
    //val pizzaId: MutableMap<Pizza, Int> = mutableMapOf(),
): Cloneable<Menu> {

    fun indexOf(ingredients: Set<Int>): Int = pizzas.indexOfFirst { it.ingredients == ingredients }
    operator fun contains(ingredients: Set<Int>): Boolean = pizzas.any { it.ingredients == ingredients }
    fun remove(pizzaId: Int): Pizza? = get(pizzaId, false)
    operator fun get(pizzaId: Int, keepAfter: Boolean = true): Pizza? {
        var foundIngredients: Set<Int>? = null
        var i= 0
        for(p in pizzas){
            if(pizzaId in p){
                foundIngredients = p.ingredients
                if(!keepAfter){
                    p.ids -= pizzaId
                    if(p.ids.size <= 0)
                        pizzas.removeAt(i)
                }
                break
            }
            i++
        }
        return if(foundIngredients == null) null
        else Pizza(pizzaId, foundIngredients)
    }
    override fun clone_(isShallowClone: Boolean): Menu {
        if(isShallowClone) return copy()
        val ingredientMap = ingredientNumber.copy() as MutableMap
        val pizzas = mutableListOf<PizzaBase>() //pizzas.copy().toMutableList()
        for(p in this.pizzas)
            pizzas += p.clone_(false)
        return Menu(ingredientMap, pizzas)
    }

    override fun toString(): String = pizzas.joinToString("\n")
}