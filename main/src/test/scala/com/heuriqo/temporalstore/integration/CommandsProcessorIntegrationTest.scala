package com.heuriqo.temporalstore.integration

import com.heuriqo.temporalstore.datastore.{ArrayBufferHistoryContainer, TemporalDatastoreImpl}
import com.heuriqo.temporalstore.interpreter.{CommandsParser, CommandsProcessor}
import com.heuriqo.temporalstore.unit.BaseSpec

/**
 * This test implements the interactive session example contained in the spec of the excercise.
 */
class CommandsProcessorIntegrationTest extends BaseSpec {

    trait CommonSetup {
        val datastore = new TemporalDatastoreImpl(10)(new ArrayBufferHistoryContainer(10))
        val commandsParser = new CommandsParser
        val proc = new CommandsProcessor(datastore, commandsParser)
    }

    "Command processor" must "correctly handle example interactive session" in new CommonSetup {
        proc.execute("CREATE 0 100 1.5") shouldBe "OK 1.5"
        proc.execute("UPDATE 0 105 1.6") shouldBe "OK 1.5"
        proc.execute("GET 0 100") shouldBe "OK 1.5"
        proc.execute("GET 0 110") shouldBe "OK 1.6"
        proc.execute("LATEST 0") shouldBe "OK 105 1.6"
        proc.execute("LATEST 1") should startWith ("ERR")
        proc.execute("CREATE 1 110 2.5") shouldBe "OK 2.5"
        proc.execute("CREATE 1 115 2.4") should startWith ("ERR")
        proc.execute("UPDATE 1 115 2.4") shouldBe "OK 2.5"
        proc.execute("UPDATE 1 120 2.3") shouldBe "OK 2.4"
        proc.execute("UPDATE 1 125 2.2") shouldBe "OK 2.3"
        proc.execute("LATEST 1") shouldBe "OK 125 2.2"
        proc.execute("GET 1 120") shouldBe "OK 2.3"
        proc.execute("UPDATE 1 120 2.35") shouldBe "OK 2.3"
        proc.execute("GET 1 122") shouldBe "OK 2.35"
        proc.execute("DELETE 1 122") shouldBe "OK 2.35"
        proc.execute("GET 1 125") shouldBe "OK 2.35"
        proc.execute("DELETE 1") shouldBe "OK 2.35"
    }

}
