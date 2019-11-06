package com.vango.pokertable

import com.fasterxml.jackson.annotation.JsonProperty
import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank
import com.vango.pokertable.card.Rank.ACE
import com.vango.pokertable.card.Suit
import com.vango.pokertable.player.Player
import com.vango.pokertable.WinType.*
import java.util.stream.Collectors

class PokerTable(val players: List<Player>,
                 val dealerCards: List<Card>?,
                 var winType: WinType?) {

    @JsonProperty("winners")
    fun getWinners(): List<Player> {
        val playersWithARoyalFlush = getPlayersWithStraightFlush(true)
        if (playersWithARoyalFlush.isNotEmpty()) winType = ROYAL_FLUSH
        if (playersWithARoyalFlush.size == 1)
            return playersWithARoyalFlush
        else if (playersWithARoyalFlush.size > 1)
            return getPlayerWithHighestCard(playersWithARoyalFlush)

        val playersWithStraightFlush = getPlayersWithStraightFlush(false)
        if (playersWithStraightFlush.isNotEmpty()) winType = STRAIGHT_FLUSH
        if (playersWithStraightFlush.size == 1)
            return playersWithStraightFlush
        else if (playersWithStraightFlush.size > 1)
            return getPlayerWithHighestCard(playersWithStraightFlush)

        val playersWithFourOfAKind = getPlayersWithCardsOfAKind(4)
        if (playersWithFourOfAKind.isNotEmpty()) winType = FOUR_OF_A_KIND
        if (playersWithFourOfAKind.size == 1)
            return playersWithFourOfAKind
        else if (playersWithFourOfAKind.size > 1)
            return getPlayerWithHighestCard(playersWithFourOfAKind)

        val playersWithFullHouse = getPlayersWithFullHouse()
        if (playersWithFullHouse.isNotEmpty()) winType = FULL_HOUSE
        if (playersWithFullHouse.size == 1)
            return playersWithFullHouse
        else if (playersWithFullHouse.size > 1)
            return getPlayerWithHighestCard(playersWithFullHouse)

        val playersWithFlush = getPlayersWithFlush()
        if (playersWithFlush.isNotEmpty()) winType = FLUSH
        if (playersWithFlush.size == 1)
            return playersWithFlush
        else if (playersWithFlush.size > 1)
            return getPlayerWithHighestCard(playersWithFlush)

        val playersWithStraight = getPlayersWithStraight(null)
        if (playersWithStraight.isNotEmpty()) winType = STRAIGHT
        if (playersWithStraight.size == 1)
            return playersWithStraight
        else if (playersWithStraight.size > 1)
            return getPlayerWithHighestCard(playersWithStraight)

        val playersWithThreeOfAKind = getPlayersWithCardsOfAKind(3)
        if (playersWithThreeOfAKind.isNotEmpty()) winType = THREE_OF_A_KIND
        if (playersWithThreeOfAKind.size == 1)
            return playersWithThreeOfAKind
        else if (playersWithThreeOfAKind.size > 1)
            return getPlayerWithHighestCard(playersWithThreeOfAKind)

        val playersWithTwoPair = getPlayersWithTwoPair()
        if (playersWithTwoPair.isNotEmpty()) winType = TWO_PAIR
        if (playersWithTwoPair.size == 1)
            return playersWithTwoPair
        else if (playersWithTwoPair.size > 1)
            return getPlayerWithHighestCard(playersWithTwoPair)

        val playersWithPair = getPlayersWithCardsOfAKind(2)
        if (playersWithPair.isNotEmpty()) winType = PAIR
        if (playersWithPair.size == 1)
            return playersWithPair
        else if (playersWithPair.size > 1)
            return getPlayerWithHighestCard(playersWithPair)

        winType = HIGH_CARD
        return getPlayerWithHighestCard(null)
    }

    private fun getPlayersWithTwoPair(): List<Player> {
        return players.filter { player ->
            val playersCardsInPlay = getPlayersCardsInPlay(player);
            val rankingsInPlay = playersCardsInPlay.groupingBy { card -> card.rank.ranking }.eachCount()
            rankingsInPlay.filter { rankingCount -> rankingCount.value >= 2 }.size >= 2
        }
    }

    private fun getPlayersWithStraightFlush(withAceHigh: Boolean): List<Player> {
        for (suit in Suit.values()) {
            val playersWithSuitedStraight = getPlayersWithStraight(suit)
            val playersWithStraightFlush = playersWithSuitedStraight.stream().filter { player ->
                if (withAceHigh)
                    getPlayersCardsInPlay(player).contains(Card(ACE, suit))
                else
                    !getPlayersCardsInPlay(player).contains(Card(ACE, suit))
            }.collect(Collectors.toList())
            if (playersWithStraightFlush.isNotEmpty()) return playersWithStraightFlush
        }
        return emptyList()
    }

    private fun getPlayersWithFullHouse(): List<Player> {
        return players.stream().filter { player ->
            getPlayersWithCardsOfAKind(2).contains(player) &&
                    getPlayersWithCardsOfAKind(3).contains(player)
        }.collect(Collectors.toList())
    }

    private fun getPlayersWithFlush(): List<Player> {
        return players.stream().filter { player ->
            val cardsInPlay = getPlayersCardsInPlay(player)
            val suitsInPlay = cardsInPlay.groupingBy { card -> card.suit.suit }.eachCount()
            suitsInPlay.filter { (_, v) -> v > 5 }.isNotEmpty()
        }.collect(Collectors.toList())
    }

    private fun getPlayersWithStraight(suit: Suit?): List<Player> {
        return players.stream().filter { player ->
            val cardsInPlay = getPlayersCardsInPlay(player)
            val rankingsInPlay = cardsInPlay
                    .filter { card -> if (suit != null) card.suit == suit else true }
                    .groupingBy { card -> card.rank }.eachCount()
            var i = 0
            for (r in Rank.values()) {
                if (rankingsInPlay.contains(r)) i++
                else i = 0
                if (i == 5) break
            }
            if (rankingsInPlay.contains(ACE)) i++
            i >= 5
        }.collect(Collectors.toList())
    }

    private fun getPlayersWithCardsOfAKind(ofAKind: Int): List<Player> {
        return players.stream().filter { player ->
            val cardsInPlay = getPlayersCardsInPlay(player)
            val rankingsInPlay = cardsInPlay.groupingBy { card -> card.rank.ranking }.eachCount()
            rankingsInPlay.containsValue(ofAKind)
        }.collect(Collectors.toList())
    }

    private fun getPlayerWithHighestCard(filteredPlayers: List<Player>?): List<Player> {
        val highestRank: Rank
        val players = filteredPlayers ?: players
        highestRank = players.maxBy { player -> player.getHighestCard().rank.ranking }?.getHighestCard()!!.rank
        return players.filter { player -> player.getHighestCard().rank == highestRank }
    }

    private fun getPlayersCardsInPlay(player: Player): MutableList<Card> {
        val cardsInPlay = mutableListOf<Card>()
        if (dealerCards != null) {
            cardsInPlay.addAll(dealerCards)
        }
        cardsInPlay.add(player.card1)
        cardsInPlay.add(player.card2)
        return cardsInPlay;
    }
}