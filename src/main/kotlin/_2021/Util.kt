package _2021

import _2021.model.*
import sidev.lib.check.isNull
import sidev.lib.check.notNull
import sidev.lib.console.prin
import sidev.lib.console.prine
import sidev.lib.jvm.tool.util.FileUtil
import java.awt.MenuContainer
import java.io.File
import java.math.BigInteger
import java.util.*

//import side

object Util {
    object Construct_ {
        fun getBest(solutions: List<Delivery>): Delivery {
            var res= solutions.first()
            for(i in 1 until solutions.size){
                val sol= solutions[i]
                if(sol.tag.score > res.tag.score)
                    res= sol
            }
            return res
        }

        fun runAllContruct(fileNameIndex: Int, menuContainer: MutableMap<String, Menu>?= null): List<Delivery> {
            val fileName= Config.fileNames[fileNameIndex]
            //val fileNameDir= Config.getSolFileDir(fileNameIndex)
            val lines= readFile(fileName)
            val menu= toMenu(lines)
            val memberCountMap = getMemberCountMap(lines)

            menuContainer?.put(fileName, menu)

            //prine("========== lines ==========")
            //prine(lines.joinToString("\n"))

            prin("================== Construct - $fileName Mulai =================")
            val results= mutableListOf<Delivery>()

            val d1= Construct.gm(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            //val d2= Construct.li(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            val d3= Construct.gm_li(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            val d4= Construct.gm_gi(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            val d5= Construct.gm_gpc(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            //val d6= Construct.gm_gwi(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            //val d7= Construct.lm(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            //val d8= Construct.lm_gi(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }
            val d9= Construct.lm_gpc(menu, memberCountMap).also { it.tag.fileName = fileName; calculateScore(it); results += it }

            //print(calculateScore(deliv))
            for(res in results){
                prin(res.miniString())
            }
            return results
        }

        fun runAllDelivery(menuContainer: MutableMap<String, Menu>?= null): Map<String, Delivery> {
            val results= mutableListOf<Delivery>()
            for(i in Config.fileNames.indices){
                results += getBest(runAllContruct(i, menuContainer))
            }
            val map= mutableMapOf<String, Delivery>()
            for(res in results){
                map[res.tag.fileName!!] = res
            }
            return map
        }
    }

    fun readFile(name: String): List<List<String>> {
        val file = File(Config.DATASET_DIR + "\\$name")
        prine(Config.DATASET_DIR + "\\$name")
        val scanner = Scanner(file)
        val list= mutableListOf<List<String>>()
        while(scanner.hasNextLine())
            list += scanner.nextLine().trim().split(" ")
        return list
    }

    fun getMemberCountMap(rawList: List<List<String>>): Map<Int, Int> {
        val memberCounts= rawList.first()
        val map= mutableMapOf<Int, Int>()

        var member = 2
        for(i in 1 until memberCounts.size){
            val count = memberCounts[i].toInt()
            map[member++]= count
        }
        return map
    }

    fun toMenu(rawList: List<List<String>>): Menu {
        val menu = Menu()
        var ingNo = 0
        val ingNoMap = mutableMapOf<String, Int>()
        for(i in 1 until rawList.size){
            val ingLs= rawList[i]
            val ingSet= mutableSetOf<Int>()
            for(u in 1 until ingLs.size){
                val ing= ingLs[u]
                ingNoMap[ing].isNull {
                    ingSet += ingNo
                    ingNoMap[ing] = ingNo++
                }.notNull {
                    ingSet += it
                }
            }
            val pizzaBaseInd= menu.indexOf(ingSet)
            if(pizzaBaseInd < 0)
                menu.pizzas += PizzaBase(ingSet, mutableSetOf(i-1))
            else
                menu.pizzas[pizzaBaseInd].ids += i-1
        }
        return menu
    }

    fun calculateScore(delivery: Delivery, updateFirst: Boolean = true): BigInteger {
        var result= BigInteger.valueOf(0)
        for(singleDel in delivery){
            if(updateFirst)
                singleDel.updateIngredients()
            val ingInt = BigInteger.valueOf(singleDel.uniqueIngredients.size.toLong())
            result += ingInt.pow(2)
        }
        return result.also {
            delivery.tag.score= it
            //prine(it)
        }
    }

    fun toLinear(memberCountMap: Map<Int, Int>): List<Int> {
        val countList= mutableListOf<Int>()
        for((member, count) in memberCountMap){
            for(i in 0 until count){
                countList += member
            }
        }
        return countList
    }

    fun saveRes(file: File, delivery: Delivery): Boolean {
        file.delete()
        if(!FileUtil.saveln(file, delivery.order.size.toString(), false))
            return false
        for(singleDel in delivery){
            var lineStr= singleDel.member.toString()
            for(p in singleDel.pizzas){
                lineStr += " ${p.id}"
            }
            if(!FileUtil.saveln(file, lineStr, true))
                return false
        }
        return true
    }
}