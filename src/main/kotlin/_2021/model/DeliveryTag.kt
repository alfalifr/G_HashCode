package _2021.model

import _2021.Construct
import _2021.optimize.Optimize
import java.math.BigInteger

data class DeliveryTag(
    var fileName: String?= null,
    var construct: Construct = Construct._INIT,
    var optimization: Optimize = Optimize.NOT_YET,
    var score: BigInteger = BigInteger.ZERO
){
    override fun toString(): String = "$fileName - ${construct.code} - ${optimization.code} - score=$score"
}