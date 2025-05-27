<template>
    <table class="key-values">
        <tbody>
            <tr v-for="key in keys" :key>
                <td class="key">{{ key }}</td>
                <td>
                    {{ data[key] }}
                </td>
            </tr>
        </tbody>
    </table>
</template>

<script setup lang="ts">
const { data } = defineProps<{
    data: Record<string, string>
}>()
const keys = Object.keys(data)
</script>

<style scoped lang="scss">
.key-values {
    font-family: monospace;

    .key {
        padding-right: 2rem;
    }
}
</style>
