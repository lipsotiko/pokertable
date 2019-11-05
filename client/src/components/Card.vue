<template>
  <div class="card">
    <div class="rank">
      <p v-if="rank === 'TWO'">2</p>
      <p v-if="rank === 'THREE'">3</p>
      <p v-if="rank === 'FOUR'">4</p>
      <p v-if="rank === 'FIVE'">5</p>
      <p v-if="rank === 'SIX'">6</p>
      <p v-if="rank === 'SEVEN'">7</p>
      <p v-if="rank === 'EIGHT'">8</p>
      <p v-if="rank === 'NINE'">9</p>
      <p v-if="rank === 'TEN'">10</p>
      <p v-if="rank === 'JACK'">J</p>
      <p v-if="rank === 'QUEEN'">Q</p>
      <p v-if="rank === 'KING'">K</p>
      <p v-if="rank === 'ACE'">A</p>
    </div>
    <div class="suit">
      <p v-if="suit === 'DIAMOND'" style="color: red">&diams;</p>
      <p v-if="suit === 'CLUB'" style="color: black">&clubs;</p>
      <p v-if="suit === 'HEART'" style="color: red">&hearts;</p>
      <p v-if="suit === 'SPADE'" style="color: black">&spades;</p>
    </div>
  </div>
</template>
<style scoped>
.card {
  display: flex;
  border: 1px solid black;
  border-radius: 4px;
  width: 48px;
  height: 68px;
  padding-top: 2px;
  margin: 2px;
}
.rank {
  padding-left: 8px;
}

.suit {
  padding-left: 5px;
}
</style>
<script>
export default {
  name: "Card",
  props: ["rank", "suit"],
  methods: {}
};
</script>