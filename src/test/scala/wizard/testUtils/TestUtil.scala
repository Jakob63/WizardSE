package wizard.testUtils

import wizard.Wizard

import java.io.ByteArrayInputStream

object TestUtil {
    def simulateInput[t] (input: String)(block: => t): t = {
        Console.withIn(new ByteArrayInputStream(input.getBytes())) {
            block
        }
    }
}
