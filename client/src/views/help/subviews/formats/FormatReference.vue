<template>
    <div>
        <h4>Reference:</h4>
        <ExternalLink :href>
            <slot></slot>
        </ExternalLink>
    </div>
</template>
<script setup lang="ts">
const { href } = defineProps<{ href: string }>()
</script>
<style scoped lang="scss">
h4 {
    display: inline-block;
    margin: 0 0.3rem 0 0;
}
</style>
