package com.heuriqo.temporalstore.performance

object BatchProcessingSimulation extends AbstractContainerBenchmark {

    def main(args: Array[String]) {
        for (
            tr <- 3 to 5;
            del <- 1 to 6
        ) {
            val experiment = new Experiment(
                deletesEnabled = true,
                deleteOperationFrequencyReciprocal = exp10(del),
                queriesToInsertsRatio = 2,
                numberOfOperations = exp10(7),
                timepointsRange = exp10(tr)
            )

            experiment.execute()
        }
    }

}
