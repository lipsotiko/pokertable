<template>
  <div id="app">
    <div>
      <h1>Vango's Poker Table</h1>
    </div>
    <input v-model="numberOfPlayers" type="number" placeholder="Input number of players" />
    <input v-if="dealerCards.length < 5" type="button" value="Deal Cards" @click="dealCards()" />
    <input v-else type="button" value="Reset" @click="restCards()" />
    <h2 v-if="players.length > 0">Player's Hands</h2>
    <ul>
      <li v-for="player in players" :key="player.id" :class="cardClass(player.isWinner)">
        <p class="player-id">P {{player.id}}</p>
        <card :rank="player.card1.rank" :suit="player.card1.suit" />
        <card :rank="player.card2.rank" :suit="player.card2.suit" />
      </li>
    </ul>
    <h2 v-if="dealerCards.length > 0">Dealer's Cards</h2>
    <ul>
      <li v-for="card in dealerCards" :key="card.id" class="card">
        <card :rank="card.rank" :suit="card.suit" />
      </li>
    </ul>
    <h2 v-if="dealerCards.length === 5">Winning Hand</h2>
    <p>{{winningHand}}</p>
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
      winningHand: null
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
      } else if (this.players.length > 0 && this.dealerCards.length === 0) {
        //deal the flop
        for (let i = 0; i < 3; i++) {
          this.dealerCards.push(this.cards[this.cardIndex++]);
        }
      } else if (this.dealerCards.length === 3) {
        //deal the turn
        this.dealerCards.push(this.cards[this.cardIndex++]);
      } else if (this.dealerCards.length === 4) {
        //deal the river
        this.dealerCards.push(this.cards[this.cardIndex++]);

        //post to server to evaluate hands and find winner
        const pokerTableResults = await this.evaluatePokerTable();
        this.assignWinners(pokerTableResults);
      }
    },
    async restCards() {
      await this.getCards();
      this.cardIndex = 0;
      this.numberOfPlayers = null;
      this.dealerCards = [];
      this.players = [];
      this.winningHand = null;
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
    assignWinners(pokerTableResults) {
      this.winningHand = pokerTableResults.winType;
      this.players = this.players.map(player => {
        player.isWinner =
          pokerTableResults.winners.filter(
            winner =>
              player.card1.suit === winner.card1.suit &&
              player.card1.rank === winner.card1.rank &&
              player.card2.suit === winner.card2.suit &&
              player.card2.rank === winner.card2.rank
          ).length > 0;
        return player;
      });
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

