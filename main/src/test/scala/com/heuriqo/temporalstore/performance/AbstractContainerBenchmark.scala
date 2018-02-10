package com.heuriqo.temporalstore.performance

import com.heuriqo.temporalstore.datastore._

import scala.collection._
import scala.util.Random

abstract class AbstractContainerBenchmark {
    protected val seedGenerator = new Random
    protected val exportDataSeparator = ", "

    case class InvocationsCounter(invocations: Int, totalNanoTime: Long) {
        def plus(nanoTime: Long): InvocationsCounter = InvocationsCounter(invocations+1, totalNanoTime + nanoTime)
    }

    class OperationStats(collectCurveData: Boolean = false) {
        var numberOfInvocations: Long = 0
        var totalTime: Long = 0
        val timesMap: mutable.Map[Int, InvocationsCounter] = new mutable.HashMap[Int, InvocationsCounter]()

        def averageTimeNanos: Double = totalTime.toDouble / numberOfInvocations / 1000

        def addInvocation(nanoTime: Long, containerSize: Int) {
            numberOfInvocations += 1
            totalTime += nanoTime
            if (collectCurveData) {
                val logSize = math.log10(math.max(containerSize.toDouble, 1.0))
                val sizeFactor: Int = (logSize * 100).toInt
                timesMap.get(sizeFactor) match {
                    case Some(counter) => timesMap.put(sizeFactor, counter plus nanoTime)
                    case None => timesMap.put(sizeFactor, InvocationsCounter(1, nanoTime))
                }
            }
        }
    }

    class Stats(collectCurveData: Boolean = false) {
        val insert: OperationStats = new OperationStats(collectCurveData)
        val update: OperationStats = new OperationStats(collectCurveData)
        val clearFrom: OperationStats = new OperationStats(collectCurveData)
        val query: OperationStats = new OperationStats(collectCurveData)
        var totalSeconds: Double = 0.0
    }


    def exp10(n: Int): Int =  {
        var result = 1
        for (i <- 1 to n)
            result *= 10
        return result
    }

    class Experiment(deletesEnabled: Boolean, deleteOperationFrequencyReciprocal: Int, queriesToInsertsRatio: Double, numberOfOperations: Int, timepointsRange: Int) {
        val implA: HistoryContainer = new ArrayBufferHistoryContainer(10)
        val implB: HistoryContainer = new ArrayBufferHistoryContainer(1000)
        val implC: HistoryContainer = new ImmutableRedBlackTreeHistoryContainer
        val implD: HistoryContainer = new MutableRedBlackTreeHistoryContainer
        val randomSeed = seedGenerator.nextLong()

        def execute() {
            println(s"=============== EXPERIMENT ===============")
            println(s"number of API operations: $numberOfOperations")
            println(s"timepoints range: $timepointsRange")
            println(s"queries-to-updated ratio: $queriesToInsertsRatio")
            println(s"clear-from invoked with probability: 1/$deleteOperationFrequencyReciprocal")
            println()
            executeForContainerWithStatsPrettyPrinting(implA, "array buffer implementation, initial size = 10")
            executeForContainerWithStatsPrettyPrinting(implB, "array buffer implementation, initial size = 1000")
            executeForContainerWithStatsPrettyPrinting(implC, "scala (immutable) TreeMap implementation")
            executeForContainerWithStatsPrettyPrinting(implD, "java (mutable) TreeMap implementation")
        }

        //useful for collecting data for graphical visualisaion
        def executeWithStructuredStatsOutput() {
            val statsA = new Stats
            measurePerformanceOf(implA, statsA)
            exportStatsForAnalysis("A", statsA)

            val statsB = new Stats
            measurePerformanceOf(implB, statsB)
            exportStatsForAnalysis("B", statsB)
            
            val statsC = new Stats
            measurePerformanceOf(implC, statsC)
            exportStatsForAnalysis("C", statsC)
            
            val statsD = new Stats
            measurePerformanceOf(implD, statsD)
            exportStatsForAnalysis("D", statsD)
        }

        def executeWithCumulativeStats(statsA: Stats, statsB: Stats, statsC: Stats, statsD: Stats) {
            measurePerformanceOf(implA, statsA)
            measurePerformanceOf(implB, statsB)
            measurePerformanceOf(implC, statsC)
            measurePerformanceOf(implD, statsD)
        }

        def executeForContainerWithStatsPrettyPrinting(container: HistoryContainer, testDescription: String) {
            println(s"starting benchmark for: $testDescription")
            println(s"number of operations = $numberOfOperations")
            val results = new Stats
            measurePerformanceOf(container, results)
            println("results:")
            println(s"    total time [seconds] = ${results.totalSeconds}")
            println(s"    number of entries in container when test finished: ${container.size}")
            printStatsForHuman("insert", results.insert)
            printStatsForHuman("update", results.update)
            printStatsForHuman("clear from", results.clearFrom)
            printStatsForHuman("query", results.query)
            println()
        }

        private def printStatsForHuman(operationName: String, stats: OperationStats) {
            println(s"    $operationName: invocations = ${stats.numberOfInvocations} averageTime [micro-seconds] = ${stats.averageTimeNanos}")
        }

        private def exportStatsForAnalysis(implTag: String, stats: Stats) {
            print(implTag)
            print(exportDataSeparator)

            print(Math.log10(timepointsRange))
            print(exportDataSeparator)

            if (deletesEnabled)
                print(Math.log10(deleteOperationFrequencyReciprocal))
            print(exportDataSeparator)

            print(f"${stats.totalSeconds}%1.5f")
            print(exportDataSeparator)

            print(f"${stats.query.averageTimeNanos}%1.5f")
            print(exportDataSeparator)

            print(f"${stats.insert.averageTimeNanos}%1.5f")
            print(exportDataSeparator)

            print(f"${stats.update.averageTimeNanos}%1.5f")
            print(exportDataSeparator)

            print(f"${stats.clearFrom.averageTimeNanos}%1.5f")
            println()
        }

        private def measurePerformanceOf(container: HistoryContainer, stats: Stats) {
            System.gc()
            val q = queriesToInsertsRatio / (1 + queriesToInsertsRatio)
            val random = new Random(randomSeed)
            def nextTimepoint: Timepoint = Timepoint(random.nextInt(timepointsRange).toLong)
            def nextData: Data = Data(random.nextInt(1000).toString)

            val beginTime = System.currentTimeMillis()
            
            for (n <- 1 to numberOfOperations) {
                if (deletesEnabled && random.nextInt(deleteOperationFrequencyReciprocal) == 0) {
                    val (time, result) = measureExecutionTimeOf(container.clearAllFrom(nextTimepoint))
                    stats.clearFrom.addInvocation(time, container.size)
                } else {
                    if (random.nextDouble() < q) {
                        val (time, result) = measureExecutionTimeOf(container.query(nextTimepoint))
                        stats.query.addInvocation(time, container.size)
                    } else {
                        val newEntry = HistoryEntry(nextTimepoint, nextData)
                        val (time, result) = measureExecutionTimeOf(container.insertOrOverrideEntry(newEntry))
                        if (result.isDefined && result.get.time == newEntry.time)
                            stats.update.addInvocation(time, container.size)
                        else
                            stats.insert.addInvocation(time, container.size)
                    }
                }
            }
            
            val finishTime = System.currentTimeMillis()
            stats.totalSeconds = (finishTime - beginTime).toDouble / 1000
        }

        private def measureExecutionTimeOf[T](block: => T): (Long, T) = {
            val start = System.nanoTime()
            val result = block
            return (System.nanoTime() - start, result)
        }

    }

}
