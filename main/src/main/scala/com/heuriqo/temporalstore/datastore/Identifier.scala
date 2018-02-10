package com.heuriqo.temporalstore.datastore

/**
 * Represents the entity we store history for.
 * @param value
 */
case class Identifier(value: Int) extends AnyVal {
    override def toString: String = value.toString
}
