package com.vango.pokertable

import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank
import com.vango.pokertable.card.Suit
import com.vango.pokertable.card.Suit.*
import com.vango.pokertable.player.CardStatus
import com.vango.pokertable.player.CardType.*
import com.vango.pokertable.player.CardType
import com.vango.pokertable.player.Player
import com.vango.pokertable.WinType.*

class PlayerWinProbability(player: Player) : Player(player.card1, player.card2) {

    private val probabilityGrid = mutableMapOf<String, MutableMap<Int, CardStatus>>()
    val winTypeProbabilities = mutableMapOf<WinType, Int>()

    init {
        for (suit in Suit.values()) {
            probabilityGrid[suit.suit] = mutableMapOf<Int, CardStatus>()
            for (rank in Rank.values()) {
                probabilityGrid[suit.suit]!![rank.ranking] = CardStatus(Card(rank, suit), true, DECK)
            }
        }
    }

    fun updateAvailability(card: Card, cardType: CardType) {
        val cardStatus = probabilityGrid[card.suit.suit]!![card.rank.ranking]!!
        cardStatus.unavailable()
        cardStatus.setType(cardType)
    }

    fun calculateProbabilities() {
        evaluateRoyalFlushProbabilities()
    }

    private fun evaluateRoyalFlushProbabilities() {
        val royalFlushEntries = mutableMapOf<String, Int>()
        Suit.values().forEach { suit -> royalFlushEntries[suit.suit] = 0 }
        probabilityGrid.entries.forEach { suitEntry ->
            run {
                royalFlushEntries[suitEntry.key] = suitEntry.value.entries.filter { rankEntry ->
                    rankEntry.value.rank.ranking >= 10 &&
                            rankEntry.value.suit.suit == suitEntry.key &&
                            (listOf(DEALER, PLAYER).contains(rankEntry.value.cardType))
                }.size

                if (royalFlushEntries[suitEntry.key] == 5) winTypeProbabilities[ROYAL_FLUSH] = 100
                //else //calculate probability
            }
        }
    }
}