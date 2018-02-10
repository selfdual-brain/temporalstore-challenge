package com.heuriqo.temporalstore

import java.io.{PrintStream, BufferedReader}

import com.heuriqo.temporalstore.datastore._
import com.heuriqo.temporalstore.interpreter.{CommandsParser, CommandsProcessor}

abstract class AbstractStartup {

    def main(args: Array[String]) {
        if (! CommandLineParser.isNumberOfCommandLineArgumentsCorrect(args.length))
            printUsageInfo()
        else
            CommandLineParser.parseCommandLineArgs(args) match {
                case Left(parsingFailure) =>
                    println("erminating because command-line parameters were invalid")
                    println(s"    argument position: ${parsingFailure.argPosition}")
                    println(s"    argument explanation: ${parsingFailure.argName}")
                    println(s"    argument actual value: ${parsingFailure.actualValue}")
                    println(s"    what was wrong: ${parsingFailure.errorDesc}")
                case Right(config) =>
                    runUsingConfig(config)
            }
    }

    def runUsingConfig(config: Config) {
        //setting up components and wiring
        val datastore: TemporalDatastore = new TemporalDatastoreImpl(config.initialIdentifiersHashtableSize) (
            config.historyContainerImplementation match {
                case "binary-search" => new ArrayBufferHistoryContainer(10)
                case "java-treemap" => new MutableRedBlackTreeHistoryContainer
                case "scala-treemap" => new ImmutableRedBlackTreeHistoryContainer
            }
        )

        val commandsParser = new CommandsParser
        val commandsProcessor = new CommandsProcessor(datastore, commandsParser)
        val input = Console.in
        val output = Console.out

        runCommandsProcessingLoop(input, output, commandsProcessor)
    }

    def runCommandsProcessingLoop(input: BufferedReader, output: PrintStream, commandsProcessor: CommandsProcessor)

    private def printUsageInfo() {
        println("missing mandatory command-line parameters: <initial size of identifiers collection> <history container implementation to use>")
        println("explanation:")
        println("    <initial size of identifiers collection> - sets initial size for main map of identifiers")
        println("    <history container implementation to use> - for each identifier a new container is created, there are 3 implementation available: binary-search, java-treemap, scala-treemap")
        println("example: ")
        println("> scala com.heuriqo.temporalstore.TemporalBufferApp 1000 java-treemap")
        println("... this will preallocate main hash table for 1000 identifiers and will use java-treemap implementation for all history containers")
    }

}


