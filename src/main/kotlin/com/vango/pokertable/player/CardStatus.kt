package com.vango.pokertable.player

import com.vango.pokertable.card.Card

class CardStatus(card: Card, private var available: Boolean, var cardType: CardType) : Card(card.rank, card.suit) {

    fun available(): Boolean {
        return available
    }

    fun unavailable() {
        available = false
    }

    fun setType(cardType: CardType) {
        this.cardType = cardType
    }
}
