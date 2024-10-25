package wizard

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec

class WizardTests extends AnyWordSpec with Matchers {
    "Wizard" should {
        "have a bar" in {
            Wizard.bar() shouldBe s"---------${Wizard.eol}"
        }
        "have a bar2" in {
            Wizard.bar2() shouldBe s"---------------------------------${Wizard.eol}"
        }
        "have cells" in {
            Wizard.cells() shouldBe s"|       |${Wizard.eol}"
        }
        "have cells2" in {
            Wizard.cells2() shouldBe s"| game  |${Wizard.eol}"
        }
        "have cells3" in {
            Wizard.cells3() shouldBe s"| trump |${Wizard.eol}"
        }
        "have cells4" in {
            Wizard.cells4() shouldBe s"|Set win|\t|Set win|\t|Set win|\t${Wizard.eol}"
        }
        "have cells5" in {
            Wizard.cells5() shouldBe s"|       |\t|       |\t|       |\t${Wizard.eol}"
        }
        "have mesh2" in {
            Wizard.mesh2 shouldBe s"---------${Wizard.eol}|       |${Wizard.eol}| game  |${Wizard.eol}|       |${Wizard.eol}---------${Wizard.eol}"
        }
        "have mesh3" in {
            Wizard.mesh3 shouldBe s"---------${Wizard.eol}|       |${Wizard.eol}| trump |${Wizard.eol}|       |${Wizard.eol}---------${Wizard.eol}"
        }
        "have mesh4" in {
            Wizard.mesh4 shouldBe s"---------------------------------${Wizard.eol}|       |\t|       |\t|       |\t${Wizard.eol}|Set win|\t|Set win|\t|Set win|\t${Wizard.eol}|       |\t|       |\t|       |\t${Wizard.eol}---------------------------------${Wizard.eol}"
        }

    }
}