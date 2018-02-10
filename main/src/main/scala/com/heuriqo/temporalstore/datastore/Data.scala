package com.heuriqo.temporalstore.datastore

/**
 * Data payload wrapper.
 *
 * @param value
 */
case class Data(value: String) extends AnyVal {
    override def toString: String = value.toString
}
