package com.heuriqo.temporalstore.interpreter

import com.heuriqo.temporalstore.datastore.{Data, Identifier, Timepoint}

import scala.util.{Failure, Success, Try}

/**
 * Decodes string commands, discovers syntax errors.
 *
 */
class CommandsParser {

    /**
     * If command syntax is ok, creates internal representation of a command.
     * Otherwise - provides syntax error description.
     *
     * @param input command in string form
     * @return Command instance or syntax error description
     */
    def parse(input: String): Either[String, Command] = {
        val trimmedInput = input.trim
        if (trimmedInput.isEmpty)
            return Left("empty input")
        else {
            val tokens: Array[String] = trimmedInput.split("\\s+")

            tokens(0).toUpperCase match { //we convert command keyword to uppercase to achieve some additional flexibility - this is quite useful in interactive mode
                case "CREATE" =>
                    if (tokens.length == 4)
                        this.parseCreate(id = tokens(1), timestamp = tokens(2), data = tokens(3))
                    else
                        signalWrongNumberOfCommandArgs("CREATE", expected = "3", tokens)

                case "UPDATE" =>
                    if (tokens.length == 4)
                        this.parseUpdate(id = tokens(1), timestamp = tokens(2), data = tokens(3))
                    else
                        signalWrongNumberOfCommandArgs("UPDATE", expected = "3", tokens)

                case "DELETE" =>
                    tokens.length match {
                        case 2 => this.parseDeleteAll(id = tokens(1))
                        case 3 => this.parseDeleteFrom(id = tokens(1), timepoint = tokens(2))
                        case other => signalWrongNumberOfCommandArgs("DELETE", expected = "1 or 2", tokens)
                    }

                case "GET" =>
                    if (tokens.length == 3)
                        this.parseGet(id = tokens(1), timepoint = tokens(2))
                    else
                        signalWrongNumberOfCommandArgs("GET", expected = "2", tokens)

                case "LATEST" =>
                    if (tokens.length == 2)
                        this.parseLatest(id = tokens(1))
                    else
                        signalWrongNumberOfCommandArgs("LATEST", expected = "1", tokens)

                //this command was not included in the original requirements but I added id as was extremely useful for testing in interactive mode
                case "LIST" =>
                    if (tokens.length == 2)
                        this.parseList(id = tokens(1))
                    else
                        signalWrongNumberOfCommandArgs("LATEST", expected = "1", tokens)

                case "QUIT" =>
                    if (tokens.length == 1)
                        this.parseQuit()
                    else
                        signalWrongNumberOfCommandArgs("QUIT", expected = "no arguments", tokens)

                case other =>
                    Left(s"unknown command: $other")
            }
        }
    }

    private def signalWrongNumberOfCommandArgs(commandName: String, expected: String, tokens: Array[String]): Either[String, Command] =
        Left(s"wrong number of arguments for command $commandName, expected $expected, found ${tokens.length - 1}")

    private def withParsingExceptionsConvertedToSyntaxErrorString(block: => Either[String, Command]): Either[String, Command] = {
        try {
            block
        } catch {
            case ex: ParsingException => Left(ex.getMessage)
        }
    }

    private def parseCreate(id: String, timestamp: String, data: String): Either[String, Command] =
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(CreateCommand(parseId(id), parseTimepoint(timestamp), Data(data)))
        }

    private def parseUpdate(id: String, timestamp: String, data: String): Either[String, Command] =
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(UpdateCommand(parseId(id), parseTimepoint(timestamp), Data(data)))
        }

    private def parseDeleteAll(id: String): Either[String, Command] =
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(DeleteAllCommand(parseId(id)))
        }

    private def parseDeleteFrom(id: String, timepoint: String): Either[String, Command] =
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(DeleteFromCommand(parseId(id), parseTimepoint(timepoint)))
        }

    private def parseGet(id: String, timepoint: String): Either[String, Command] = {
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(GetCommand(parseId(id), parseTimepoint(timepoint)))
        }
    }

    private def parseList(id: String): Either[String, Command] = {
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(ListCommand(parseId(id)))
        }
    }

    private def parseLatest(id: String): Either[String, Command] = {
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(LatestCommand(parseId(id)))
        }
    }

    private def parseQuit(): Either[String, Command] = {
        withParsingExceptionsConvertedToSyntaxErrorString {
            Right(QuitCommand)
        }
    }

    @throws[ParsingException]
    private def parseId(s: String): Identifier = {
        Try {
            s.toInt
        } match {
            case Success(n) =>
                if (n >= 0)
                    Identifier(n)
                else
                    throw new ParsingException(s, "wrong identifier - negative value not allowed")

            case Failure(ex) =>
                throw new ParsingException(s, ex)
        }
    }

    @throws[ParsingException]
    private def parseTimepoint(s: String): Timepoint = {
        Try {
            s.toLong
        } match {
            case Success(n) =>
                if (n >= 0)
                    Timepoint(n)
                else
                    throw new ParsingException(s, "wrong timepoint - negative value not allowed")

            case Failure(ex) =>
                throw new ParsingException(s, ex)
        }
    }
}

class ParsingException(val parsedString: String, val msg: String) extends Exception {

    def this(parsedString: String, nestedException: Throwable) {
        this(parsedString, "integer parsing failed")
        this.initCause(nestedException)
    }

    override def getMessage: String = msg + ", input value was: " + parsedString

}
