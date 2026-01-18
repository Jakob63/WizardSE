package wizard

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import wizard.model.fileIoComponent.FileIOInterface

class WizardModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    // XML:
    //bind[FileIOInterface].to[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO]
    // JSON:
    bind[FileIOInterface].to[wizard.model.fileIoComponent.fileIoJsonImpl.FileIO]
  }
}
