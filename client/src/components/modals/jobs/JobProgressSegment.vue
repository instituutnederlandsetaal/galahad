<template>
    <div class="segment" :style>
        <template v-if="value > 0">
            {{ text }}
        </template>
    </div>
</template>

<script setup lang="ts">
const { color, label, value, total } = defineProps<{
	color: string
	label: string
	value: number
	total: number
}>()

const text = computed(
	() => `${label}: ${value} ${value == 1 ? "document" : "documents"}`,
)

const style = computed(() => ({
	width: `${(100 * value) / total}%`,
	backgroundColor: color,
}))
</script>

<style scoped lang="scss">
.segment {
    display: inline-block;
    text-align: center;
    white-space: nowrap;
    overflow: clip;
    transition: width 1s;
}
</style>
