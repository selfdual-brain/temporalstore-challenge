package com.heuriqo.temporalstore

import scala.util.{Failure, Success, Try}

/**
 * Encapsulation of command-line arguments processing
 */
object CommandLineParser {
    private val requiredNumberOfCommandLineArgs = 2

    private val namesOfArgs = Array(
        "initial size of identifiers hash table",
        "history container implementation to use"
    )

    def isNumberOfCommandLineArgumentsCorrect(n: Int): Boolean = n == requiredNumberOfCommandLineArgs

    def parseCommandLineArgs(args: Array[String]): Either[CommandLineArgError, Config] = {
        for {
            identifiersCollectionInitialSize <- parseArg0(args(0)).right
            historyInitialSize <- parseArg1(args(1)).right
        }
        yield Config(identifiersCollectionInitialSize, historyInitialSize)
    }

    private def parseArg0(s: String): Either[CommandLineArgError, Int] = {
        parseAsPositiveInteger(s) match {
            case Some(n) => Right(n)
            case None => Left(CommandLineArgError(0, namesOfArgs(0), s, "expected integer value from range [1 ..2147483647]"))
        }
    }

    private def parseArg1(s: String): Either[CommandLineArgError, String] = {
        s match {
            case "binary-search" => Right(s)
            case "java-treemap" => Right(s)
            case "scala-treemap" => Right(s)
            case other => Left(CommandLineArgError(1, namesOfArgs(1), s, "expected one of keywords: binary-search, java-treemap, scala-treemap"))
        }
    }

    private def parseAsPositiveInteger(s: String): Option[Int] = {
        Try{s.toInt} match {
            case Success(n) => if (n > 0) Some(n) else None
            case Failure(ex) => None
        }
    }

}

case class Config(initialIdentifiersHashtableSize: Int, historyContainerImplementation: String)

case class CommandLineArgError(argPosition: Int, argName: String, actualValue: String, errorDesc: String)

