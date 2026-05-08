<!-- A modal used by PoS confusion & metrics. -->
<template>
    <GModal @hide="$emit('hide')">
        <GTable :columns="filteredColumns" :items :title>
            <template #help>
                <p>
                    Here you can see a sample of how a token was tagged by <i>{{ hypothesisJob }}</i> and
                    <i>{{ referenceJob }}</i
                    >. The samples are a random selection of all tokens in this category.
                </p>
            </template>

            <template #header>
                <p>Columns to display:</p>
                <form @submit.prevent class="columnSelector">
                    <GCheckBox v-for="annotation in annotations" :key="annotation" v-model="visibleColumns[annotation]">
                        {{ annotation }}
                    </GCheckBox>
                </form>
            </template>
        </GTable>
        <!--Download-->
        <p>Download all samples for this category.</p>
        <DownloadButton wide :loading="downloading" @click="$emit('download')" />
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores

// Types & API.
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import stores from "@/stores"
import { literalsForTermComparison } from "@/ts/termcomparison"
import type { TermComparison } from "@/types/evaluation"

// Stores
const corporaStore = stores.useCorpora()

// Props
const props = defineProps({
    samples: { type: Object },
    referenceJob: { type: String },
    hypothesisJob: { type: String },
    downloading: { type: Boolean, default: false },
    annotationType: { type: String },
})

// Emits
const emit = defineEmits<{ download: []; hide: [] }>()
// Fields
const ignorableAnnotations = ["token", "id", "misc"]
const title = computed<string>(() => {
    if (props.samples.title) return props.samples.title
    return props.samples.agreement ? "PoS agree samples" : "Pos Confusion samples"
})
const annotations = computed(() => {
    const firstSample = props.samples.samples[0]
    const hypoAnnotations = Object.keys(firstSample.hyp.annotations)
    const refAnnotations = Object.keys(firstSample.ref.annotations)
    return [...new Set([...hypoAnnotations, ...refAnnotations])].filter((i) => !ignorableAnnotations.includes(i))
})

const columns = computed(() => {
    // Currently we take annotations from the first sample of the hypothesis.
    const referenceColumns = annotations.value.map((i) => ({
        key: `${props.referenceJob}-${i}`,
        label: `${props.referenceJob} ${i}`,
    }))
    const hypothesisColumns = annotations.value.map((i) => ({
        key: `${props.hypothesisJob}-${i}`,
        label: `${props.hypothesisJob} ${i}`,
    }))

    return [{ key: "literal", label: "token" }, ...hypothesisColumns, ...referenceColumns]
})
const visibleColumns = ref<Record<string, boolean>>({}) // { [annotation]: boolean }
/**
 * columns filtered based on the visible columns selection.
 */
const filteredColumns = computed(() => {
    const columnNames = ["literal"]
    for (const [annotation, visible] of Object.entries(visibleColumns.value)) {
        if (visible) {
            columnNames.push(`${props.referenceJob}-${annotation}`)
            columnNames.push(`${props.hypothesisJob}-${annotation}`)
        }
    }
    return columns.value.filter((i) => columnNames.includes(i.key))
})

const items = computed(() => {
    if (!props.samples.samples) return []
    return props.samples.samples.map((sample: TermComparison) => {
        const hypoAnnotations = Object.entries(sample.hyp.annotations).map((i) => ({
            [`${props.hypothesisJob}-${i[0]}`]: i[1],
        }))
        const refAnnotations = Object.entries(sample.ref.annotations).map((i) => ({
            [`${props.referenceJob}-${i[0]}`]: i[1],
        }))

        return {
            literal: literalsForTermComparison(sample),
            ...Object.assign({}, ...hypoAnnotations, ...refAnnotations),
        }
    })
})

// Watches & mounts
watch(
    () => props.samples.annotationType,
    (annotation) => {
        visibleColumns.value = {} // reset
        // annotationType is e.g. "pos" or "pos + lemma"
        const annotations = annotation.split("+").map((i) => i.trim())
        for (const annotation of annotations) {
            visibleColumns.value[annotation] = true
        }
    },
)
</script>

<style scoped lang="scss">
.columnSelector {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
}
</style>
