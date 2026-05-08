<template>
    <!-- Links:
    - https://vuejs.org/style-guide/
    - https://vuejs.org/api/sfc-script-setup.html
    - https://vuejs.org/guide/typescript/composition-api.html -->

    <!-- Attribute order: v-for/v-if; id; ref/key; v-model; others...; @click/@etc. -->
    <!-- within others, boolean arguments first, same-name linked props second, the rest third. -->
    <button v-if="visible" v-model="foo" primary bordered :color class="btn" @click="handleClick">Button</button>
</template>

<script setup lang="ts">
// -- libraries --
import axios from "axios"
// --- stores ---
import stores from "@/stores"
// --- api ---
import * as API from "@/api"
// --- util ---
import { formatDate } from "@/ts/utils"
// --- views ---
import HelpView from "@/views/HelpView.vue"

// --- types ---
type myType = { id: number; name: string }

// --- model ---
const model = defineModel<string>()

// --- ref ---
const metricsFilter = useTemplateRef<InstanceType<typeof MetricsFilter>>("metricsFilter")

// --- props ---
const { foo = "bar" } = defineProps<{ foo?: string }>()

// --- emits ---
// Defining emits is technically optional if they are not used in <script>, but it is a good practice to do so.
const emit = defineEmits<{ click: [name: string]; voidExample: [] }>()

// --- store data ---
const store = stores.useMyStore()

// --- data ---
const visible = ref<boolean>(true)

// --- computed ---
const isVisible = computed<boolean>(() => visible.value) // arrow function where possible

// --- watch ---
watch(
    () => store.someValue,
    (newValue, oldValue) => {
        console.log(`Value changed from ${oldValue} to ${newValue}`)
    },
)

// --- lifecycle (in order of execution) ---
onMounted((): void => {
    console.log("Component mounted")
})

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
