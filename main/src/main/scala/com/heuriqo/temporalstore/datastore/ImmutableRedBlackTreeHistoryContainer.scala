package com.heuriqo.temporalstore.datastore

import scala.collection._
import scala.util.{Failure, Success, Try}

/**
 * Implementation of HistoryContainer based on scala.collection.immutable.TreeMap.
 * This is immutable red-black tree implementation built into Scala collections library.
 */
class ImmutableRedBlackTreeHistoryContainer extends HistoryContainer {
    private var entries: SortedMap[Timepoint, Data] = createEmptyMap()

    override def insertOrOverrideEntry(newEntry: HistoryEntry): Option[HistoryEntry] = {
        if (newEntry.time.value < 0)
            throw new Exception(s"Illegal negative timepoint while inserting: $newEntry.time.value")

        val previousValueSeenForThisTimepoint: Option[HistoryEntry] = Try {entries.to(newEntry.time).last} match {
            case Success((timepoint, data)) => Some(HistoryEntry(timepoint, data))
            case Failure(ex) => None
        }

        entries += (newEntry.time -> newEntry.data)
        return previousValueSeenForThisTimepoint
    }

    override def getAllEntries: Traversable[HistoryEntry] = entries map transformTupleIntoHistoryEntry

    override def size: Int = entries.size

    override def getFirstEntry: Option[HistoryEntry] =
        Try{entries.head} match {
            case Success(entry) => Some(transformTupleIntoHistoryEntry(entry))
            case Failure(ex) => None
        }

    override def getLastEntry: Option[HistoryEntry] = entries.lastOption map transformTupleIntoHistoryEntry

    override def clearAllFrom(time: Timepoint) {
        entries = entries.to(time)
        entries = entries - time
    }

    override def clearAll() {
        entries = createEmptyMap()
    }

    override def query(time: Timepoint): Option[HistoryEntry] = entries.to(time).lastOption map transformTupleIntoHistoryEntry

    private def transformTupleIntoHistoryEntry(pair: (Timepoint, Data)): HistoryEntry = HistoryEntry(pair._1, pair._2)

    private def createEmptyMap(): SortedMap[Timepoint, Data] = new immutable.TreeMap[Timepoint, Data]
}
