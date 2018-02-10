package com.heuriqo.temporalstore.datastore

/**
 * Wrapper for (timepoint, payload) pair).
 *
 * @param time
 * @param data
 */
case class HistoryEntry(time: Timepoint, data: Data) extends Ordered[HistoryEntry] {
    override def compare(that: HistoryEntry): Int = time.compare(that.time)

    override def toString: String = time.toString +"->" + data.toString
}
