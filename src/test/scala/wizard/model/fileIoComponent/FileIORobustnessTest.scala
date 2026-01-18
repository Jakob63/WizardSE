package wizard.model.fileIoComponent

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.concurrent.TimeLimitedTests
import org.scalatest.time.SpanSugar.*
import org.scalatest.BeforeAndAfterEach
import wizard.controller.GameLogic
import wizard.actionmanagement.Observer
import wizard.actionmanagement.InputRouter
import java.io.{File, PrintWriter}

class FileIORobustnessTest extends AnyWordSpec with Matchers with TimeLimitedTests with BeforeAndAfterEach {

  val timeLimit = 30.seconds

  override def beforeEach(): Unit = {
    InputRouter.clear()
    System.setProperty("WIZARD_INTERACTIVE", "false")
  }

  override def afterEach(): Unit = {
    System.setProperty("WIZARD_INTERACTIVE", "true")
  }

  class TestObserver extends Observer {
    var loadFailedCalled = false
    var failedTitle = ""
    override def update(updateMSG: String, obj: Any*): Any = {
      if (updateMSG == "LoadFailed") {
        loadFailedCalled = true
        failedTitle = obj.head.asInstanceOf[String]
      }
    }
  }

  "GameLogic loading robustness" should {

    "handle non-existent files" in {
      val gameLogic = new GameLogic
      val obs = new TestObserver
      gameLogic.add(obs)
      
      gameLogic.load("non_existent_file_12345")
      obs.loadFailedCalled should be (true)
    }

    "handle malformed JSON files" in {
      val gameLogic = new GameLogic
      val jsonFileIO = new wizard.model.fileIoComponent.fileIoJsonImpl.FileIO
      val tempFile = File.createTempFile("malformed", ".json")
      val pw = new PrintWriter(tempFile)
      pw.write("{ this is not valid json }")
      pw.close()
      
      intercept[Exception] {
        jsonFileIO.load(tempFile.getAbsolutePath)
      }
      tempFile.delete()
    }

    "handle malformed XML files" in {
      val xmlFileIO = new wizard.model.fileIoComponent.fileIoXmlImpl.FileIO
      val tempFile = File.createTempFile("malformed", ".xml")
      val pw = new PrintWriter(tempFile)
      pw.write("<game><invalid xml")
      pw.close()
      
      intercept[Exception] {
        xmlFileIO.load(tempFile.getAbsolutePath)
      }
      tempFile.delete()
    }
    
    "handle missing fields in JSON" in {
      val jsonFileIO = new wizard.model.fileIoComponent.fileIoJsonImpl.FileIO
      val tempFile = File.createTempFile("missing_fields", ".json")
      val pw = new PrintWriter(tempFile)
      pw.write("""{"rounds": 10}""")
      pw.close()
      
      intercept[Exception] {
        jsonFileIO.load(tempFile.getAbsolutePath)
      }
      tempFile.delete()
    }

    "handle missing attributes in XML" in {
      val xmlFileIO = new wizard.model.fileIoComponent.fileIoXmlImpl.FileIO
      val tempFile = File.createTempFile("missing_attr", ".xml")
      val pw = new PrintWriter(tempFile)
      pw.write("""<game rounds="10"></game>""")
      pw.close()
      
      intercept[Exception] {
        xmlFileIO.load(tempFile.getAbsolutePath)
      }
      tempFile.delete()
    }
  }
}
