<!-- A modal used by PoS confusion & metrics. -->
<template>
    <GModal :show="show" @hide="$emit('hide')" :title="title">
        <template #help>
            Here you can see a sample of how a token was tagged by <i>{{ hypothesisJob }}</i> and
            <i>{{ referenceJob }}</i
            >. The samples are a random selection of all tokens in this category.
        </template>

        <p>Columns to display:</p>
        <div class="columnSelector">
            <GInput
                type="checkbox"
                v-for="annotation in annotations"
                :key="annotation"
                v-model="visibleColumns[annotation]"
            >
                {{ annotation }}
            </GInput>
        </div>

        <GTable :columns="filteredColumns" :items="items" headless>
            <template #head="data">{{ data.field.label || data.field.key }}</template>
            <template #cell="data">{{ data.value }}</template>
        </GTable>
        <!--Download-->
        <p>Download all samples for this category.</p>
        <DownloadButton wide @click="$emit('download')" :loading="downloading" />
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores
import { computed, ref, watch } from "vue"
import stores, { CorporaStore } from "@/stores"
// Types & API.
import * as API from "@/api/evaluation"
import * as Utils from "@/api/utils"
import { TermComparison } from "@/types/evaluation"
import { literalsForTermComparison } from "@/stores/evaluation"
// Components
import { GModal, GTable, DownloadButton, GInput } from "@/components"

// Stores
const corporaStore = stores.useCorpora() as CorporaStore

// Props
const props = defineProps({
    show: { type: Boolean },
    samples: { type: Object },
    referenceJob: { type: String },
    hypothesisJob: { type: String },
    downloading: { type: Boolean, default: false },
    annotationType: { type: String },
})

// Emits
defineEmits(["hide", "download"])

// Fields
const ignorableAnnotations = ["token", "id", "misc"]
const title = computed(() => {
    if (props.samples.title) return props.samples.title
    return props.samples.agreement ? "PoS agree samples" : "Pos Confusion samples"
})
const annotations = computed(() => {
    const firstSample = props.samples.samples[0]
    const hypoAnnotations = Object.keys(firstSample.hypoTerm.annotations)
    const refAnnotations = Object.keys(firstSample.refTerm.annotations)
    return [...new Set([...hypoAnnotations, ...refAnnotations])].filter((i) => !ignorableAnnotations.includes(i))
})

const columns = computed(() => {
    // Currently we take annotations from the first sample of the hypothesis.
    const referenceColumns = annotations.value.map((i) => ({
        key: props.referenceJob + "-" + i,
        label: props.referenceJob + " " + i,
    }))
    const hypothesisColumns = annotations.value.map((i) => ({
        key: props.hypothesisJob + "-" + i,
        label: props.hypothesisJob + " " + i,
    }))

    return [{ key: "literal", label: "token" }, ...hypothesisColumns, ...referenceColumns]
})
const visibleColumns = ref({}) // { [annotation]: boolean }
/**
 * columns filtered based on the visible columns selection.
 */
const filteredColumns = computed(() => {
    const columnNames = ["literal"]
    for (const [annotation, visible] of Object.entries(visibleColumns.value)) {
        if (visible) {
            columnNames.push(props.referenceJob + "-" + annotation)
            columnNames.push(props.hypothesisJob + "-" + annotation)
        }
    }
    return columns.value.filter((i) => columnNames.includes(i.key))
})

const items = computed(() => {
    if (!props.samples.samples) return []
    return props.samples.samples.map((sample: TermComparison) => {
        const hypoAnnotations = Object.entries(sample.hypoTerm.annotations).map((i) => ({
            [props.hypothesisJob + "-" + i[0]]: i[1],
        }))
        const refAnnotations = Object.entries(sample.refTerm.annotations).map((i) => ({
            [props.referenceJob + "-" + i[0]]: i[1],
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
button,
p {
    margin: 5px auto;
    display: block;
    width: fit-content;
}

.fa-download {
    padding: 0 1em;
}

.columnSelector {
    display: flex;
    flex-wrap: wrap;
    justify-content: center;
}

/* 
:deep(td):nth-child(1),
:deep(td):nth-child(3) {
    border-right: 1px solid var(--int-very-light-grey-hover);
} */
</style>
