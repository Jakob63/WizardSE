package wizard

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import com.google.inject.Guice
import wizard.model.fileIoComponent.FileIOInterface

class WizardModuleTest extends AnyWordSpec with Matchers {
  "A WizardModule" should {
    "bind FileIOInterface to an implementation" in {
      val injector = Guice.createInjector(new WizardModule)
      val fileIo = injector.getInstance(classOf[FileIOInterface])
      
      fileIo should not be null
      (fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO] || 
       fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoJsonImpl.FileIO]) should be (true)
    }

    "provide the XML implementation by default" in {
      val injector = Guice.createInjector(new WizardModule)
      val fileIo = injector.getInstance(classOf[FileIOInterface])
      fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO] should be (true)
    }

    "allow manual binding of JSON implementation" in {
        import com.google.inject.AbstractModule
        import net.codingwell.scalaguice.ScalaModule

        val jsonModule = new AbstractModule with ScalaModule {
            override def configure(): Unit = {
                bind[FileIOInterface].to[wizard.model.fileIoComponent.fileIoJsonImpl.FileIO]
            }
        }
        val injector = Guice.createInjector(jsonModule)
        val fileIo = injector.getInstance(classOf[FileIOInterface])
        fileIo.isInstanceOf[wizard.model.fileIoComponent.fileIoJsonImpl.FileIO] should be (true)
    }

    "be used in GameLogic to provide a FileIOInterface" in {
      val gameLogic = new wizard.controller.GameLogic
      gameLogic.fileIo should not be null
      gameLogic.fileIo.isInstanceOf[FileIOInterface] should be (true)
    }
  }
}
