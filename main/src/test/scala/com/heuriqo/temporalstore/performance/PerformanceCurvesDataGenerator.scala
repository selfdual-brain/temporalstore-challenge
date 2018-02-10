package com.heuriqo.temporalstore.performance

import java.io.{PrintWriter, File}

object PerformanceCurvesDataGenerator extends AbstractContainerBenchmark {

    def main(args: Array[String]) {
        if (args.isEmpty)
            throw new Exception("Missing required parameter: test results filesystem folder path")

        val outputDir = new File(args(0))
        if (! outputDir.exists())
            throw new Exception("Output directory not found: " + outputDir)
        if (! outputDir.isDirectory)
            throw new Exception("Output path points to a file (should be a directory!!): " + outputDir)

        curveInterpolationTest(outputDir)
    }

    def exportCurveToFile(operationStats: OperationStats, file: File) {
        val writer = new PrintWriter(file)
        try {
            for ((k,ic) <- operationStats.timesMap.toArray.sortBy(t => t._1)) {
                writer.print(math.pow(10.0, k.toDouble / 100))
                writer.print(exportDataSeparator)
                writer.print(ic.invocations)
                writer.print(exportDataSeparator)
                writer.print(ic.totalNanoTime.toDouble / ic.invocations)
                writer.println()
            }
        } finally {
            writer.close()
        }
    }

    def curveInterpolationTest(outputDir: File) {
        val statsA = new Stats(collectCurveData = true)
        val statsB = new Stats(collectCurveData = true)
        val statsC = new Stats(collectCurveData = true)
        val statsD = new Stats(collectCurveData = true)

        for (
            tr <- 2 to 6;
            del <- 1 to 8
        ) {
            val experiment = new Experiment(
                deletesEnabled = true,
                deleteOperationFrequencyReciprocal = exp10(del),
                queriesToInsertsRatio = 2,
                numberOfOperations = exp10(6) * 2,
                timepointsRange = exp10(tr)
            )

            println(s"measuring for tr=$tr, del=$del")
            experiment.executeWithCumulativeStats(statsA, statsB, statsC, statsD)
        }

        exportAllCurves(statsA, outputDir, "A")
        exportAllCurves(statsB, outputDir, "B")
        exportAllCurves(statsC, outputDir, "C")
        exportAllCurves(statsD, outputDir, "D")
    }

    private def exportAllCurves(stats: Stats, outputDir: File, algorithmCode: String) {
        exportCurveToFile(stats.query, new File(outputDir, s"curve-$algorithmCode-query.txt"))
        exportCurveToFile(stats.insert, new File(outputDir, s"curve-$algorithmCode-insert.txt"))
        exportCurveToFile(stats.update, new File(outputDir, s"curve-$algorithmCode-update.txt"))
        exportCurveToFile(stats.clearFrom, new File(outputDir, s"curve-$algorithmCode-clear.txt"))
    }


}
