package wizard

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class WizardTest extends AnyWordSpec with Matchers {
  "The Wizard object" should {
    val eol = Wizard.eol

    "produce correct bar strings" in {
      Wizard.bar(4, 2) should be ("---------" + eol)
      Wizard.bar2(4, 2) should be ("---------" + eol)
    }

    "produce correct cell strings" in {
      Wizard.cells(7, 1) should be ("|       |" + eol)
      Wizard.cells2() should be ("| game  |" + eol)
      Wizard.cells3() should be ("| trump |" + eol)
      Wizard.cells4() should be ("|Set win|\t|Set win|\t|Set win|\t" + eol)
      Wizard.cells5(7, 1) should be ("|       |\t|       |\t|       |\t" + eol)
    }

    "produce correct mesh strings" in {
      val m2 = Wizard.mesh2
      m2 should include ("game")
      m2.split(eol).length should be (5)

      val m3 = Wizard.mesh3
      m3 should include ("trump")
      m3.split(eol).length should be (5)

      val m4 = Wizard.mesh4
      m4 should include ("Set win")
      m4.split(eol).length should be (5)
    }
  }
}
