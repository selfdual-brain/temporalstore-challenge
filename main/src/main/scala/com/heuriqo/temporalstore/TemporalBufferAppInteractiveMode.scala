package com.heuriqo.temporalstore

import java.io.{PrintStream, BufferedReader}

import com.heuriqo.temporalstore.interpreter.CommandsProcessor

object TemporalBufferAppInteractiveMode extends AbstractStartup {

    def runCommandsProcessingLoop(input: BufferedReader, output: PrintStream, commandsProcessor: CommandsProcessor) {
        //commands processing loop
        while (true) {
            print("> ")
            val command = input.readLine()
            val commandResult = commandsProcessor.execute(command)
            output.println(commandResult)
        }
    }

}
