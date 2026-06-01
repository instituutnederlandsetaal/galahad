<template>
    <div role="progressbar" class="progress">
        <JobProgressBarSegment
            label="failed"
            color="var(--int-red)"
            :total="job.progress.total"
            :value="job.progress.failed"
        />
        <JobProgressBarSegment
            label="finished"
            color="var(--int-green)"
            :total="job.progress.total"
            :value="job.progress.finished"
        />
        <JobProgressBarSegment
            label="processing"
            color="var(--int-light-grey)"
            :total="job.progress.total"
            :value="job.progress.processing"
        />
        <JobProgressBarSegment
            label="untagged"
            color="var(--int-very-light-grey)"
            :total="job.progress.total"
            :value="job.progress.untagged"
        />
    </div>
</template>

<script setup lang="ts">
import type { Job } from "@/types/jobs"

const { job } = defineProps<{ job: Job }>()
</script>

<style scoped lang="scss">
.progress {
    max-width: 100%;
    line-height: 2rem;
    width: 700px;
    border: 1px solid var(--int-light-grey);
}
</style>
