# Dependency Injection

Das Projekt nutzt **Google Guice** (mit der `scala-guice` Erweiterung), um Abhängigkeiten zwischen Komponenten sauber zu verwalten und lose Kopplung zu ermöglichen.

### Konfiguration (Module)
In der Klasse `wizard.WizardModule` werden die Bindungen zwischen Interfaces und konkreten Implementierungen definiert. Hier wird beispielsweise festgelegt, ob das Spiel XML oder JSON zur Speicherung verwenden soll.

```scala
class WizardModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    // XML Bindung:
    bind[FileIOInterface].to[wizard.model.fileIoComponent.fileIoXmlImpl.FileIO]
    // JSON Bindung (auskommentiert):
    // bind[FileIOInterface].to[wizard.model.fileIoComponent.fileIoJsonImpl.FileIO]
  }
}
```

### Nutzung
Im Controller (`GameLogic`) wird der Guice-Injector initialisiert, um die benötigten Instanzen zu erzeugen:

```scala
val injector: Injector = Guice.createInjector(new WizardModule)
val fileIo: FileIOInterface = injector.getInstance(classOf[FileIOInterface])
```

Dadurch muss der Controller nicht wissen, welche konkrete FileIO-Klasse er nutzt, was den Austausch der Komponenten (z.B. für Tests oder neue Formate) extrem vereinfacht.


