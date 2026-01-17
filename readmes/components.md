# Komponenten & Interfaces

Die Architektur des Spiels ist in klar voneinander getrennte Komponenten unterteilt, um die Wartbarkeit und Erweiterbarkeit zu erhöhen.

### Kapselung durch Interfaces
Ein zentrales Beispiel für die Komponenten-Struktur ist das **FileIO-System**. Die Spiellogik interagiert nicht direkt mit den konkreten Speicher-Implementierungen, sondern nutzt das `FileIOInterface`.

- **Interface:** `wizard.model.fileIoComponent.FileIOInterface`
- **Implementierungen:** 
  - `wizard.model.fileIoComponent.fileIoXmlImpl.FileIO`
  - `wizard.model.fileIoComponent.fileIoJsonImpl.FileIO`

### Zugriffsschutz
Durch diese Struktur hat der Rest der Applikation keinen Zugriff auf die internen Details der Speicherung (wie z.B. XML-Strukturen oder JSON-Parsing-Logik). Der Zugriff erfolgt ausschließlich über die definierten Methoden des Interfaces. Dies ermöglicht es, die Implementierung auszutauschen, ohne den restlichen Code anpassen zu müssen.


