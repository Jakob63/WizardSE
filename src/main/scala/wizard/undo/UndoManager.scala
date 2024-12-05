package wizard.undo

import java.util.NoSuchElementException

class UndoManager {
    private var undoStack: List[Command] = Nil
    private var redoStack: List[Command] = Nil

    def doStep(command: Command): Unit = {
        undoStack = command :: undoStack
        command.doStep()
    }

    def undoStep(): Unit = {
        undoStack match {
            case Nil => throw new NoSuchElementException("No commands to undo")
            case head :: stack =>
                head.undoStep()
                undoStack = stack
                redoStack = head :: redoStack
        }
    }

    def redoStep(): Unit = {
        redoStack match {
            case Nil => throw new NoSuchElementException("No commands to redo")
            case head :: stack =>
                head.doStep()
                redoStack = stack
                undoStack = head :: undoStack
        }
    }
}