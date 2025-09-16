<template>
    <GCard title="Taggers overview">
        <template #help>
            <p>Here you can see an overview of all available taggers within GaLAHaD.</p>
            <p>
                For more information on the taggers, please visit
                <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized/">
                    galahad-taggers-dockerized on GitHub </ExternalLink
                >.
            </p>
        </template>

        <GTable :loading :columns :items sortColumn="id">
            <template #table-empty>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />.
            </template>

            <template #cell-id="d: TableData<Tagger>">
                <span :id="d.item.id" :class="markActive(d.item.id)">{{ d.value }}</span>
            </template>

            <template #cell-annotations="d: TableData<Tagger>">
                {{ d.item.annotations.map((a) => a.annotation).join(", ") }}
                <AnnotationItemsViewer :items="d.item.annotations">
                    <template #title>Annotations and principles of {{ d.item.id }}</template>
                </AnnotationItemsViewer>
            </template>

            <template #cell-attributions="d: TableData<Tagger>">
                {{ Object.keys(d.item.attributions).length }} attributions
                <InspectButton @click="showAttributions = true" />
                <GModal v-if="showAttributions" @hide="showAttributions = false">
                    <template #title>Attributions of {{ d.item.id }}</template>
                    <ul>
                        <li v-for="attribution in d.item.attributions" :key="attribution.name">
                            <dl>
                                <dt>
                                    <b>{{ attribution.name }}:</b>
                                </dt>
                                <dd>
                                    <ExternalLink :href="attribution.href">
                                        {{ attribution.details ?? attribution.href }}
                                    </ExternalLink>
                                </dd>
                            </dl>
                        </li>
                        <!-- always add version last -->
                        <li>
                            <dl>
                                <dt>
                                    <b>Version:</b>
                                </dt>
                                <dd>
                                    {{ d.item.version }}
                                </dd>
                            </dl>
                        </li>
                    </ul>
                </GModal>
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { Tagger } from "@/types/taggers"
import type { Column, TableData } from "@/types/ui/table"

const { taggers: items, loading } = storeToRefs(stores.useTaggers())

const showAnnotations = ref<boolean>()
const showAttributions = ref<boolean>()

const columns: Column<Tagger>[] = [
    { key: "id", label: "name" },
    { key: "description" },
    { key: "language" },
    {
        key: "period",
        sortOn: (t: Tagger): string => `${t.period.from} ${t.period.to}`,
        format: (t: Tagger): string => `${t.period.from} – ${t.period.to}`,
    },
    { key: "annotations", sortOn: (t: Tagger): string => t.annotations.map((a) => a.annotation).join() },
    { key: "attributions", noSort: true },
]

/** Mark the active row, retrieved from the url anchor. */
function markActive(id: string): string {
    const hash = window.location.hash.substring(1)
    return id === hash ? "active" : ""
}
</script>

<style scoped lang="scss">
:deep(tr):has(> td > span.active) {
    background-color: var(--int-theme-lighter);
}

ul {
    padding: 0 1rem;
    .annotation-principles {
        padding: 0 1rem;
    }
}
</style>
