package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore._

abstract class HistoryContainerSpec extends BaseSpec {
    val entry1 = HistoryEntry(Timepoint(10), Data("x"))
    val entry2 = HistoryEntry(Timepoint(20), Data("y"))
    val entry3 = HistoryEntry(Timepoint(30), Data("z"))

    val duplicatedValueEntry = HistoryEntry(Timepoint(31), Data("y"))

    val entry1ovr = HistoryEntry(Timepoint(10), Data("a"))
    val entry2ovr = HistoryEntry(Timepoint(20), Data("a"))
    val entry3ovr = HistoryEntry(Timepoint(30), Data("a"))

    def createHistoryContainer: HistoryContainer

    trait CommonSetup {
        val container: HistoryContainer = createHistoryContainer
    }

    "History container" must "be empty after creation" in new CommonSetup {
        container.getAllEntries.size shouldBe 0
        container.size shouldBe 0
    }

    it must "correctly sort entries" in new CommonSetup {
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry1)
        container.getAllEntries.toArray shouldBe Array(entry1, entry2, entry3)
    }

    it must "be empty after removing entries one-by-one" in new CommonSetup {
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry1)
        container.clearAllFrom(Timepoint(30))
        container.clearAllFrom(Timepoint(20))
        container.clearAllFrom(Timepoint(10))
        container.getAllEntries.size shouldBe 0
        container.size shouldBe 0
    }

    it must "be empty after removing all entries" in new CommonSetup {
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry1)
        container.clearAll()
        container.getAllEntries.size shouldBe 0
        container.size shouldBe 0
    }

    it must "correctly handle entry insert in the beginning of history"  in new CommonSetup {
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry1) shouldBe None
    }

    it must "correctly handle entry insert in the middle of history"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry2) shouldBe Some(entry1)
    }

    it must "correctly handle entry insert in the end of history"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3) shouldBe Some(entry2)
    }

    it must "correctly handle first entry override"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry1ovr) shouldBe Some(entry1)
    }

    it must "correctly handle middle entry override"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry2ovr) shouldBe Some(entry2)
    }

    it must "correctly handle last entry override"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(entry3ovr) shouldBe Some(entry3)
    }

    it must "correctly handle queries"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)

        container.query(Timepoint(5)) shouldBe None
        container.query(Timepoint(10)) shouldBe Some(entry1)
        container.query(Timepoint(15)) shouldBe Some(entry1)
        container.query(Timepoint(30)) shouldBe Some(entry3)
        container.query(Timepoint(31)) shouldBe Some(entry3)
        container.getFirstEntry shouldBe Some(entry1)
        container.getLastEntry shouldBe Some(entry3)
        container.getAllEntries.toArray shouldBe Array(entry1, entry2, entry3)
    }

    it must "correctly handle removing all entries after selected timepoint (case 1)"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.clearAllFrom(Timepoint(5))
        container.size shouldBe 0
    }

    it must "correctly handle removing all entries after selected timepoint (case 2)"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.clearAllFrom(Timepoint(15))
        container.getLastEntry shouldBe Some(entry1)
    }

    it must "correctly handle removing all entries after selected timepoint (case 3)"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.clearAllFrom(Timepoint(30))
        container.getLastEntry shouldBe Some(entry2)
    }

    it must "correctly handle removing all entries after selected timepoint (case 4)"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.clearAllFrom(Timepoint(31))
        container.getLastEntry shouldBe Some(entry3)
    }

    it must "accept values duplication when inserting"  in new CommonSetup {
        container.insertOrOverrideEntry(entry1)
        container.insertOrOverrideEntry(entry2)
        container.insertOrOverrideEntry(entry3)
        container.insertOrOverrideEntry(duplicatedValueEntry)
        container.query(Timepoint(20)) shouldBe Some(entry2)
        container.query(Timepoint(32)) shouldBe Some(duplicatedValueEntry)
    }

    it must "accept whole range of timepoints"  in new CommonSetup {
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(0), Data("a")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(Int.MaxValue), Data("a")))
    }

    it must "refuse negative timepoints" in new CommonSetup {
        intercept[Exception] {
            container.insertOrOverrideEntry(HistoryEntry(Timepoint(-1), Data("a")))
        }
    }

    it must "accept long string as data payload"  in new CommonSetup {
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(1), Data("qwertyuiopasdfghjklzxcvbnm!@#$%^&*()_+sfsdfegeregsdf2347234872395823957923489we7r93")))
    }

    it must "accept queries when history is empty"  in new CommonSetup {
        container.query(Timepoint(1)) shouldBe None
        container.getFirstEntry shouldBe None
        container.getLastEntry shouldBe None
        container.getAllEntries.toArray shouldBe Array()
    }

    it must "handle size exceeding declared initial size" in new CommonSetup {
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(1), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(2), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(3), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(4), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(5), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(6), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(7), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(7), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(9), Data("1")))
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(10), Data("1")))

        //here is where the resizing must happen
        container.insertOrOverrideEntry(HistoryEntry(Timepoint(11), Data("1")))
    }

}
