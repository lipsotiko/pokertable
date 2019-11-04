package com.vango.pokertable

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class PokerTableController {

    @PostMapping("/api/evaluate")
    fun evaluateHands(@RequestBody pokerTable: PokerTable): PokerTable {
        return pokerTable;
    }
}
