package com.heuriqo.temporalstore.datastore

import scala.collection.Traversable

/**
 * API of collections we use to store history entries.
 */
trait HistoryContainer {

    /**
     * If the entry with given timepoint already exists in the history - overrides this entry (and returns the overriden entry).
     * Otherwise - inserts new entry, keeping the chronological sorting of entries (and returns closest entry towards the left end of timeline, or None if there is no entry on the left) .
     *
     * @param newEntry entry to be added
     * @return previous entry seen for this timepoint (= the entry that would be returned by query method before the insertOrOverrideEntry was invoked)
     */
    def insertOrOverrideEntry(newEntry: HistoryEntry): Option[HistoryEntry]

    /**
     * Gets the entry for given timepoint.
     * If there is no such entry - returns closest entry towards the left end of timeline, or None if there is no entry on the left.
     *
     * @param time timepoint we query about
     * @return history entry applicable for this timepoint
     */
    def query(time: Timepoint): Option[HistoryEntry]

    /**
     * Returns first entry  (= the entry placed on the ledt end of the timeline).
     * Returns None if the history is empty.
     */
    def getFirstEntry: Option[HistoryEntry]

    /**
     * Returns latest entry (= the entry placed on the right end of the timeline).
     * Returns None if the history is empty.
     */
    def getLastEntry: Option[HistoryEntry]

    /**
     * Returns the collection of all history entries (sorted by time).
     */
    def getAllEntries: Traversable[HistoryEntry]

    /**
     * Returns the number of history entries.
     */
    def size: Int

    /**
     * Removes all entries from the history.
     */
    def clearAll()

    /**
     * Removes all entries that have timepoint equal or higher than given timepoint.
     */
    def clearAllFrom(time: Timepoint)

}
