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
    var bestWinType: WinType = NOTHING
    var bestWinTypeProbability: Double = 0.0
    var overallProbability = 0.0

    init {
        for (suit in Suit.values()) {
            probabilityGrid[suit] = mutableMapOf<Rank, CardStatus>()
            for (rank in Rank.values()) {
                probabilityGrid[suit]!![rank] = CardStatus(Card(rank, suit), DECK)
            }
        }
    }

    fun updateCardStatus(card: Card, cardType: CardType) {
        probabilityGrid[card.suit]!![card.rank]!!.setType(cardType)
    }

    fun calculateProbabilities() {
        evaluateStraightAndRoyalFlush()
        if (bestWinTypeProbability != 100.0) evaluateFourOfAKind()
        if (bestWinTypeProbability != 100.0) evaluateFullHouse()
        if (bestWinTypeProbability != 100.0) evaluateFlush()
        if (bestWinTypeProbability != 100.0) evaluateStraight(null)
        if (bestWinTypeProbability != 100.0) evaluateThreeOfAKind()
        if (bestWinTypeProbability != 100.0) evaluateTwoPair()
        if (bestWinTypeProbability != 100.0) evaluatePair()
        if (bestWinTypeProbability != 100.0) evaluateHighestCard()
    }

    private fun evaluateStraightAndRoyalFlush() {
        for (suit in Suit.values()) {
            evaluateStraight(suit)
            if (bestWinType == STRAIGHT_FLUSH &&
                    getCardsOfType(listOf(DEALER, PLAYER)).contains(Card(Rank.ACE, suit))) {
                bestWinType = ROYAL_FLUSH
            }
        }
    }

    private fun evaluateFlush() {
        val playerAndDealerCards = getCardsOfType(listOf(DEALER, PLAYER))
        val suitCounts = playerAndDealerCards.groupingBy { it.suit.suit }.eachCount()
        val playersCards = getCardsOfType(listOf(PLAYER))
        var playerHasAtLeastOneCardInFlush = false
        var flushExistsForSuit = false

        for (suit in Suit.values()) {
            val playersSuitedCards = playersCards.filter { it.suit == suit }
            flushExistsForSuit = suitCounts.filter { (_, v) -> v >= 5 }.contains(suit.suit)
            if (flushExistsForSuit && playersSuitedCards.isNotEmpty()) {
                playerHasAtLeastOneCardInFlush = true
                break
            }
        }

        if (flushExistsForSuit && playerHasAtLeastOneCardInFlush && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = FLUSH
            bestWinTypeProbability = 100.0
        } else {
            val maxSuitCount = suitCounts.maxBy { it.value }
            val cardsLeftInDeck = getCardsOfType(listOf(DECK)).size
            val suitedCardsLeftInDeck = getCardsOfType(listOf(DECK)).count { it.suit.suit == maxSuitCount?.key }
            bestWinType = FLUSH
            bestWinTypeProbability = (suitedCardsLeftInDeck.toDouble() / cardsLeftInDeck.toDouble()) * 100
        }
    }

    private fun evaluateFullHouse() {
        if (getRankingsCount().containsValue(2) && getRankingsCount().containsValue(3) && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = FULL_HOUSE
            bestWinTypeProbability = 100.0
        }
    }

    private fun evaluateFourOfAKind() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 4 }.isNotEmpty() && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = FOUR_OF_A_KIND
            bestWinTypeProbability = 100.0
        }
    }

    private fun evaluateThreeOfAKind() {
        if (getRankingsCount().filter { rankingCount -> rankingCount.value == 3 }.isNotEmpty() && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = THREE_OF_A_KIND
            bestWinTypeProbability = 100.0
        }
    }

    private fun evaluateTwoPair() {
        val pairRankings = getRankingsCount().filter { rankingCount -> rankingCount.value == 2 }
        val playerHasAtLeastOneCardInPair = (pairRankings.contains(card1.rank.ranking) || pairRankings.contains(card2.rank.ranking))
        if (playerHasAtLeastOneCardInPair && pairRankings.size == 2 && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = TWO_PAIR
            bestWinTypeProbability = 100.0
        }
    }

    private fun evaluatePair() {
        val pairRankings = getRankingsCount().filter { rankingCount -> rankingCount.value == 2 }
        val playerHasAtLeastOneCardInPair = (pairRankings.contains(card1.rank.ranking) || pairRankings.contains(card2.rank.ranking))
        if (playerHasAtLeastOneCardInPair && getCardsOfType(listOf(DEALER)).size == 5) {
            bestWinType = PAIR
            bestWinTypeProbability = 100.0
        }
    }

    private fun getRankingsCount(): Map<Int, Int> {
        return getCardsOfType(listOf(DEALER, PLAYER)).groupingBy { card -> card.rank.ranking }.eachCount()
    }

    private fun evaluateStraight(suit: Suit?) {
        val rankingsInPlay = getCardsOfType(listOf(DEALER, PLAYER))
                .filter { card -> if (suit != null) card.suit == suit else true }
                .groupingBy { card -> card.rank }
                .eachCount()

        val straight: MutableList<Rank> = mutableListOf()
        for (r in Rank.values()) {
            if (rankingsInPlay.contains(r)) straight.add(r)
            else straight.clear()
            if (straight.size == 5) break
        }
        if (rankingsInPlay.contains(Rank.ACE)) straight.add(Rank.ACE)

        var playerHasAtLeastOneCardInStraight = false
        if ((straight.contains(card1.rank) && suit != null && card1.suit == suit) ||
                (straight.contains(card2.rank) && suit != null && card2.suit == suit) ||
                (straight.contains(card1.rank) && suit == null && card1.suit == suit) ||
                (straight.contains(card2.rank) && suit == null)
        )
            playerHasAtLeastOneCardInStraight = true

        if (straight.size >= 5 && suit != null && playerHasAtLeastOneCardInStraight) {
            bestWinType = STRAIGHT_FLUSH
            bestWinTypeProbability = 100.0
        }
        if (straight.size >= 5 && suit == null && playerHasAtLeastOneCardInStraight) {
            bestWinType = STRAIGHT
            bestWinTypeProbability = 100.0
        }
    }

    private fun evaluateHighestCard() {
        val otherPlayersHighestRank = getOtherPlayersCards().maxBy { card -> card.rank.ranking }
        val otherPlayersSecondHighestRank = getOtherPlayersCards()
                .filter { card -> card.rank.ranking != otherPlayersHighestRank!!.rank.ranking }.maxBy { card -> card.rank.ranking }

        if ((otherPlayersHighestRank!!.rank.ranking < getHighestCard().rank.ranking) ||
                (otherPlayersSecondHighestRank != null &&
                        otherPlayersHighestRank.rank.ranking == getHighestCard().rank.ranking &&
                        otherPlayersSecondHighestRank.rank.ranking == getLowestCard().rank.ranking) ||
                (otherPlayersSecondHighestRank != null &&
                        otherPlayersHighestRank.rank.ranking == getHighestCard().rank.ranking &&
                        otherPlayersSecondHighestRank.rank.ranking < getLowestCard().rank.ranking)) {
            bestWinType = HIGHEST_CARD
            bestWinTypeProbability = 100.0

        }
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

    private fun getCardsOfType(types: List<CardType>): List<Card> {
        return probabilityGrid.map { suitMap: Map.Entry<Suit, MutableMap<Rank, CardStatus>> ->
            suitMap.value
                    .filter { rankMap -> types.contains(rankMap.value.cardType) }
                    .map { rankMap ->
                        Card(rankMap.key, suitMap.key)
                    }
        }.flatten()
    }
}
