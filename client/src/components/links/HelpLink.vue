<template>
    <ExternalLink :href>
        Go to <i>{{ subject }}</i> in the manual
    </ExternalLink>
</template>

<script setup lang="ts">
import type { HelpLink } from "@/types/ui/help"

const { helpLink } = defineProps<{
    helpLink: HelpLink | string
}>()

const href =
    typeof helpLink === "string"
        ? `/galahad/help/general#${helpLink}`
        : helpLink.href
const subject = typeof helpLink === "string" ? helpLink : helpLink.subject
</script>
