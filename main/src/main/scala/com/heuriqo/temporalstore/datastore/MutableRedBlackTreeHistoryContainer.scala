package com.heuriqo.temporalstore.datastore

import java.util
import java.util.Comparator

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

/**
 *  Implementation of HistoryContainer based on java.util.TreeMap.
 *  This is mutable red-black tree implementation built into JDK.
 *  We are using java implementation of TreeMap because as of Scala version 7 there is no native Scala implementation
 * of mutable TreeMap included in the base library.
 *
 * Remark: Java 6 introduced very useful interface NavigableMap that makes our implementation almost trivial.
 */
class MutableRedBlackTreeHistoryContainer extends HistoryContainer {

    //we have to define explicit comparator for timepoints because Timepoint is naturally-ordered using the Scala way
    //and there is no automatic java-scala interoperability regarding Ordered/Comparable
    private val comparator: Comparator[Timepoint] = new Comparator[Timepoint] {
        override def compare(left: Timepoint, right: Timepoint): Int = left.compare(right)
    }

    //here the magic happens; java version of TreeMap impelements NavigableMap
    private val entries: util.NavigableMap[Timepoint, Data] = new util.TreeMap[Timepoint, Data](comparator)

    override def query(time: Timepoint): Option[HistoryEntry] = Option(entries.floorEntry(time)) map transformMapEntryIntoHistoryEntry

    override def insertOrOverrideEntry(newEntry: HistoryEntry): Option[HistoryEntry] = {
        if (newEntry.time.value < 0)
            throw new Exception(s"Illegal negative timepoint while inserting: $newEntry.time.value")

        val previouslySeenValue = this.query(newEntry.time)
        entries.put(newEntry.time, newEntry.data)
        return previouslySeenValue
    }

    override def getAllEntries: Traversable[HistoryEntry] = entries.entrySet().map(e => HistoryEntry(e.getKey, e.getValue))

    override def size: Int = entries.size()

    override def getFirstEntry: Option[HistoryEntry] = Option(entries.firstEntry()) map transformMapEntryIntoHistoryEntry

    override def getLastEntry: Option[HistoryEntry] = Option(entries.lastEntry()) map transformMapEntryIntoHistoryEntry

    override def clearAllFrom(time: Timepoint): Unit = entries.tailMap(time).clear()

    override def clearAll(): Unit = entries.clear()

    private def transformMapEntryIntoHistoryEntry(pair: java.util.Map.Entry[Timepoint, Data]): HistoryEntry = HistoryEntry(pair.getKey, pair.getValue)

}
