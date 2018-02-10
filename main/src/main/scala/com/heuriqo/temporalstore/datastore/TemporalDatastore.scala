package com.heuriqo.temporalstore.datastore

/**
 * Internal API for a temporal datastore.
 */
trait TemporalDatastore {

    /**
      * Creates new history and initializes it with first entry.
      *
      * @param id identifier
      * @param entry first entry
      */
     @throws[IdentifierAlreadyRegisteredException]
     def create(id: Identifier, entry: HistoryEntry)

     /**
      * Inserts/overrider an entry in the history.
      *
      * @param id identifier for which the history will be changed
      * @param entry new entry
      * @return previous entry as seen for this timestamp (if the new entry will be first entry, returns None)
      */
     @throws[UnknownIdentifierException]
     def update(id: Identifier, entry: HistoryEntry): Option[HistoryEntry]

     /**
      * Deletes history for given identifier.
      * The identifier stays registered.
      *
      * @param id identifier for which the history will be deleted
      * @return latest entry (if present)
      */
     @throws[UnknownIdentifierException]
     def deleteHistoryFor(id: Identifier): Option[HistoryEntry]

     /**
      * Deletes history for given identifier starting from provided timepoint up.
      *
      * @param id identifier for which the history will be deleted
      * @param time timepoint
      * @return latest entry (after deletion is completed)
      */
    @throws[UnknownIdentifierException]
     def deleteFromTimepoint(id: Identifier, time: Timepoint): Option[HistoryEntry]

    /**
     * Returns the data from the history for given identifier as seen for provided timestamp.
     *
     * @param id identifier for which the history will be queried
     * @param time timepoint
     * @return last entry as seen for the provided timepoint or None if there is no entry in scope
     */
    @throws[UnknownIdentifierException]
    def get(id: Identifier, time: Timepoint): Option[HistoryEntry]

    /**
     *  Returns latest entry from the history for given identifier.
     *
     * @param id identifier for which the history will be queried
     * @return latest entry from the history (or None if the history is empty)
     */
    @throws[UnknownIdentifierException]
    def getLatest(id: Identifier): Option[HistoryEntry]

    /**
     * Returns collection of all history entries for given identifier.
     *
     * @param id identifier for which the history will be queried
     * @throws sequence of history entries ordered by time
     */
    @throws[UnknownIdentifierException]
    def getHistory(id: Identifier): Traversable[HistoryEntry]

}
