<template>
    <GTabs class="level-2" basePath="/application" :tabs="[
        { id: 'about', title: 'About' },
        { id: 'api', title: 'API' },
    ]">
        <template #api-title>
            <ExternalLink class="nav-link" :href="apiUrl">API</ExternalLink>
        </template>
    </GTabs>
</template>

<script setup lang="ts">
const apiUrl = `${window.location.protocol}//${window.location.hostname}/galahad/api/swagger-ui/index.html`
</script>
