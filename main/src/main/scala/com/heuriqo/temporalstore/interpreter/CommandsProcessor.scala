package com.heuriqo.temporalstore.interpreter

import com.heuriqo.temporalstore.datastore.TemporalDatastore

/**
 * Implements top-level API for executing comands.
 *
 * @param db temporal datastore to be used
 * @param parser parser to be used
 */
class CommandsProcessor(db: TemporalDatastore, parser: CommandsParser) {

    /**
     * Does analysis of given string command.
     * If the command is correct, tries to execute this command.
     * Commands results, syntax errors and datastore errors are reported as strings.
     *
     * @param commandAsString command to be executed
     * @return result of executing this command (or error description)
     */
    def execute(commandAsString: String): String = {
        parser.parse(commandAsString) match {
            case Left(syntaxError) => "ERR syntax error: " + syntaxError
            case Right(command) =>
                command.executeFor(db) match {
                    case Left(error) => "ERR " + error
                    case Right(executionResult) => "OK " + executionResult
                }
        }

    }

}
