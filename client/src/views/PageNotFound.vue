<template>
    <GCard title="Error 404">
        <GInfo error>
            <p>We could not find the page you were looking for.</p>
            <GNav :route="{ path: '/' }">Go back to the homepage</GNav>
        </GInfo>
    </GCard>
</template>

<script setup lang="ts"></script>

<style scoped lang="scss">
.g-card {
    overflow-y: auto;
}
</style>
