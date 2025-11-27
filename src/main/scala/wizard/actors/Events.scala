package wizard.actors

object Events {
  val Info                   = "info"
  val ErrorUnknownCommand    = "error.unknown.command"
  val ErrorInvalidPayload    = "error.invalid.payload"

  def accepted(cmd: String): String = s"$cmd.accepted"
  def rejected(cmd: String): String = s"$cmd.rejected"

  val GameStarted            = "game.started"
  val RoundStarted           = "round.started"
  val TrumpCard              = "trump.card"
  val PlayersHandsUpdated    = "players.hands.updated"
  val TrickCardPlayed        = "trick.card.played"
  val RoundFinished          = "round.finished"
  val PlayerEvent            = "player.event"
  val PlayerNamesPrompt      = "player.names.prompt"
  val PlayerHand             = "player.hand"
  val PlayerPlayCard         = "player.play.card"
}
