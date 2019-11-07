package com.vango.pokertable

import com.vango.pokertable.WinType.*
import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank
import com.vango.pokertable.card.Suit
import com.vango.pokertable.player.CardStatus
import com.vango.pokertable.player.CardType
import com.vango.pokertable.player.CardType.*
import com.vango.pokertable.player.Player

class PlayerWinProbability(player: Player) : Player(player.card1, player.card2) {

    private val probabilityGrid = mutableMapOf<Suit, MutableMap<Rank, CardStatus>>()
    val winTypeProbabilities = mutableMapOf<WinType, Int>()

    init {
        for (suit in Suit.values()) {
            probabilityGrid[suit] = mutableMapOf<Rank, CardStatus>()
            for (rank in Rank.values()) {
                probabilityGrid[suit]!![rank] = CardStatus(Card(rank, suit), DECK)
            }
        }
    }

    fun updateAvailability(card: Card, cardType: CardType) {
        probabilityGrid[card.suit]!![card.rank]!!.setType(cardType)
    }

    fun calculateProbabilities() {
        evaluateStraightAndRoyalFlush()
        evaluateFourOfAKind()
        evaluateFullHouse()
        evaluateFlush()
        evaluateStraight(null)
        evaluateThreeOfAKind()
        evaluateTwoPair()
        evaluatePair()
        evaluateHighestCard()
    }

    private fun evaluateFlush() {
        if(getPlayerAndDealerCards().groupingBy { card -> card.suit.suit }.eachCount().filter { (_, v) -> v >= 5 }.isNotEmpty())
            winTypeProbabilities[FLUSH] = 100
    }

    private fun evaluateFullHouse() {
        if (getRankingsCount().containsValue(2) && getRankingsCount().containsValue(3))
            winTypeProbabilities[FULL_HOUSE] = 100
    }

    private fun evaluateFourOfAKind() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 4 }.isNotEmpty())
            winTypeProbabilities[FOUR_OF_A_KIND] = 100
    }

    private fun evaluateThreeOfAKind() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 3 }.isNotEmpty())
            winTypeProbabilities[THREE_OF_A_KIND] = 100
    }

    private fun evaluateTwoPair() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 2 }.size == 2)
            winTypeProbabilities[TWO_PAIR] = 100
    }

    private fun evaluatePair() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 2 }.isNotEmpty())
            winTypeProbabilities[PAIR] = 100
    }

    private fun getRankingsCount(): Map<Int, Int> {
        return getPlayerAndDealerCards().groupingBy { card -> card.rank.ranking }.eachCount()
    }

    private fun evaluateStraightAndRoyalFlush() {
        for (suit in Suit.values()) {
            evaluateStraight(suit)
            if (winTypeProbabilities[STRAIGHT_FLUSH] == 100 && getPlayerAndDealerCards().contains(Card(Rank.ACE, suit))) {
                winTypeProbabilities[ROYAL_FLUSH] = 100
            }
        }
    }

    private fun evaluateStraight(suit: Suit?) {
        val rankingsInPlay = getPlayerAndDealerCards()
                .filter { card -> if (suit != null) card.suit == suit else true }
                .groupingBy { card -> card.rank }
                .eachCount()
        var i = 0
        for (r in Rank.values()) {
            if (rankingsInPlay.contains(r)) i++
            else i = 0
            if (i == 5) break
        }
        if (rankingsInPlay.contains(Rank.ACE)) i++
        if (i >= 5 && suit != null) winTypeProbabilities[STRAIGHT_FLUSH] = 100
        if (i >= 5 && suit == null) winTypeProbabilities[STRAIGHT] = 100
    }

    private fun evaluateHighestCard() {
        val otherPlayersHighestRank = getOtherPlayersCards().maxBy { card -> card.rank.ranking }
        val otherPlayersSecondHighestRank = getOtherPlayersCards()
                .filter { card -> card.rank.ranking != otherPlayersHighestRank!!.rank.ranking }.maxBy { card -> card.rank.ranking }

        if (otherPlayersHighestRank!!.rank.ranking < getHighestCard().rank.ranking)
            winTypeProbabilities[HIGHEST_CARD] = 100
        else if (otherPlayersSecondHighestRank != null &&
                otherPlayersHighestRank.rank.ranking == getHighestCard().rank.ranking &&
                otherPlayersSecondHighestRank.rank.ranking == getLowestCard().rank.ranking)
            winTypeProbabilities[HIGHEST_CARD] = 100
        else if (otherPlayersSecondHighestRank != null &&
                otherPlayersHighestRank.rank.ranking == getHighestCard().rank.ranking &&
                otherPlayersSecondHighestRank.rank.ranking < getLowestCard().rank.ranking)
            winTypeProbabilities[HIGHEST_CARD] = 100
    }

    private fun getPlayerAndDealerCards(): List<Card> {
        return probabilityGrid.map { suitMap: Map.Entry<Suit, MutableMap<Rank, CardStatus>> ->
            suitMap.value
                    .filter { rankMap -> listOf(PLAYER, DEALER).contains(rankMap.value.cardType) }
                    .map { rankMap ->
                        Card(rankMap.key, suitMap.key)
                    }
        }.flatten()
    }

    private fun getOtherPlayersCards(): List<Card> {
        return probabilityGrid.map { suitMap: Map.Entry<Suit, MutableMap<Rank, CardStatus>> ->
            suitMap.value
                    .filter { rankMap -> listOf(OTHER_PLAYERS).contains(rankMap.value.cardType) }
                    .map { rankMap ->
                        Card(rankMap.key, suitMap.key)
                    }
        }.flatten()
    }
}