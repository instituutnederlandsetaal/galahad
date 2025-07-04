<template>
    <div role="progressbar" class="progress">
        <JobProgressSegment
            label="failed"
            color="var(--int-red)"
            :total="job.progress.total"
            :value="job.progress.failed"
        />
        <JobProgressSegment
            label="finished"
            color="var(--int-green)"
            :total="job.progress.total"
            :value="job.progress.finished"
        />
        <JobProgressSegment
            label="processing"
            color="var(--int-light-grey)"
            :total="job.progress.total"
            :value="job.progress.processing"
        />
        <!-- When busy, consider untagged documents pending. -->
        <!-- Confusingly, the API already calls them pending, though. -->
        <JobProgressSegment
            label="pending"
            color="var(--int-very-light-grey)"
            :total="job.progress.total"
            :value="job.progress.busy ? job.progress.pending : 0"
        />
        <!-- Otherwise, just untagged. -->
        <JobProgressSegment
            label="untagged"
            color="var(--int-very-light-grey)"
            :total="job.progress.total"
            :value="job.progress.busy ? 0 : job.progress.pending"
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
