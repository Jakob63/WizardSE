// Round.scala
package wizard.rounds

import wizard.player.Player

class Round(players: List[Player]) {
  // Aktueller Trumpf
  var trump: Color = _
  var leadSuit: Color = _

  // Methode zum Setzen des Trumpfs
  def setTrump(trump: Color): Unit = {
    this.trump = trump
  }

  // is game over
  def isOver(): Boolean = {
    players.forall(player => player.hand.isEmpty)
  }

  // finalize round
  def finalizeRound(): Unit = {
    players.foreach(player => player.points += player.roundPoints)
    players.foreach(player => player.tricks += player.roundTricks)
    players.foreach(player => player.bids += player.roundBids)
    players.foreach(player => player.roundPoints = 0)
    players.foreach(player => player.roundTricks = 0)
    players.foreach(player => player.roundBids = 0)
  }

  // Methode zum Spielen einer Runde
  def playRound(currentround: Int): Unit = {
    // Bieten bzw angeben wie viele
    players.foreach(player => player.bid())
    // Karten spielen
    for (i <- 1 to currentround) {
      leadSuit = null
      players.foreach { player =>
        val card = player.playCard(leadSuit, trump)
        if (leadSuit == null && card.value != "Wizard" && card.value != "Fool") {
          leadSuit = card.color
        }
      }
    }
    // Punkte vergeben
    players.foreach(player => player.addPoints(player.roundPoints))
    // Stiche vergeben
    players.foreach(player => player.addTricks(player.roundTricks))
  }
}