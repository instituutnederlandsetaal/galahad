<template>
    <!-- Links:
    - https://vuejs.org/style-guide/
    - https://vuejs.org/api/sfc-script-setup.html
    - https://vuejs.org/guide/typescript/composition-api.html -->

    <!-- Attribute order: v-for/v-if; id; ref/key; v-model; others...; @click/@etc. -->
    <button v-if="visible" v-model="foo" class="btn" @click="handleClick">Button</button>
</template>

<script setup lang="ts">
// -- libraries --
// --- stores ---
// --- api ---
// --- views ---
// --- types ---
// --- model ---
const model = defineModel<string>()
// --- props ---
const { foo = "bar" } = defineProps<{
    foo?: string
}>()
// --- emits ---
// Defining emits is technically optional if they are not used in <script>, but it is a good practice to do so.
const emit = defineEmits<{
    click: [name: string]
    voidExample: []
}>()
// --- data ---
const visible = ref<boolean>(true)
// --- computed ---
const isVisible = computed<boolean>(() => visible.value) // arrow function where possible
// --- watch ---
// --- lifecycle (in order of execution) ---
// --- methods ---
function handleClick(): void {
    // do something
}
</script>

<style scoped lang="scss">
/**
 * Avoid html element as selectors and use classes instead.
 */
.btn {
    background-color: green;
}
</style>
