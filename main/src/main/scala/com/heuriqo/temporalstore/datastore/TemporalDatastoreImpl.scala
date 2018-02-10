package com.heuriqo.temporalstore.datastore

import scala.collection._

/**
 * An implementation of temporal datastore implemented using: <ol>
 *     <li>a large main hash map: identifiers -----> history containers</li>
 *     <li>array buffers as history containers; a buffers keeps history entries dorted by time (and all operations inside a container are based on binary rearch)</li>
 * </ol>
 *
 * @param expectedNumberOfIdentifiers main hash table size will be initialized with this value
 * @param createNewHistoryContainer a factory for creating history containers (a separate history container is created for every registered identifier)
 */
class TemporalDatastoreImpl(expectedNumberOfIdentifiers: Int)(createNewHistoryContainer: => HistoryContainer) extends TemporalDatastore {
    private val id2history: BigHashMap[Identifier, HistoryContainer] = new BigHashMap[Identifier, HistoryContainer](expectedNumberOfIdentifiers)

    override def create(id: Identifier, entry: HistoryEntry): Unit = {
        if (id2history.contains(id))
            throw new IdentifierAlreadyRegisteredException(id)

        val newHstoryContainer: HistoryContainer = createNewHistoryContainer
        newHstoryContainer.insertOrOverrideEntry(entry)
        id2history.put(id, newHstoryContainer)
    }

    override def deleteFromTimepoint(id: Identifier, time: Timepoint): Option[HistoryEntry] = {
        val history = getHistoryFor(id)
        history.clearAllFrom(time)
        return history.getLastEntry
    }

    override def update(id: Identifier, entry: HistoryEntry): Option[HistoryEntry] = this.getHistoryFor(id).insertOrOverrideEntry(entry)

    override def deleteHistoryFor(id: Identifier): Option[HistoryEntry] = {
        val history = getHistoryFor(id)
        val result = history.getLastEntry
        history.clearAll()
        return result
    }

    override def get(id: Identifier, time: Timepoint): Option[HistoryEntry] = this.getHistoryFor(id).query(time)

    override def getLatest(id: Identifier): Option[HistoryEntry] = this.getHistoryFor(id).getLastEntry

    override def getHistory(id: Identifier): scala.Traversable[HistoryEntry] = this.getHistoryFor(id).getAllEntries

    private def getHistoryFor(id: Identifier): HistoryContainer = {
        id2history.get(id) match {
            case Some(history) => history
            case None => throw new UnknownIdentifierException(id)
        }
    }

}

/**
 * Slightly hacked version of HashMap that exposes initial size of the underlaying array.
 */
private class BigHashMap[A, B](tableSizeOnCreation: Int) extends mutable.HashMap[A, B] {
    override def initialSize: Int = tableSizeOnCreation
}
