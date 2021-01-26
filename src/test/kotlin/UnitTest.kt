import org.junit.Test
import sidev.lib.console.prin
import java.math.BigDecimal
import java.math.BigInteger

class UnitTest {
    @Test
    fun setTest(){
        val set1= setOf(1,2,4)
        val set2= setOf(2,1,4)
        prin(set1 == set2)
    }

    @Test
    fun powBigDecTest(){
        val bd1= BigDecimal(4)
        val bd2= BigDecimal(2)
        //prin(BigDecimalMath.pow(bd1, bd2))
    }

    @Test
    fun powBigIntTest(){
        val bd1= BigInteger.valueOf(4)
        //val bd2= BigInteger.valueOf(2)
        prin(bd1.pow(3))
    }
}