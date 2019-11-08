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
        val highestWinType = playerWinProbabilities
                .mapNotNull { p ->
                    p.winTypeProbabilities.filter { w -> w.value == 100 }.maxBy { w -> w.key }
                }.map { v -> v.key }
                .first()

        playerWinProbabilities
                .filter { p -> p.winTypeProbabilities[highestWinType] == 100 }
                .map { p ->
                    p.overallProbability = 100
                    winType = highestWinType
                }

        return playerWinProbabilities
    }

}