<template>
    <GCard title="Document View">
        <template #help>
            The document view show the differences between the reference and hypothesis layer in a single document. Red
            words indicate a difference between the layers for the selected annotation. Hover over a word to see all
            annotations.
        </template>

        <p v-if="!commonAnnotations.length">
            Select a reference layer, a hypothesis layer. Then, select a document and an annotation.
        </p>
        <GForm v-else>
            <DocumentSelect v-model="selectedDocument" />
            <fieldset>
                <label for="annotation-select">Annotation</label>
                <MultiSelect
                    id="annotation-select"
                    v-model="selectedAnnotations"
                    :options="annotationOptions"
                    optionLabel="text"
                    optionValue="value"
                    placeholder="Annotation"
                    :maxSelectedLabels="5"
                />
            </fieldset>
        </GForm>

        <GSpinner v-if="loading" />
        <!-- <SingleTermComparisonTable :hypoTerm="tc.hyp" :refTerm="tc.ref" class="tooltip" /> -->
        <div v-if="matches?.length ?? 0" class="document">
            <template v-for="tc in paginate(sourceMatches)" :key="tc.ref.id">
                <span
                    class="wordComparison"
                    :class="{ incorrect: !annotationsEqual(tc) }"
                    @mouseenter="
                        (e) => {
                            hoveredTerm = tc
                            x = e.pageX
                            y = e.pageY
                        }
                    "
                    @mouseleave="hoveredTerm = null"
                >
                    {{ tc.ref.annotations["token"] }}
                </span>
                <!-- force a whitespace node in between words -->
                <span v-html="'&#32;'"></span>
            </template>
        </div>

        <!-- tooltip at mouse cursor -->
        <!-- should set absolute pixel position based on mouse cursor -->
        <SingleTermComparisonTable
            v-if="hoveredTerm"
            :hypoTerm="hoveredTerm.hyp"
            :refTerm="hoveredTerm.ref"
            class="tooltip"
            :style="{ top: `${y}px`, left: `${x}px` }"
        />

        <GTablePaginator v-if="numPages > 1" v-model="page" :numPages />
    </GCard>
</template>

<script setup lang="ts">
import { ref } from "vue"
import useComparison from "@/stores/evaluation/comparison"
import useLayers from "@/stores/layers"
import type { TermComparison } from "@/types/evaluation"
import type { SelectOption } from "@/types/ui/select"
import { useMouse } from "@vueuse/core"

import MultiSelect from "primevue/multiselect"
import type { Term } from "@/types/annotation"

const { commonAnnotations, hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
const { loading, matches, document: selectedDocument } = storeToRefs(useComparison())

const annotationOptions = computed(() =>
    // only logical annotations
    commonAnnotations.value.filter((option: SelectOption) => !["token"].includes(option.text)),
)
const selectedAnnotations = ref<string[]>()
const x = ref(0)
const y = ref(0)
/** Return items on page N */
function paginate(items: T[]): T[] {
    return items.slice((page.value - 1) * pageSize, page.value * pageSize)
}
// Fields
const sourceMatches = computed(() => matches.value?.filter((m) => m.ref.annotations.token))
const pageSize = 200
const page = ref<number>(1)
const numPages = computed(() => Math.ceil((sourceMatches.value?.length ?? 0) / pageSize))
const hoveredTerm = ref<TermComparison | null>(null)
// Methods
function annotationsEqual(tc: TermComparison) {
    for (const annotation of selectedAnnotations.value ?? []) {
        if (tc.ref.annotations[annotation] !== tc.hyp.annotations[annotation]) {
            return false
        }
    }
    return true
}
watchPostEffect(() => {
    selectedAnnotations.value ??= [annotationOptions.value[0]?.value]
})
watch(matches, () => {
    page.value = 1
})
</script>

<style scoped lang="scss">
.document {
    padding: 2rem;
    max-width: 80ch;
    font-size: 1.3rem;
    line-height: 2;
}

.wordComparison {
    cursor: default;
    position: relative;
    text-align: center;
    &.incorrect {
        background-color: rgba(255, 0, 0, 0.1);
    }

    &:hover .tooltip {
        visibility: visible;
    }

    &:hover {
        text-decoration: underline;
    }
}
.tooltip {
    // font-size: 0.8rem;
    // min-width: fit-content;
    // min-height: fit-content;
    // visibility: hidden;
    position: absolute;
    z-index: 1;
    // width: max-content;
    // bottom: 100%;
    // text-align: left;
    // padding: 0;
    transform: translateX(-50%) translateY(-125%);
    // margin: 0;
}
</style>
