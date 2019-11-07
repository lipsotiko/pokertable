package com.vango.pokertable


import com.vango.pokertable.WinType.*
import com.vango.pokertable.card.Card
import com.vango.pokertable.player.CardType.*
import com.vango.pokertable.player.Player

class PokerTable(private val players: List<Player>,
                 private val dealerCards: List<Card>) {

    var playerWinProbabilities: MutableList<PlayerWinProbability> = mutableListOf()
    var winType: WinType? = null
    var playerResults: List<Player> = listOf()

    init {
        players.forEach { player ->
            val playerProbability = PlayerWinProbability(player)
            players.forEach { otherPlayer ->
                if (player != otherPlayer) {
                    playerProbability.updateAvailability(otherPlayer.card1, OTHER_PLAYERS)
                    playerProbability.updateAvailability(otherPlayer.card2, OTHER_PLAYERS)
                }
            }
            playerProbability.updateAvailability(player.card1, PLAYER)
            playerProbability.updateAvailability(player.card2, PLAYER)
            dealerCards.forEach { card -> playerProbability.updateAvailability(card, DEALER) }
            playerProbability.calculateProbabilities()
            playerWinProbabilities.add(playerProbability)
        }
    }

    fun winType(): WinType? {
        return winType
    }

    fun playerResults(): List<Player> {
        return playerResults
    }

    fun evaluate() {
        val playersWithARoyalFlush = playerWinProbabilities.filter { p -> p.winTypeProbabilities[ROYAL_FLUSH] == 100 }
        if (playersWithARoyalFlush.size == 1) {
            winType = ROYAL_FLUSH
            playerResults = playersWithARoyalFlush
            return
        }

        val playersWithStraightFlush = playerWinProbabilities.filter { p -> p.winTypeProbabilities[STRAIGHT_FLUSH] == 100 }
        if (playersWithStraightFlush.size == 1) {
            winType = STRAIGHT_FLUSH
            playerResults = playersWithStraightFlush
            return
        }

        val playersWithFourOfAKind = playerWinProbabilities.filter { p -> p.winTypeProbabilities[FOUR_OF_A_KIND] == 100 }
        if (playersWithFourOfAKind.size == 1) {
            winType = FOUR_OF_A_KIND
            playerResults = playersWithFourOfAKind
            return
        }

        val playersWithFullHouse = playerWinProbabilities.filter { p -> p.winTypeProbabilities[FULL_HOUSE] == 100 }
        if (playersWithFullHouse.size == 1) {
            winType = FULL_HOUSE
            playerResults = playersWithFullHouse
            return
        }

        val playersWithFlush = playerWinProbabilities.filter { p -> p.winTypeProbabilities[FLUSH] == 100 }
        if (playersWithFlush.isNotEmpty()) winType = FLUSH
        if (playersWithFlush.size == 1) {
            playerResults = playersWithFlush
            return
        }

        val playersWithStraight = playerWinProbabilities.filter { p -> p.winTypeProbabilities[STRAIGHT] == 100 }
        if (playersWithStraight.size == 1) {
            winType = STRAIGHT
            playerResults = playersWithStraight
            return
        }

        val playersWithThreeOfAKind = playerWinProbabilities.filter { p -> p.winTypeProbabilities[THREE_OF_A_KIND] == 100 }
        if (playersWithThreeOfAKind.size == 1) {
            winType = THREE_OF_A_KIND
            playerResults = playersWithThreeOfAKind
            return
        }

        val playersWithTwoPair = playerWinProbabilities.filter { p -> p.winTypeProbabilities[TWO_PAIR] == 100 }
        if (playersWithTwoPair.size == 1) {
            winType = TWO_PAIR
            playerResults = playersWithTwoPair
            return
        }

        val playersWithPair = playerWinProbabilities.filter { p -> p.winTypeProbabilities[PAIR] == 100 }
        if (playersWithPair.size == 1) {
            winType = PAIR
            playerResults = playersWithPair
            return
        }

        winType = HIGHEST_CARD
        playerResults = playerWinProbabilities.filter { p -> p.winTypeProbabilities[HIGHEST_CARD] == 100 }
    }

}