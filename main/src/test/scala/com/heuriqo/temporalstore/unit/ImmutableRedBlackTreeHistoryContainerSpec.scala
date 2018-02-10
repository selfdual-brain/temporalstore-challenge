package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore.{HistoryContainer, ImmutableRedBlackTreeHistoryContainer}

class ImmutableRedBlackTreeHistoryContainerSpec extends HistoryContainerSpec {
    override def createHistoryContainer: HistoryContainer = new ImmutableRedBlackTreeHistoryContainer
}
