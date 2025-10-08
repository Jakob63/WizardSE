package wizard.model.cards

enum Color():
    case Red
    case Green
    case Blue
    case Yellow
end Color

def colorToAnsi(color: Color): String = color match {
    case Color.Red => Console.RED
    case Color.Green => Console.GREEN
    case Color.Blue => Console.BLUE
    case Color.Yellow => Console.YELLOW
}

enum Value(enumValue: String):
    
    def cardType(): String = enumValue
    
    case Chester extends Value("C")
    case One extends Value("1")
    case Two extends Value("2")
    case Three extends Value("3")
    case Four extends Value("4")
    case Five extends Value("5")
    case Six extends Value("6")
    case Seven extends Value("7")
    case Eight extends Value("8")
    case Nine extends Value("9")
    case Ten extends Value("10")
    case Eleven extends Value("11")
    case Twelve extends Value("12")
    case Thirteen extends Value("13")
    case WizardKarte extends Value("W")
end Value

def valueToAnsi(value: Value): String = value match {
    case Value.Chester => Console.RESET
    case Value.WizardKarte => Console.RESET
    // dont change the console color for the other values
    case _ => ""
    
}
case class Card(value: Value, color: Color){
    private def displayValue(v: Value): String = v match {
        case Value.WizardKarte => "Wizard"
        case other => other.toString
    }
    override def toString: String = value match {
        case Value.Chester | Value.WizardKarte => displayValue(value)
        case _ => s"${displayValue(value)} of $color"
    }
}
