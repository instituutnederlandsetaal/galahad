<template>
    <GTabs class="level-2" basePath="/application" :tabs="[
        { id: 'about', title: 'About' },
        { id: 'api', title: 'API', stub: true },
    ]">
        <template #api-title>
            <ExternalLink class="blockLink" :href="apiUrl">API</ExternalLink>
        </template>
    </GTabs>
</template>

<script setup lang="ts">
const apiUrl = `${window.location.protocol}//${window.location.hostname}/galahad/api/swagger-ui/index.html`
</script>

<style scoped lang="scss">
/* Makes block links full size in tab header */
.blockLink {
    display: block;
    margin: 0 -20px;
}
</style>
