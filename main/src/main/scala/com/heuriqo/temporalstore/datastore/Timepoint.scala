package com.heuriqo.temporalstore.datastore

/**
 * Represents the timepoints.
 * @param value
 */
case class Timepoint(value: Long) extends Ordered[Timepoint] {
    override def compare(that: Timepoint): Int = value.compareTo(that.value)

    override def toString: String = value.toString
}
