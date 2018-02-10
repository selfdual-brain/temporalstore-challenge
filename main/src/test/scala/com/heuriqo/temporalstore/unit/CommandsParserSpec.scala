package com.heuriqo.temporalstore.unit

import com.heuriqo.temporalstore.datastore.{Data, Identifier, Timepoint}
import com.heuriqo.temporalstore.interpreter._

class CommandsParserSpec extends BaseSpec {

    trait CommonSetup {
        val parser = new CommandsParser
    }

    "Commands parser" must "handle CREATE command" in new CommonSetup {
        parser.parse("create 1 2 abc") shouldBe Right(CreateCommand(Identifier(1), Timepoint(2), Data("abc")))
        parser.parse("create 1 2").isLeft shouldBe true
        parser.parse("create").isLeft shouldBe true
        parser.parse("create 1 2 abc x").isLeft shouldBe true
    }

    it must "handle UPDATE command" in new CommonSetup {
        parser.parse("update 1 2 abc") shouldBe Right(UpdateCommand(Identifier(1), Timepoint(2), Data("abc")))
        parser.parse("update 1 2").isLeft shouldBe true
        parser.parse("update").isLeft shouldBe true
        parser.parse("update 1 2 abc x").isLeft shouldBe true
    }

    it must "handle GET command" in new CommonSetup {
        parser.parse("get 1 2") shouldBe Right(GetCommand(Identifier(1), Timepoint(2)))
        parser.parse("get 1").isLeft shouldBe true
        parser.parse("get 1 2 3").isLeft shouldBe true
    }

    it must "handle DELETE command" in new CommonSetup {
        parser.parse("delete 1") shouldBe Right(DeleteAllCommand(Identifier(1)))
        parser.parse("delete 1 2") shouldBe Right(DeleteFromCommand(Identifier(1), Timepoint(2)))
        parser.parse("delete").isLeft shouldBe true
        parser.parse("delete 1 2 3  4").isLeft shouldBe true
    }

    it must "handle LATEST command" in new CommonSetup {
        parser.parse("latest 1") shouldBe Right(LatestCommand(Identifier(1)))
        parser.parse("latest").isLeft shouldBe true
        parser.parse("latest 1 2").isLeft shouldBe true
    }

    it must "handle LIST command" in new CommonSetup {
        parser.parse("list 1") shouldBe Right(ListCommand(Identifier(1)))
        parser.parse("list").isLeft shouldBe true
        parser.parse("list 1 2").isLeft shouldBe true
    }

    it must "handle QUIT command" in new CommonSetup {
        parser.parse("quit") shouldBe Right(QuitCommand)
        parser.parse("quit 1").isLeft shouldBe true
    }

    it must "gracefully handle unknown commands" in new CommonSetup {
        parser.parse("bingo 1").isLeft shouldBe true
    }

    it must "gracefully handle command that has many tokens" in new CommonSetup {
        parser.parse("delete 1 2 3 4 5 6 7 8 9 10 q w e r t y u i o p a s d f g h j k l z x c v b n m ! @ # $ % ^ & * ( )").isLeft shouldBe true
        parser.parse("bingo 1 2 3 4 5 6 7 8 9 10 q w e r t y u i o p a s d f g h j k l z x c v b n m ! @ # $ % ^ & * ( )").isLeft shouldBe true
    }

    it must "gracefully handle empty input" in new CommonSetup {
        parser.parse("").isLeft shouldBe true
    }

    it must "gracefully handle number format and range check for identifiers" in new CommonSetup {
        //not a number
        parser.parse("create a 100").isLeft shouldBe true
        //number but not integer
        parser.parse("create 0.1 100").isLeft shouldBe true
        //negative integer
        parser.parse("create -1 100").isLeft shouldBe true
        //too big integer
        parser.parse("create 10000000000000000 100").isLeft shouldBe true
    }

    it must "gracefully handle number format and range check for timepoints" in new CommonSetup {
        //not a number
        parser.parse("create 100 a").isLeft shouldBe true
        //number but not integer
        parser.parse("create 100 0.1").isLeft shouldBe true
        //negative integer
        parser.parse("create 100 -1").isLeft shouldBe true
        //too big integer
        parser.parse("create 100 10000000000000000").isLeft shouldBe true
    }

}
