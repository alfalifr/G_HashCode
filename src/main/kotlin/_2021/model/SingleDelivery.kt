package _2021.model

import sidev.lib.collection.asMutableList
import sidev.lib.collection.copy
import sidev.lib.structure.data.Cloneable

data class SingleDelivery(
    val teamId: Int, val member: Int, val pizzas: MutableList<Pizza>
): Cloneable<SingleDelivery> {
    val uniqueIngredients: Set<Int> = mutableSetOf()

    fun updateIngredients(){
        val mutSet = uniqueIngredients as MutableSet
        mutSet.clear()
        for(p in pizzas)
            mutSet += p.ingredients.toSet()
    }

    fun uniqueIngredientsCount(
        addedPizzaIngredients: List<Pizza>?= null,
        removedPizzaId: List<Int>?= null,
    ): Int {
        if(addedPizzaIngredients == null && removedPizzaId == null)
            return uniqueIngredients.size
        val countedPizza= pizzas.copy().asMutableList()
        if(removedPizzaId != null)
            for(id in removedPizzaId){
                countedPizza.removeIf { it.id == id }
            }
        if(addedPizzaIngredients != null)
            for(p in addedPizzaIngredients){
                countedPizza += p
            }
        val set= mutableSetOf<Int>()
        for(p in countedPizza)
            set += p.ingredients.toSet()
        return set.size
    }

    override fun clone_(isShallowClone: Boolean): SingleDelivery {
        return if(isShallowClone) copy()
        else copy(pizzas = ArrayList(pizzas))
    }
}