<template>
  <div id="app">
    <div>
      <h1>Vango's Poker Table</h1>
    </div>
    <input v-model="numberOfPlayers" type="number" placeholder="Input number of players" />
    <input v-if="dealerCards.length < 5" type="button" :value="dealButtonText" @click="dealCards()" />
    <input type="button" value="Reset" @click="restCards()" />
    <h2 v-if="players.length > 0">Player's Hands</h2>
    <ul v-if="pokerTableResults">
      <li
        v-for="result in pokerTableResults.playerWinProbabilities"
        :key="result.id"
        :class="cardClass(result.overallProbability === 100 && result.bestWinType === winType && dealerCards.length === 5)"
      >
        <p class="player-id">P {{result.id}}</p>
        <br />
        <p>{{result.overallProbability}}</p>
        <card :rank="result.card1.rank" :suit="result.card1.suit" />
        <card :rank="result.card2.rank" :suit="result.card2.suit" />
      </li>
    </ul>
    <h2 v-if="dealerCards.length > 0">Dealer's Cards</h2>
    <ul>
      <li v-for="card in dealerCards" :key="card.id" class="card">
        <card :rank="card.rank" :suit="card.suit" />
      </li>
    </ul>
    <h2 v-if="dealerCards.length === 5">Winning Hand</h2>
    <p>{{winType}}</p>
    <hr />
    <a class="git-url" href="https://github.com/lipsotiko/pokertable">Git</a>
  </div>
</template>

<style scoped>
.card {
  display: inline-flex;
  margin: 5px;
  padding: 5px;
}

.winner {
  border: 4px solid rgb(19, 167, 19);
  border-radius: 4px;
}

.player-id {
  margin: "5px";
}
</style>

<script>
import Card from "./components/Card";

export default {
  name: "app",
  components: {
    card: Card
  },
  data() {
    return {
      numberOfPlayers: null,
      cards: null,
      players: [],
      cardIndex: 0,
      dealerCards: [],
      winType: null,
      dealButtonText: "Deal Cards",
      pokerTableResults: null
    };
  },
  async created() {
    await this.getCards();
  },
  methods: {
    async dealCards() {
      let numberOfPlayers = parseInt(this.numberOfPlayers);

      if (this.players.length === 0 && this.dealerCards.length == 0) {
        //deal the first two cards to each player
        for (let i = 0; i < numberOfPlayers; i++) {
          this.players.push({ card1: this.cards[this.cardIndex++], id: i });
        }

        for (let i = 0; i < this.players.length; i++) {
          this.players[i].card2 = this.cards[this.cardIndex++];
        }

        this.pokerTableResults = await this.evaluatePokerTable();
        this.dealButtonText = "Deal Flop";
      } else if (this.players.length > 0 && this.dealerCards.length === 0) {
        //deal the flop
        for (let i = 0; i < 3; i++) {
          this.dealerCards.push(this.cards[this.cardIndex++]);
        }
        this.pokerTableResults = await this.evaluatePokerTable();
        this.dealButtonText = "Deal Turn";
      } else if (this.dealerCards.length === 3) {
        //deal the turn
        this.dealerCards.push(this.cards[this.cardIndex++]);
        this.pokerTableResults = await this.evaluatePokerTable();
        this.dealButtonText = "Deal River";
      } else if (this.dealerCards.length === 4) {
        //deal the river
        this.dealerCards.push(this.cards[this.cardIndex++]);

        //post to server to evaluate hands and find winner
        this.pokerTableResults = await this.evaluatePokerTable();
        this.winType = this.pokerTableResults.winType;
      }
    },
    async restCards() {
      await this.getCards();
      this.cardIndex = 0;
      this.numberOfPlayers = null;
      this.dealerCards = [];
      this.players = [];
      this.winType = null;
      this.dealButtonText = "Deal Cards";
      this.pokerTableResults = null;
    },
    async getCards() {
      let cardsResponse = await fetch("/api/cards");
      this.cards = await cardsResponse.json();
      this.cards.map((card, index) => (card.id = index));
    },
    async evaluatePokerTable() {
      const pokerTableResponse = await fetch("/api/evaluate", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          players: this.players,
          dealerCards: this.dealerCards
        })
      });

      const pokerTableResults = await pokerTableResponse.json();
      return pokerTableResults;
    },
    cardClass: function(isWinner) {
      return {
        card: true,
        winner: isWinner
      };
    }
  }
};
</script>

