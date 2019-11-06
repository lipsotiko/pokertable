package com.vango.pokertable

import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Rank.*
import com.vango.pokertable.card.Suit.*
import com.vango.pokertable.player.Player
import com.vango.pokertable.WinType.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PokerTableTest {

    @Test
    fun high_card_wins() {
        val player1 = Player(Card(ACE, CLUB), Card(TWO, CLUB))
        val player2 = Player(Card(THREE, CLUB), Card(FOUR, CLUB))
        val player3 = Player(Card(KING, CLUB), Card(FIVE, CLUB))
        val table = PokerTable(listOf(player1, player2, player3), emptyList(), null)
        assertEquals(player1, table.getWinners()[0])
        assertEquals(HIGH_CARD, table.winType)
    }

    @Test
    fun high_card_ties() {
        val player1 = Player(Card(THREE, CLUB), Card(FOUR, CLUB))
        val player2 = Player(Card(THREE, SPADE), Card(FOUR, DIAMOND))
        val player3 = Player(Card(TWO, CLUB), Card(THREE, SPADE))
        val table = PokerTable(listOf(player1, player2, player3), emptyList(), null)
        val winners = table.getWinners()
        assertEquals(2, winners.size)
        assertEquals(FOUR, winners[0].card2.rank)
        assertEquals(HIGH_CARD, table.winType)
    }

    @Test
    fun pair_wins() {
        val player1 = Player(Card(ACE, CLUB), Card(TWO, CLUB))
        val player2 = Player(Card(THREE, DIAMOND), Card(FIVE, DIAMOND))
        val table = PokerTable(listOf(player1, player2), listOf(Card(TWO, HEART)), null)
        assertEquals(player1, table.getWinners()[0])
        assertEquals(PAIR, table.winType)
    }

    @Test
    fun two_pair_wins() {
        val player1 = Player(Card(SIX, DIAMOND), Card(FIVE, SPADE))
        val player2 = Player(Card(SEVEN, CLUB), Card(SEVEN, HEART))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(Card(SIX, HEART), Card(FIVE, DIAMOND)), null)
        assertEquals(1, table.getWinners().size)
        assertEquals(player1, table.getWinners()[0])
        assertEquals(TWO_PAIR, table.winType)
    }

    @Test
    fun two_pair_ties() {
        val player1 = Player(Card(SIX, DIAMOND), Card(FIVE, SPADE))
        val player2 = Player(Card(SIX, CLUB), Card(FIVE, HEART))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(Card(SIX, HEART), Card(FIVE, DIAMOND)), null)
        assertEquals(2, table.getWinners().size)
        assertEquals(TWO_PAIR, table.winType)
    }

    @Test
    fun three_of_a_kind_wins() {
        val player1 = Player(Card(TWO, CLUB), Card(TWO, HEART))
        val player2 = Player(Card(THREE, DIAMOND), Card(FOUR, SPADE))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(Card(THREE, HEART), Card(FOUR, DIAMOND), Card(TWO, SPADE)), null)
        assertEquals(player1, table.getWinners()[0])
        assertEquals(THREE_OF_A_KIND, table.winType)
    }

    @Test
    fun straight_wins() {
        val player1 = Player(Card(TWO, CLUB), Card(THREE, HEART))
        val player2 = Player(Card(THREE, DIAMOND), Card(THREE, SPADE))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(THREE, HEART),
                Card(FOUR, DIAMOND),
                Card(FIVE, SPADE),
                Card(SIX, SPADE)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(STRAIGHT, table.winType)
    }

    @Test
    fun straight_with_low_ace_wins() {
        val player1 = Player(Card(ACE, CLUB), Card(TWO, HEART))
        val player2 = Player(Card(THREE, DIAMOND), Card(THREE, SPADE))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(THREE, HEART),
                Card(FOUR, DIAMOND),
                Card(FIVE, SPADE),
                Card(JACK, SPADE)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(STRAIGHT, table.winType)
    }

    @Test
    fun flush_wins() {
        val player1 = Player(Card(KING, SPADE), Card(JACK, SPADE))
        val player2 = Player(Card(TWO, CLUB), Card(THREE, HEART))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(Card(TWO, SPADE), Card(TEN, SPADE), Card(FIVE, SPADE), Card(SIX, SPADE)), null)
        assertEquals(player1, table.getWinners()[0])
        assertEquals(FLUSH, table.winType)
    }

    @Test
    fun full_house_wins() {
        val player1 = Player(Card(TWO, CLUB), Card(TWO, HEART))
        val player2 = Player(Card(ACE, CLUB), Card(TWO, SPADE))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(THREE, SPADE),
                Card(FOUR, SPADE),
                Card(FIVE, SPADE),
                Card(FIVE, DIAMOND),
                Card(FIVE, HEART)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(FULL_HOUSE, table.winType)
    }

    @Test
    fun four_of_a_kind_wins() {
        val player1 = Player(Card(ACE, DIAMOND), Card(ACE, SPADE))
        val player2 = Player(Card(TWO, CLUB), Card(TWO, HEART))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(ACE, CLUB),
                Card(ACE, HEART),
                Card(FIVE, SPADE),
                Card(FIVE, DIAMOND),
                Card(FIVE, HEART)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(FOUR_OF_A_KIND, table.winType)
    }

    @Test
    fun straight_flush_wins() {
        val player1 = Player(Card(TWO, CLUB), Card(THREE, CLUB))
        val player2 = Player(Card(KING, DIAMOND), Card(KING, SPADE))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(KING, CLUB),
                Card(KING, HEART),
                Card(FOUR, CLUB),
                Card(FIVE, CLUB),
                Card(SIX, CLUB)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(STRAIGHT_FLUSH, table.winType)
    }

    @Test
    fun royal_flush_wins() {
        val player1 = Player(Card(ACE, DIAMOND), Card(KING, DIAMOND))
        val player2 = Player(Card(TWO, DIAMOND), Card(KING, DIAMOND))
        val players: List<Player> = listOf(player1, player2)
        val table = PokerTable(players, listOf(
                Card(QUEEN, DIAMOND),
                Card(JACK, DIAMOND),
                Card(TEN, DIAMOND),
                Card(NINE, DIAMOND),
                Card(FIVE, DIAMOND)
        ), null
        )
        assertEquals(player1, table.getWinners()[0])
        assertEquals(ROYAL_FLUSH, table.winType)
    }
}
