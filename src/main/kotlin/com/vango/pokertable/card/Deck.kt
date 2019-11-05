package com.vango.pokertable.card

import org.springframework.stereotype.Service

@Service
class Deck {

    fun getDeckOfCards(): List<Card> {
        val deckOfCards = mutableListOf<Card>()
        for (suit in Suit.values()) {
            for (rank in Rank.values()) {
                deckOfCards.add(Card(rank, suit))
            }
        }
        deckOfCards.shuffle();
        return deckOfCards;
    }
}
