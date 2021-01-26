package _2021.model

import sidev.lib.reflex.getContentHashCode

data class Pizza(val id: Int, val ingredients: Set<Int>) {
    fun ingredientEquals(other: Pizza): Boolean =
        ingredients.size == other.ingredients.size
        && run {
            var bool= true
            val ing2 = other.ingredients
            for(e in ingredients)
                if(e !in ing2){
                    bool= false
                    break
                }
            bool
        }
/*
    fun strictEquals(other: Any?): Boolean = other is Pizza
            && id == other.id
            && ingredientEquals(other)
 */
    override fun equals(other: Any?): Boolean = other is Pizza
            && id == other.id
            && ingredientEquals(other)

    override fun hashCode(): Int = getContentHashCode(ingredients, false)
}