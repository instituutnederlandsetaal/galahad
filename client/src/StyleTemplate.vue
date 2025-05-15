<template>
    <!-- Links:
    - https://vuejs.org/style-guide/
    - https://vuejs.org/api/sfc-script-setup.html
    - https://vuejs.org/guide/typescript/composition-api.html -->

    <!-- Attribute order: v-for/v-if; id; ref/key; v-model; others...; @click/@etc. -->
    <button v-if="visible" class="btn" @click="handleClick">Button</button>
</template>

<script setup lang="ts">
// library imports
import { computed, ref } from 'vue';
// components imports
// props
// emits
// data
const visible = ref<boolean>(true)
// computed
const isVisible = computed<boolean>(() => visible.value) // arrow function where possible
// watch
// lifecycle (in order of execution)
// methods
</script>

<style scoped lang="scss">
/**
 * Avoid html element as selectors and use classes instead. 
 */
.btn {
    background-color: green;
}
</style>
