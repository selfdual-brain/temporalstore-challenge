package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore.{ArrayBufferHistoryContainer, HistoryContainer}

class ArrayBufferHistoryContainerSpec extends HistoryContainerSpec {
    override def createHistoryContainer: HistoryContainer = new ArrayBufferHistoryContainer(10)
}
