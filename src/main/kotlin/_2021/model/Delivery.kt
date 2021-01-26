package _2021.model

import sidev.lib.collection.findIndexed
import sidev.lib.structure.data.Cloneable

data class Delivery(
    val order: MutableList<SingleDelivery> = mutableListOf(),
    val tag: DeliveryTag = DeliveryTag(),
    //var initPizzaCount: Int = -1,
): Iterable<SingleDelivery>, Cloneable<Delivery> {
    val pizzaCount: Int
        get()= order.map { it.pizzas.size }.sum()

    fun getSingleDelivery(pizzaId: Int): SingleDelivery? = order.find { it.pizzas.any { it.id == pizzaId } }
    fun getSingleDeliveryByTeam(id: Int): SingleDelivery? = order.find { it.teamId == id }

    fun moveById(id: Int, fromTeam: Int, toTeam: Int){
        var movedPizza: IndexedValue<Pizza>?= null
        order.find { it.teamId == fromTeam }!!.also { singleDel ->
            movedPizza = singleDel.pizzas.findIndexed { it.value.id == id }
            singleDel.pizzas.removeAt(movedPizza!!.index)
        }
        order.find { it.teamId == toTeam }!!.also { singleDel ->
            singleDel.pizzas.add(movedPizza!!.value)
        }
    }

    fun checkConflictInTeam(
        teamId: Int,
        menu: Menu,
        addedPizzaId: Int,
        predicate: ((pizzaId: Int) -> Boolean)?= null
    ): Boolean {
        val sameIngPizzas = menu.pizzas.find { addedPizzaId in it.ids }!!
        val singleDel = order.find { it.teamId == teamId }!!
        for(p in singleDel.pizzas)
            if(p.id in sameIngPizzas
                && (predicate == null || predicate(p.id))
            )
                return false
        return true
    }

    override fun iterator(): Iterator<SingleDelivery> = order.iterator()
    override fun toString(): String = order.joinToString("\n")
    fun miniString(): String = tag.toString()

    override fun clone_(isShallowClone: Boolean): Delivery {
        if(isShallowClone) return copy()
        val tag= tag.copy()
        val order= mutableListOf<SingleDelivery>()
        for(o in this.order)
            order += o.clone_(false)
        return Delivery(order, tag)
    }
}