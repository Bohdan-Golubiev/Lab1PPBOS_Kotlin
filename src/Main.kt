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

    val array = IntArray(arraySize) { 1 }

    /*println("Розмір масиву: ${array.size}")
    val start = System.nanoTime()
    val sum = sumInMainThread(array)
    val end = System.nanoTime()
    println("Час виконання: ${(end - start) / 1_000_000} мс")
    println("Загальна сума елементів масиву: $sum")
    println("Очікувана сума (перевірка): ${array.size.toLong()}")*/


    /*println("Розмір масиву: ${array.size}")
    val start = System.nanoTime()
    val sum = sumMultiThread(array, threadCount)
    val end = System.nanoTime()
    println("Час виконання: ${(end - start) / 1_000_000} мс")
    println("Загальна сума елементів масиву: $sum")
    println("Очікувана сума (перевірка): ${array.size.toLong()}")*/


    println("Розмір масиву: ${array.size}")
    val start = System.nanoTime()
    runBlocking{
        val totalSum = sumWithCoroutines(array, threadCount)
        val end = System.nanoTime()
        println("Час виконання: ${(end - start) / 1_000_000} мс")
        println("Загальна сума елементів масиву: $totalSum")
        println("Очікувана сума (перевірка): ${arraySize.toLong()}")
    }
}

fun sumInMainThread(array: IntArray): Long
{
    var sum = 0L
    for(i in 0 until array.size)
    {
        sum += array[i]
    }
    return sum
}

suspend fun sumWithCoroutines(array: IntArray, threadCount: Int): Long = coroutineScope {

    val chunkSize = array.size / threadCount

    val deferred = (0 until threadCount).map { t ->
        val from = t * chunkSize
        val to = if (t == threadCount - 1) array.size else from + chunkSize
        async(Dispatchers.Default) {
            var sum = 0L
            for (i in from until to)
            {
                sum += array[i]
            }

            //println("Корутина ID: ${Thread.currentThread().id} оброблила діапазон [$from, $to)")
            sum
        }
    }
    deferred.awaitAll().sum()
}

fun sumMultiThread(array: IntArray, threadCount: Int ):Long {

    val chunkSize = array.size / threadCount
    val workers = Array(threadCount) { t ->
        val from = t * chunkSize
        val to = if (t == threadCount - 1) array.size else from + chunkSize
        SumWorker(array, from, to).apply { start() }
    }

    var totalSum = 0L
    for (worker in workers) {
        worker.join()
        totalSum += worker.partialSum
    }

    return totalSum
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
        //println("Потік ID: ${currentThread().id} оброблив діапазон [$fromIndex, $toIndex)")
    }
}