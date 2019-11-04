package com.vango.pokertable

import com.fasterxml.jackson.annotation.JsonProperty
import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank
import com.vango.pokertable.card.Rank.*
import com.vango.pokertable.card.Suit
import com.vango.pokertable.player.Player
import java.util.stream.Collectors

class PokerTable(val players: List<Player>,
                 val dealerCards: List<Card>?) {

    @JsonProperty("winners")
    fun getWinners(): List<Player> {
        val playersWithARoyalFlush = getPlayersWithStraightFlush(true)
        if (playersWithARoyalFlush.size == 1)
            return playersWithARoyalFlush

        val playersWithStraightFlush = getPlayersWithStraightFlush(false)
        if (playersWithStraightFlush.size == 1)
            return playersWithStraightFlush

        val playersWithFourOfAKind = getPlayersWithCardsOfAKind(4)
        if (playersWithFourOfAKind.size == 1)
            return playersWithFourOfAKind

        val playersWithFullHouse = getPlayersWithFullHouse()
        if (playersWithFullHouse.size == 1)
            return playersWithFullHouse

        val playersWithFlush = getPlayersWithFlush()
        if (playersWithFlush.size == 1)
            return playersWithFlush

        val playersWithStraight = getPlayersWithStraight(null)
        if (playersWithStraight.size == 1)
            return playersWithStraight

        val playersWithThreeOfAKind = getPlayersWithCardsOfAKind(3)
        if (playersWithThreeOfAKind.size == 1)
            return playersWithThreeOfAKind

        val playersWithPair = getPlayersWithCardsOfAKind(2)
        if (playersWithPair.size == 1)
            return playersWithPair

        return getPlayerWithHighestCard()
    }

    private fun getPlayersWithStraightFlush(withAceHigh: Boolean): List<Player>{
        for(suit in Suit.values()) {
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
            suitsInPlay.filter { (k, v) -> v > 5 }.isNotEmpty()
        }.collect(Collectors.toList())
    }

    private fun getPlayersWithStraight(suit: Suit?): List<Player> {
        return players.stream().filter { player ->
            val cardsInPlay = getPlayersCardsInPlay(player)
            val rankingsInPlay = cardsInPlay
                    .filter { card -> if (suit != null) card.suit == suit else true}
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

    private fun getPlayerWithHighestCard(): List<Player> {
        val highestRank: Rank
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