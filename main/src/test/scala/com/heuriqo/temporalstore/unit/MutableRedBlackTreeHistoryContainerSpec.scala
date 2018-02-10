package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore.{HistoryContainer, MutableRedBlackTreeHistoryContainer}

class MutableRedBlackTreeHistoryContainerSpec extends HistoryContainerSpec {
    override def createHistoryContainer: HistoryContainer = new MutableRedBlackTreeHistoryContainer
}
