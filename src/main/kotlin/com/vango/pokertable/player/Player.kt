package com.vango.pokertable.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vango.pokertable.card.Card

open class Player(val card1: Card, val card2: Card) {

    @JsonIgnore
    fun getHighestCard(): Card {
        return if (card1.rank.ranking > card2.rank.ranking) card1 else card2
    }

    @JsonIgnore
    fun getLowestCard(): Card {
        return if (card1.rank.ranking < card2.rank.ranking) card1 else card2
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        other as Player

        if (card1 != other.card1) return false
        if (card2 != other.card2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = card1.hashCode()
        result = 31 * result + card2.hashCode()
        return result
    }

}