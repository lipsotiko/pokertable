package com.vango.pokertable.card

open class Card(val rank: Rank, val suit: Suit) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (rank != other.rank) return false
        if (suit != other.suit) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rank.hashCode()
        result = 31 * result + suit.hashCode()
        return result
    }
}