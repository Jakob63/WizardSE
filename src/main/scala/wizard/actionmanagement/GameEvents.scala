package wizard.actionmanagement

import wizard.model.player.Player

sealed trait GameEvent

//case object Event2 extends GameEvent
case class SetPlayer(player1: String) extends GameEvent

    case object StartGame extends GameEvent
    case object PlayGame extends GameEvent
    case object AskForPlayerCount extends GameEvent
    case class PlayerCountSelected(count: Int) extends GameEvent
    case object AskForPlayerNames extends GameEvent
    case class CardsDealt(players: List[Player]) extends GameEvent
    case class ShowHand(player: Player) extends GameEvent
    case object CardAuswahl extends GameEvent
    
    case object RoundOver extends GameEvent
    case object TrickOver extends GameEvent
    case object Bid extends GameEvent
    case object PlayCard extends GameEvent
    case object EndRound extends GameEvent
    case object EndTrick extends GameEvent
    case object EndGame extends GameEvent
    case object EndBid extends GameEvent
    case object EndPlayCard extends GameEvent
    case object EndRoundOver extends GameEvent
    case object EndTrickOver extends GameEvent
    case object EndGameOver extends GameEvent
    case object EndStartGame extends GameEvent
    case object EndPlayGame extends GameEvent

