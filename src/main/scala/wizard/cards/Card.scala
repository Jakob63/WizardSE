package wizard.cards

enum Color:
    case Red
    case Green
    case Blue
    case Yellow
end Color

enum Value(enumValue: String):
    case Chester extends Value("Chester")
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
    case Wizard extends Value("Wizard")
end Value


case class Card(value: Value, color: Color){
    override def toString: String = s"$value of $color"
}
