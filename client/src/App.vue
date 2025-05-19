<template>
    <GTabs
        ref="tabs"
        class="level-1"
        basePath=""
        :tabs="[
            { id: 'annotate', title: 'Annotate & Evaluate' },
            { id: 'overview', title: 'Taggers & Datasets' },
            { id: 'user', title: 'User' },
        ]"
    >
        <template #title>
            <div class="title">
                <a href="https://ivdnt.org/" target="_blank" tabindex="-1" rel="noopener noreferrer">
                    / instituut voor de Nederlandse&nbsp;taal&nbsp;/
                </a>
                <a href="/galahad/home" tabindex="-1">galahad</a>
            </div>
        </template>

        <template #tabs-start>
            <a href="/galahad/home">
                <img class="painting" src="@/assets/galahad-graal-klein.png" />
            </a>
        </template>

        <template #tabs-end>
            <!-- If we ever decide to make these links open in the same tab, this is how:
                <GNav :route="{ path: '/help' }">Help</GNav>
            -->
            <a href="/galahad/help" target="_blank">help</a>
            <a href="/galahad/application" target="_blank">about</a>
            <a href="/galahad/contribute" target="_blank">contribute</a>
            <a href="https://portal.clarin.ivdnt.org/lancelot" target="_blank">lancelot</a>
        </template>
    </GTabs>

    <GModal :show="app.errors.length > 0" title="Ocharme!" small noHelp @hide="app.resetErrors">
        Please try again or contact
        <MailAddress /> for support.
        <GInfo v-for="(error, index) in app.errors" :key="index" error>{{ error }}</GInfo>
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores
import { onMounted, ref } from "vue"
import stores, { AppStore, UserStore } from "@/stores"
// Components
import { GInfo, GModal, GTabs, MailAddress } from "@/components"

// Stores
const app = stores.useApp() as AppStore
const user = stores.useUser() as UserStore

onMounted(() => {
    user.fetchUser()
})

// there is interference with other router changes TODO: figure this out
// actually the jobSelection is now done on EvaluationView mount
const tabs = ref(null)
</script>

<style lang="scss">
@use "sass:color";

@font-face {
    font-family: "Schoolboek";
    src: url("@/assets/Schoolboek-Regular.woff") format("woff");
}

$galahad-theme: #62b6ff;

:root:root {
    // INT theme colors
    //blue
    --int-blue: #359ff0;
    --int-blue-hover: #1188e4;
    //yellow
    --int-yellow: #fff064;
    --int-yellow-lighter: #fff7ad;
    --int-yellow-hover: #ffeb33;
    --int-yellow-active: #d5c000;
    --int-yellow-outline: #ffe100; // custom; a bit darker than huisstijl yellow-outline
    // red
    --int-red: #e8503d;
    --int-red-hover: #dd442f; // custom
    --int-red-active: #d72f19; // made original hover into active
    //orange
    --int-orange: #ff8000;
    --int-orange-hover: #df7000;
    --int-orange-active: #cc6600; // made original hover into active
    //green
    --int-green: #89c24b;
    --int-green-hover: #79b246;
    --int-green-active: #6fa338; // made original hover into active
    // INT monochrome
    --black: black;
    --int-very-light-grey: #f3f3f3;
    --int-very-light-grey-hover: #d9d9d9;
    --int-light-grey: #cccdcc;
    --int-light-grey-hover: #b2b3b2;
    --int-grey: #a4a5a8;
    --int-grey-hover: #8a8b8f;
    --white: white;
    // Current Theme
    --int-theme: #{$galahad-theme};
    --int-theme-lighter: #{color.adjust($galahad-theme, $lightness: 10%)};
    --int-theme-hover: #{color.adjust($galahad-theme, $lightness: -7%)};
    --int-theme-active: #{color.adjust($galahad-theme, $lightness: -20%)};
    --int-theme-outline: #{color.adjust($galahad-theme, $lightness: -15%)};
    // --p-form-field-background: #E8503D;
    --p-multiselect-background: rgb(233, 233, 237);
    --p-multiselect-border-radius: 0;
    --p-primary-color: #{$galahad-theme};
    --p-checkbox-icon-checked-color: black;
    --p-checkbox-border-radius: 0;
    --p-highlight-background: var(--int-theme-lighter);
}

#app .p-multiselect {
    border: none;

    &:hover {
        background-color: var(--int-very-light-grey-hover);
    }
}

.p-multiselect-header {
    border-bottom: 1px solid var(--int-light-grey);
}

.p-multiselect-header::after {
    content: "select all";
}

h1,
h2,
h3,
h4,
h5 {
    font-family: Schoolboek, Helvetica, sans-serif;
    font-weight: normal;
    font-style: normal;
}

.textcolor {
    color: var(--black);
}

ul li {
    list-style: square;
}

ol li {
    list-style-type: decimal;
}

/* link */
*:not(.header) a:link {
    color: #000;
    text-decoration: underline;
    font-style: italic;

    &:hover {
        color: #000;
        text-decoration: none;
    }

    &:active {
        color: #000;
    }
}

body {
    padding: 0;
    margin: 0;
    overflow-x: hidden;
    overflow-y: hidden;
}

html {
    font-family: Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: var(--black);
}

#app {
    text-align: left;
    background-color: var(--int-very-light-grey);
    line-height: 1.5;
    padding: 0;
    margin: 0;
    height: 100vh;
}

.title {
    // Make some space for help, about, contribute.
    padding-right: 0px;
}

@media (max-width: 800px) or (max-height: 700px) {
    #app {
        overflow: auto;
    }

    .tabs.level-1 {
        height: initial !important;
        min-height: 100% !important;

        & > .header {
            position: initial;
        }
    }
}

@media (max-width: 870px) {
    .tabs.level-1 .header .tabs-start {
        display: none;
    }

    .tabs > .content {
        padding: 10px 0 0 0 !important;
    }
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.25s linear;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

// Awesome font icons
i.fa {
    font-size: 1.3em;
}
</style>
