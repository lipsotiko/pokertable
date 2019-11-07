package com.vango.pokertable.player

import com.vango.pokertable.card.Card

class CardStatus(card: Card, var cardType: CardType) : Card(card.rank, card.suit) {

    fun setType(cardType: CardType) {
        this.cardType = cardType
    }
}
