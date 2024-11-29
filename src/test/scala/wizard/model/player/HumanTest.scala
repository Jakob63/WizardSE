//package wizard.model.player
//
//import org.scalatest.matchers.must.Matchers
//import org.scalatest.wordspec.AnyWordSpec
//
//class HumanTest extends AnyWordSpec with Matchers {
//    
//    "Human" should {
//        "correct bids" in {
//            val human = new Human("Testhuman")
//            human.bids shouldBe 0
//    }
//        "correct playerType" in {
//            val human = new Human("Testhuman")
//            human.playerType shouldBe "Human"
//        }
//        
//        "correct notify" in {
//            val human = new Human("Testhuman")
//            human.notify("bid einlesen") shouldBe "bid einlesen"
//        }
//        
//        "correct notifyOberservers(invalid input, bid again)" in {
//            val human = new Human("Testhuman")
//            human.notifyObservers("invalid input, bid again") shouldBe "invalid input, bid again"
//        }
//        
//        "bid() return should be Int" in {
//            val human = new Human("Testhuman")
//            human.bid("1") shouldBe 1
//        }
//        
//        "valid input" in {
//            val human = new Human("Testhuman")
//            human.validInput("1") shouldBe true
//        }
//        
//        "invalid input" in {
//            val human = new Human("Testhuman")
//            human.validInput("a") shouldBe false
//        }
//        
//        "correct play" in {
//            val human = new Human("Testhuman")
//            human.play("Card") shouldBe "Card"
//        }
//        
//        "playCard() return should be Card" in {
//            val human = new Human("Testhuman")
//            human.playCard("Color", "Color", 1, "Card") shouldBe "Card"
//        }
//        
//        "val input should be String" in {
//            val human = new Human("Testhuman")
//            human.input("1") shouldBe "1"
//        }
//        
//        "correct catch NumberFormatException" in {
//            val human = new Human("Testhuman")
//            human.catchNumberFormatException("a") shouldBe "Invalid input"
//        }
//        
//        "correct notifyObservers(invalid Card) should be String" in {
//            val human = new Human("Testhuman")
//            human.notifyObservers("invalid Card") shouldBe "invalid Card"
//        }
//        
//        
//}
