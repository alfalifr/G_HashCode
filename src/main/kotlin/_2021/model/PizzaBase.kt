package _2021.model

import sidev.lib.collection.copy
import sidev.lib.reflex.getContentHashCode
import sidev.lib.structure.data.Cloneable

data class PizzaBase(
    val ingredients: Set<Int>, val ids: MutableSet<Int>
): Cloneable<PizzaBase> {
    val count: Int
        get()= ids.size

    fun isEmpty(): Boolean = ids.size <= 0

    fun getNext(removeAfter: Boolean = true): Pizza? = get(ids.first(), removeAfter)
    operator fun get(pizzaId: Int, removeAfter: Boolean = true): Pizza? {
        if(pizzaId !in ids) return null
        if(removeAfter)
            ids.remove(pizzaId)
        return Pizza(pizzaId, ingredients)
    }
    operator fun contains(pizzaId: Int): Boolean = pizzaId in ids

    override fun equals(other: Any?): Boolean {
        if(other === null) return false

        val otherIng: Set<Any?>? = when {
            other is PizzaBase -> other.ingredients
            other is Set<*> -> other
            else -> null
        }

        return ingredients.size == otherIng?.size && run {
            var bool= true
            for(e in ingredients)
                if(e in otherIng){
                    bool= false
                    break
                }
            bool
        }
    }

    override fun hashCode(): Int = getContentHashCode(ingredients)

    override fun clone_(isShallowClone: Boolean): PizzaBase {
        if(isShallowClone) return copy()
        val ingredients= ingredients.copy()
        val ids= ids.copy().toMutableSet()
        return PizzaBase(ingredients, ids)
    }
}