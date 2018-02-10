package com.heuriqo.temporalstore.datastore

import scala.collection.Searching._
import scala.collection._

/**
 * Implementation of HistoryContainer based on ArrayBuffer (+ binary search).
 *
 * Internally we keep a mutable sequence of history entries ordered by time.
 * Caution: this container grows as needed (= when new items are added), but it never shrinks; it was deliberately made decision to keep it this way for simplicity.
 * The crucial idea here is to use binary search. As Scala (since version 2.11) has built-in support for binary search, we do not have to implemt is from scratch.
 *
 * @param initialSize initial size of the buffer we keep entries in; any positive int value is ok; this does not influence the semantics,
 *                    only the performance (bigger initial size = less internal resizings operations to happen)
 */
class ArrayBufferHistoryContainer(initialSize: Int) extends HistoryContainer {

    private val entries = new mutable.ArrayBuffer[HistoryEntry](initialSize)

    def insertOrOverrideEntry(newEntry: HistoryEntry): Option[HistoryEntry] = {
        if (newEntry.time.value < 0)
            throw new Exception(s"Illegal negative timepoint while inserting: $newEntry.time.value")
        entries.search(newEntry) match {
            //binary search happens here
            case Found(index) =>
                val old = entries(index)
                entries(index) = newEntry
                Some(old)

            case InsertionPoint(index) =>
                entries.insert(index, newEntry)
                if (index == 0)
                    None
                else
                    Some(entries(index - 1))
        }
    }

    def query(time: Timepoint): Option[HistoryEntry] =
        entries.search(HistoryEntry(time, Data("?"))) match { //binary search happens here
            case Found(index) => Some(entries(index))
            case InsertionPoint(index) =>
                if (index == 0)
                    None
                else
                    Some(entries(index - 1))
        }

    def getFirstEntry: Option[HistoryEntry] = if (entries.isEmpty) None else Some(entries(0))

    def getLastEntry: Option[HistoryEntry] = entries.lastOption

    def getAllEntries: Traversable[HistoryEntry] = entries

    def size = entries.size

    def clearAll() {
        entries.clear()
    }

    def clearAllFrom(time: Timepoint) {
        entries.search(HistoryEntry(time, Data("?"))) match { //binary search happens here
            case Found(index) => entries.remove(index, entries.size - index)
            case InsertionPoint(index) =>
                if (index == 0)
                    this.clearAll()
                else
                    if (index < entries.size)
                        entries.remove(index, entries.size - index)
        }
    }
}
