package com.vango.pokertable


import com.vango.pokertable.card.Card
import com.vango.pokertable.player.CardType.*
import com.vango.pokertable.player.Player

class PokerTable(private val players: List<Player>,
                 private val dealerCards: List<Card>) {

    var playerWinProbabilities: MutableList<PlayerWinProbability> = mutableListOf()
    var winType: WinType? = null

    init {
        players.forEach { player ->
            val playerProbability = PlayerWinProbability(player)
            players.forEach { otherPlayer ->
                if (player != otherPlayer) {
                    playerProbability.updateCardStatus(otherPlayer.card1, OTHER_PLAYERS)
                    playerProbability.updateCardStatus(otherPlayer.card2, OTHER_PLAYERS)
                }
            }
            playerProbability.updateCardStatus(player.card1, PLAYER)
            playerProbability.updateCardStatus(player.card2, PLAYER)
            dealerCards.forEach { card -> playerProbability.updateCardStatus(card, DEALER) }
            playerProbability.calculateProbabilities()
            playerWinProbabilities.add(playerProbability)
        }
    }

    fun winType(): WinType? {
        return winType
    }

    fun generateResults(): List<PlayerWinProbability> {
        if (dealerCards.size < 3)
            return playerWinProbabilities

        val highestProbability = playerWinProbabilities.maxBy { it.bestWinTypeProbability }!!.bestWinTypeProbability

        val highestWinType = playerWinProbabilities
                .filter {
                    it.bestWinTypeProbability == highestProbability
                }.map { it.bestWinType }
                .first()

        playerWinProbabilities
                .filter { it.bestWinType == highestWinType && it.bestWinTypeProbability == highestProbability }
                .map {
                    it.overallProbability = highestProbability
                    winType = highestWinType
                }

        playerWinProbabilities.map { if (it.overallProbability == 0.0) it.overallProbability = it.bestWinTypeProbability  }

        return playerWinProbabilities
    }

}