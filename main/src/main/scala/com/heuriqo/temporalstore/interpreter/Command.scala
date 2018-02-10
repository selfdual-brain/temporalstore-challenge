package com.heuriqo.temporalstore.interpreter

import com.heuriqo.temporalstore.datastore._

/**
 * Hierarchy of  commands.
 */
sealed abstract class Command {

    /**
     * Executes a command against given datastore.
     *
     * @param db datastore
     * @return Left value contains error description, Right value contains success descrption
     */
    def executeFor(db: TemporalDatastore): Either[String, String] = {
        try {
            this.privateExecuteFor(db)
        } catch {
            case ex: IdentifierAlreadyRegisteredException => Left(s"A history already exists for identifier ${ex.id}")
            case ex: UnknownIdentifierException => Left(s"No history exists for identifier ${ex.id}")
        }
    }

    /** Implemented by subclasses to provide semantics of command execution. */
    protected def privateExecuteFor(db: TemporalDatastore): Either[String, String]

}

case class CreateCommand(id: Identifier, time: Timepoint, data: Data) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.create(id, HistoryEntry(time, data))
        Right(data.toString)
    }
}

case class UpdateCommand(id: Identifier, time: Timepoint, data: Data) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.update(id, HistoryEntry(time, data)) match {
            case Some(previousEntrySeenForThisTimepoint) => Right(previousEntrySeenForThisTimepoint.data.toString)
            case None => Right("") //we use empty string here as there is no value to show (previously no value was defined at this timepoint)
        }
    }
}

case class DeleteAllCommand(id: Identifier) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.deleteHistoryFor(id) match {
            case Some(historyEntry) => Right(historyEntry.data.toString)
            case None => Right("") //we use empty string here as there is no value to show (the history is empty)
        }
    }
}

case class DeleteFromCommand(id: Identifier, time: Timepoint) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.deleteFromTimepoint(id, time) match {
            case Some(historyEntry) => Right(historyEntry.data.toString)
            case None => Right("") //we use empty string here as there is no value to show (the history is empty)
        }
    }
}

case class GetCommand(id: Identifier, time: Timepoint) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.get(id, time) match {
            case Some(historyEntry) => Right(historyEntry.data.toString)
            case None => Left("value is not defined for this timepoint")
        }
    }
}

case class LatestCommand(id: Identifier) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        db.getLatest(id) match {
            case Some(historyEntry) => Right(historyEntry.time.toString + " " + historyEntry.data.toString)
            case None => Left("history is empty")
        }
    }
}

case class ListCommand(id: Identifier) extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = Right(db.getHistory(id).mkString(","))
}

case object QuitCommand extends Command {
    override def privateExecuteFor(db: TemporalDatastore): Either[String, String] = {
        System.exit(0)
        throw new RuntimeException("line unreachable by design")
    }
}
