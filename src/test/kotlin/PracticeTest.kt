import _2021.Config
import _2021.Construct
import _2021.Util
import _2021.model.Menu
import _2021.optimize.Evaluation
import _2021.optimize.Optimize
import org.junit.Test
import sidev.lib.console.prin
import java.io.File

class PracticeTest {
    @Test
    fun exampleTest(){
        val fileName= "e_many_teams.in" //"b_little_bit_of_everything.in" //"d_many_pizzas.in" //"c_many_ingredients.in" //"a_example"
        val lines= Util.readFile(fileName)
        //prin(lines.joinToString("\n"))
        val menu= Util.toMenu(lines)
        //prin("===== menu =======")
        //prin(menu)
        val memberCountMap = Util.getMemberCountMap(lines)
        val deliv= Construct.gm_li(menu, memberCountMap)
        //prin("===== deliv =====")
        //prin(deliv)
        print(Util.calculateScore(deliv))

        val destFile= File(Config.DATASET_DIR +"\\${fileName.split(".")[0]}_jawab")
        Util.saveRes(destFile, deliv)
    }

    @Test
    fun runAllConstruct(){
        val res= Util.Construct_.runAllContruct(2)
        prin("======== best ==========")
        prin(Util.Construct_.getBest(res).miniString())
    }

    @Test
    fun runAllConstructCase(){
        //TODO menuContainer menyebabkan Java heap space OOM
        //val menuContainer= mutableMapOf<String, Menu>()
        val res= Util.Construct_.runAllDelivery() //menuContainer
        prin("======== best ==========")
        res.forEach {
            prin(it.value.miniString())
        }
/*
        prin("========= optimasi =========")
        val fileName= Config.fileNames[0]
        val opt1= Optimize.rw(res[fileName]!!, menuContainer[fileName]!!, Evaluation.BETTER)
        prin(opt1?.first?.miniString())
        //prin(Util.Construct_.getBest(res).miniString())
 */
    }

    @Test
    fun runOpt(){
        val fileIndex= 3
        val menuContainer= mutableMapOf<String, Menu>()
        val res= Util.Construct_.runAllContruct(fileIndex, menuContainer)
        val best= Util.Construct_.getBest(res)
        prin("======== best ==========")
        prin(best.miniString())

        prin("========= optimasi =========")
        val fileName= Config.fileNames[fileIndex]
        val opt1= Optimize.rw(
            best, menuContainer[fileName]!!, Evaluation.SIMULATED_ANNEALING(Config.DEFAULT_TEMPERATURE_INIT, Config.DEFAULT_DECAY_RATE),
            iterations = 1_000_00
        )
        prin(opt1?.first?.miniString())
        //prin(Util.Construct_.getBest(res).miniString())
    }
}