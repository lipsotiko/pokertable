package com.vango.pokertable.player

import com.fasterxml.jackson.annotation.JsonIgnore
import com.vango.pokertable.card.Card

class Player(val card1: Card, val card2: Card) {

    @JsonIgnore
    fun getHighestCard(): Card {
        return if (card1.rank.ranking > card2.rank.ranking) card1 else card2
    }

    @JsonIgnore
    fun getLowestCard(): Card {
        return if (card1.rank.ranking < card2.rank.ranking) card1 else card2
    }

}