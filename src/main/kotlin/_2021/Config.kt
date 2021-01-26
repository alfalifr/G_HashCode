package _2021

import java.io.File

object Config {
    const val DATASET_DIR = "D:\\DataCloud\\OneDrive\\OneDrive - Institut Teknologi Sepuluh Nopember\\Aktif\\Lomba\\HashCode\\2021"
    val fileNames = arrayOf(
        "a_example",
        "b_little_bit_of_everything.in",
        "c_many_ingredients.in",
        "d_many_pizzas.in",
        "e_many_teams.in"
    )
    fun fileNameIndex(fileName: String): Int = fileNames.indexOf(fileName)

    fun getSolFileDir(fileNameIndex: Int): String = "$DATASET_DIR\\${fileNames[fileNameIndex].split(".")[0]}_jawab"
    fun getSolFile(fileNameIndex: Int): File = File(getSolFileDir(fileNameIndex))


    const val DEFAULT_ITERATIONS = 1_000_000
    const val DEFAULT_TEMPERATURE_INIT: Double = 35.0
    const val DEFAULT_LEVEL_INIT_PERCENTAGE: Double = 0.4
    const val DEFAULT_TABU_MOVE_PERCENTAGE: Double = 0.1
    const val DEFAULT_DECAY_RATE: Double = 0.12
}