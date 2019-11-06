package com.vango.pokertable

import com.vango.pokertable.card.Card
import com.vango.pokertable.card.Deck
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PokerTableController(val deck: Deck) {

    @PostMapping("/api/evaluate")
    fun evaluateHands(@RequestBody pokerTable: PokerTable): PokerTable {
        pokerTable.evaluateWinners()
        return pokerTable
    }

    @GetMapping("/api/cards")
    fun getDeckOfCards(): List<Card> {
        return deck.getDeckOfCards();
    }
}
