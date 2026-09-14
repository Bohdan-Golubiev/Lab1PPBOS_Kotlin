import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
fun main() {
    val arraySize = 1000000000
    val threadCount = 8

    //defaultOption(arraySize, threadCount)
    runBlocking{
        val totalSum = sumWithCoroutines(arraySize, threadCount)
        println("Загальна сума елементів масиву: $totalSum")
        println("Очікувана сума (перевірка): ${arraySize.toLong()}")
    }
}

suspend fun sumWithCoroutines(arraySize: Int, threadCount: Int): Long = coroutineScope {
    println("Розмір масиву: $arraySize")
    println("Кількість потоків: $threadCount")

    val array = IntArray(arraySize) { 1 }
    val chunkSize = arraySize / threadCount

    val deferred = (0 until threadCount).map { t ->
        val from = t * chunkSize
        val to = if (t == threadCount - 1) array.size else from + chunkSize
        async(Dispatchers.Default) {
            var sum = 0L
            for (i in from until to)
            {
                sum += array[i]
            }

            println("Корутина ID: ${Thread.currentThread().id} оброблила діапазон [$from, $to)")

            sum
        }
    }
    deferred.awaitAll().sum()
}

fun defaultOption(arraySize: Int, threadCount: Int ){

    println("Розмір масиву: $arraySize")
    println("Кількість потоків: $threadCount")

    val array = IntArray(arraySize) { 1 }

    val chunkSize = arraySize / threadCount
    val workers = Array(threadCount) { t ->
        val from = t * chunkSize
        val to = if (t == threadCount - 1) arraySize else from + chunkSize
        SumWorker(array, from, to).apply { start() }
    }

    var totalSum = 0L
    for (worker in workers) {
        worker.join()
        totalSum += worker.partialSum
    }

    println("Загальна сума елементів масиву: $totalSum")
    println("Очікувана сума (перевірка): ${arraySize.toLong()}")
}

class SumWorker(
    private val array: IntArray,
    private val fromIndex: Int,
    private val toIndex: Int
) : Thread() {

    var partialSum: Long = 0L
        private set

    override fun run() {
        var sum = 0L
        for (i in fromIndex..<toIndex) {
            sum += array[i]
        }
        partialSum = sum
        println("Потік ID: ${currentThread().id} оброблив діапазон [$fromIndex, $toIndex)")
    }
}