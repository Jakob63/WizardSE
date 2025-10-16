package wizard

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.must.Matchers
import java.util.logging.{Level, Logger}

class WizardLoggingConfigTest extends AnyWordSpec with Matchers {

  private val loggerNames = List("javafx", "com.sun.javafx", "com.sun.javafx.application")

  private case class Saved(logger: Logger, level: Option[Level], useParent: Boolean)
  private def save(): List[Saved] =
    loggerNames.map { n =>
      val l = Logger.getLogger(n)
      Saved(l, Option(l.getLevel), l.getUseParentHandlers)
    }
  private def restore(saved: List[Saved]): Unit =
    saved.foreach { s =>
      // Note: getLevel may be null to indicate inheriting from parent; handle accordingly
      s.level match {
        case Some(lvl) => s.logger.setLevel(lvl)
        case None => s.logger.setLevel(null)
      }
      s.logger.setUseParentHandlers(s.useParent)
    }

  "Wizard.configureJavaFXLogging" should {
    "set OFF levels and disable parent handlers in quiet mode (default)" in {
      val saved = save()
      try {
        System.clearProperty("WIZARD_QUIET_LOGS") // default = quiet
        Wizard.configureJavaFXLogging()
        loggerNames.foreach { n =>
          val l = Logger.getLogger(n)
          l.getLevel mustBe Level.OFF
          l.getUseParentHandlers mustBe false
        }
      } finally {
        restore(saved)
      }
    }

    "enable logging when WIZARD_QUIET_LOGS=0 and respect javafx.logging.level" in {
      val saved = save()
      try {
        System.setProperty("WIZARD_QUIET_LOGS", "0")
        System.setProperty("javafx.logging.level", "SEVERE")
        Wizard.configureJavaFXLogging()
        loggerNames.foreach { n =>
          val l = Logger.getLogger(n)
          l.getLevel mustBe Level.SEVERE
          l.getUseParentHandlers mustBe true
        }
      } finally {
        System.clearProperty("WIZARD_QUIET_LOGS")
        System.clearProperty("javafx.logging.level")
        restore(saved)
      }
    }
  }
}
