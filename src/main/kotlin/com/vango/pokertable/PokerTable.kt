package com.vango.pokertable

import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank
import com.vango.pokertable.card.Rank.ACE
import com.vango.pokertable.card.Suit
import com.vango.pokertable.player.Player
import com.vango.pokertable.WinType.*
import com.vango.pokertable.player.CardType.*
import java.util.stream.Collectors

class PokerTable(private val players: List<Player>,
                 private val dealerCards: List<Card>) {

    var winType: WinType? = null
    var playerResults: List<Player> = listOf()
    var playerWinProbabilities: MutableList<PlayerWinProbability> = mutableListOf()

    fun winType(): WinType? {
        return winType
    }

    fun playerResults(): List<Player> {
        return playerResults
    }

    fun evaluate() {
        players.forEach { player ->
            val playerProbability = PlayerWinProbability(player)
            players.forEach { player2 ->
                playerProbability.updateAvailability(player2.card1, OTHER_PLAYERS)
                playerProbability.updateAvailability(player2.card2, OTHER_PLAYERS)
            }
            playerProbability.updateAvailability(player.card1, PLAYER)
            playerProbability.updateAvailability(player.card2, PLAYER)
            dealerCards.forEach { card -> playerProbability.updateAvailability(card, DEALER) }
            playerProbability.calculateProbabilities()
            playerWinProbabilities.add(playerProbability)
        }

        val playersWithARoyalFlush = playerWinProbabilities.filter { p -> p.winTypeProbabilities[ROYAL_FLUSH] == 100 }
        if (playersWithARoyalFlush.isNotEmpty()) winType = ROYAL_FLUSH
        if (playersWithARoyalFlush.size == 1)
            playerResults = playersWithARoyalFlush
        else if (playersWithARoyalFlush.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithARoyalFlush)

        if (playerResults.isNotEmpty()) return

        val playersWithStraightFlush = getPlayersWithStraightFlush(false)
        if (playersWithStraightFlush.isNotEmpty()) winType = STRAIGHT_FLUSH
        if (playersWithStraightFlush.size == 1)
            playerResults = playersWithStraightFlush
        else if (playersWithStraightFlush.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithStraightFlush)

        if (playerResults.isNotEmpty()) return

        val playersWithFourOfAKind = getPlayersWithCardsOfAKind(4)
        if (playersWithFourOfAKind.isNotEmpty()) winType = FOUR_OF_A_KIND
        if (playersWithFourOfAKind.size == 1)
            playerResults = playersWithFourOfAKind
        else if (playersWithFourOfAKind.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithFourOfAKind)

        if (playerResults.isNotEmpty()) return

        val playersWithFullHouse = getPlayersWithFullHouse()
        if (playersWithFullHouse.isNotEmpty()) winType = FULL_HOUSE
        if (playersWithFullHouse.size == 1)
            playerResults = playersWithFullHouse
        else if (playersWithFullHouse.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithFullHouse)

        if (playerResults.isNotEmpty()) return

        val playersWithFlush = getPlayersWithFlush()
        if (playersWithFlush.isNotEmpty()) winType = FLUSH
        if (playersWithFlush.size == 1)
            playerResults = playersWithFlush
        else if (playersWithFlush.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithFlush)

        if (playerResults.isNotEmpty()) return

        val playersWithStraight = getPlayersWithStraight(null)
        if (playersWithStraight.isNotEmpty()) winType = STRAIGHT
        if (playersWithStraight.size == 1)
            playerResults = playersWithStraight
        else if (playersWithStraight.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithStraight)

        if (playerResults.isNotEmpty()) return

        val playersWithThreeOfAKind = getPlayersWithCardsOfAKind(3)
        if (playersWithThreeOfAKind.isNotEmpty()) winType = THREE_OF_A_KIND
        if (playersWithThreeOfAKind.size == 1)
            playerResults = playersWithThreeOfAKind
        else if (playersWithThreeOfAKind.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithThreeOfAKind)

        if (playerResults.isNotEmpty()) return

        val playersWithTwoPair = getPlayersWithTwoPair()
        if (playersWithTwoPair.isNotEmpty()) winType = TWO_PAIR
        if (playersWithTwoPair.size == 1)
            playerResults = playersWithTwoPair
        else if (playersWithTwoPair.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithTwoPair)

        if (playerResults.isNotEmpty()) return

        val playersWithPair = getPlayersWithCardsOfAKind(2)
        if (playersWithPair.isNotEmpty()) winType = PAIR
        if (playersWithPair.size == 1)
            playerResults = playersWithPair
        else if (playersWithPair.size > 1)
            playerResults = getPlayerWithHighestCard(playersWithPair)

        if (playerResults.isNotEmpty()) return

        winType = HIGH_CARD
        playerResults = getPlayerWithHighestCard(players)
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
        var highestRank = filteredPlayers?.maxBy { player -> player.getHighestCard().rank.ranking }?.getHighestCard()!!.rank
        var playersWithHighestRank = filteredPlayers.filter { player -> player.getHighestCard().rank == highestRank }
        if (playersWithHighestRank.size == 1) return playersWithHighestRank

        highestRank = playersWithHighestRank.maxBy { player -> player.getLowestCard().rank.ranking }!!.getLowestCard().rank
        playersWithHighestRank = filteredPlayers.filter { player -> player.getLowestCard().rank == highestRank }
        if (playersWithHighestRank.size == 1) return playersWithHighestRank

        return playersWithHighestRank;
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