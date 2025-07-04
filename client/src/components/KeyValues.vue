<template>
    <table>
        <tbody>
            <tr v-for="key in keys" :key>
                <td>{{ key }}</td>
                <td>{{ data[key] }}</td>
            </tr>
        </tbody>
    </table>
</template>

<script setup lang="ts">
// #props
const { data } = defineProps<{ data: Record<string, string> }>()

// #data
const keys = Object.keys(data)
</script>

<style scoped lang="scss">
table {
    font-family: monospace;

    td:first-child {
        padding-right: 2rem;
    }
}
</style>
