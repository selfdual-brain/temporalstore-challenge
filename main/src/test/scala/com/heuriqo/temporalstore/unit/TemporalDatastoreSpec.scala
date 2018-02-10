package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore._

class TemporalDatastoreSpec extends BaseSpec {

    trait CommonSetup {
        val db = new TemporalDatastoreImpl(3)(new ArrayBufferHistoryContainer(10))
        val entry1 = HistoryEntry(Timepoint(10), Data("x"))
        val entry2 = HistoryEntry(Timepoint(20), Data("y"))
        val entry3 = HistoryEntry(Timepoint(30), Data("z"))
    }

    "Temporal datastore" must "correctly create new history" in new CommonSetup {
        db.create(Identifier(1), entry1)
        db.get(Identifier(1), Timepoint(12)) shouldBe Some(entry1)
    }

    it must "refuse to re-create history for the same identifier" in new CommonSetup {
        db.create(Identifier(1), entry1)

        intercept[IdentifierAlreadyRegisteredException] {
            db.create(Identifier(1), entry2)
        }
    }

    it must "refuse to update history for unknown identifier"  in new CommonSetup {
        intercept[UnknownIdentifierException] {
            db.update(Identifier(1), entry1)
        }
    }

    it must "keep the identifier registered even if all history entries are deleted"  in new CommonSetup {
        db.create(Identifier(1), entry1)
        db.deleteHistoryFor(Identifier(1))
        db.update(Identifier(1), entry1)
    }

    it must "keep histories for several identifiers to be independent" in new CommonSetup {
        db.create(Identifier(1), entry1)
        db.create(Identifier(2), entry1)
        db.update(Identifier(1), entry2)
        db.getHistory(Identifier(1)).toArray shouldBe Array(entry1, entry2)
        db.getHistory(Identifier(2)).toArray shouldBe Array(entry1)
    }

    it must "handle identifiers collection size exceeding declared initial size" in new CommonSetup {
        db.create(Identifier(1), entry1)
        db.create(Identifier(2), entry1)
        db.create(Identifier(3), entry1)

        //here is where the resizing must happen
        db.create(Identifier(4), entry1)

    }

}
