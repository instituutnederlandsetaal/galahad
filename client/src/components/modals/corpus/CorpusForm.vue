<!-- create or update a corpus -->
<template>
    <GModal :title :show @hide="$emit('hide')">
        <template #help>
            <slot name="help"></slot>
        </template>
        <form @submit.prevent @keyup.enter="doAction">
            <table>
                <template v-if="userStore.hasWriteAccess || !item">
                    <tr>
                        <td>
                            <label>Name:</label> <span class="warning"><small>(Required)</small></span>
                        </td>
                        <td>
                            <GInput v-model="name" focus placeholder="corpus name" :validator="validateCorpusName"
                                validityDescriptor="3-100 characters" />
                        </td>
                    </tr>
                    <tr>
                        <td><label>Year from:</label></td>
                        <td>
                            <GNumber v-model="eraFrom" validityDescriptor="Must be before end year" placeholder="YYYY"
                                :min="-10000" :max="2100" :step="50"
                                :validator="(v) => { console.log(v); return v <= eraTo }" />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Year to:</label></td>
                        <td>
                            <GNumber v-model="eraTo" validityDescriptor="Must be after start year" placeholder="YYYY"
                                :min="-10000" :max="2100" :step="50" :validator="(v) => { return v >= eraFrom }" />
                        </td>
                    </tr>

                    <tr>
                        <td>
                            <label>
                                <ExternalLink href="/galahad/overview/tagsets">Tagset</ExternalLink>:
                            </label>
                        </td>
                        <td>
                            <GInput v-model="tagset" list="tagsets" placeholder="tagset name" />
                            <datalist id="tagsets">
                                <option v-for="(tagset, _) in tagsetsStore.tagsets" :value="tagset.shortName"></option>
                            </datalist>
                        </td>
                    </tr>

                    <template v-if="userStore.user.isAdmin">
                        <tr>
                            <td colspan="2">
                                <hr />
                            </td>
                        </tr>

                        <tr>
                            <td>Benchmark set:</td>
                            <td>
                                <GCheckBox v-model="dataset">Benchmark</GCheckBox>
                            </td>
                        </tr>
                    </template>

                    <tr>
                        <td colspan="2">
                            <hr />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Source name:</label></td>
                        <td>
                            <GInput v-model="sourceName" placeholder="source name" />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Source url:</label></td>
                        <td>
                            <GInput v-model="sourceURL" type="url" placeholder="source url" />
                        </td>
                    </tr>

                    <UserList :users="collaborators" listName="Collaborators" :showAddDialog />
                </template>
                <UserList :users="viewers" listName="Viewers" :showAddDialog />
            </table>
        </form>

        <template #buttons>
            <GButton green @click="doAction" :disabled>{{ update ? "Update" : "Create" }}</GButton>
        </template>
    </GModal>
</template>

<script setup lang="ts">
import stores from "@/stores"
import type { MutableCorpusMetadata } from "@/types/corpora"

const userStore = stores.useUser()
const tagsetsStore = stores.useTagsets()

// --- props ---
const { action, cancel, item, update, title, show } = defineProps({
    action: { type: Function },
    cancel: { type: Function },
    item: { default: null },
    update: { type: Boolean, default: false },
    title: { type: String, default: "" },
    show: { type: Boolean, default: true },
})

// --- data ---
const dataset = ref(false)
const name = ref("")
const eraFrom = ref<number>()
const eraTo = ref<number>()
const tagset = ref("")
const sourceName = ref("")
const sourceURL = ref("")
const collaborators = ref([])
const viewers = ref([])

// --- computed ---
const showAddDialog = computed(() => {
    // Only show it when you're not editing (i.e. you pressed 'new')
    // Or if you did press edit, only show it when you have access
    return !update || userStore.hasDeleteAccess
})
const disabled = computed(() => {
    if (!item && update) return true
    const i = item as MutableCorpusMetadata
    return (
        !isValid.value ||
        (update &&
            name.value === i.name &&
            eraFrom.value === i.eraFrom &&
            eraTo.value === i.eraTo &&
            tagset.value === i.tagset &&
            collaborators.value.join("\n") === i.collaborators.join("\n") &&
            viewers.value.join("\n") === i.viewers.join("\n") &&
            sourceName.value === i.sourceName &&
            sourceURL.value === i.sourceURL &&
            dataset.value === i.dataset)
    )
})
const isValid = computed(() => {
    if (!validateCorpusName(name.value)) return false
    // check if eras are integer values
    if (eraFrom.value && !Number.isInteger(eraFrom.value)) return false
    if (eraTo.value && !Number.isInteger(eraTo.value)) return false
    if (eraFrom.value > eraTo.value) return false
    return true
})

// --- watch ---
watch(
    () => item,
    (newValue: MutableCorpusMetadata): void => {
        if (!newValue) return
        name.value = newValue.name
        eraFrom.value = newValue.eraFrom
        eraTo.value = newValue.eraTo
        tagset.value = newValue.tagset
        sourceName.value = newValue.sourceName
        sourceURL.value = newValue.sourceURL
        dataset.value = newValue.dataset
        collaborators.value = [...newValue.collaborators]
        viewers.value = [...newValue.viewers]
    },
    { immediate: true, deep: true },
)

// --- methods ---
function doAction(): void {
    if (!validateCorpusName(name.value)) return
    const value: MutableCorpusMetadata = {
        owner: "", // this is set by the server for security reasons
        name: name.value,
        eraFrom: eraFrom.value,
        eraTo: eraTo.value,
        tagset: tagset.value,
        dataset: dataset.value,
        collaborators: collaborators.value,
        viewers: viewers.value,
        sourceName: sourceName.value,
        sourceURL: validateSourceURL(sourceURL.value),
    }
    action(value)
    resetFormFields()
}
function doCancel(): void {
    resetFormFields()
    cancel()
}
function resetFormFields(): void {
    collaborators.value = []
    viewers.value = []
    name.value = ""
    eraFrom.value = null
    eraTo.value = null
    tagset.value = ""
    sourceName.value = ""
    sourceURL.value = ""
    dataset.value = false
}
function validateCorpusName(name: string): boolean {
    return name.toString().match(/^.{3,100}$/)
}
function validateSourceURL(url: string): string {
    if (!url) return url
    try {
        new URL(url)
    } catch (error) {
        // try to fix it by adding a protocol
        url = `http://${url}`
    }
    return url
}
</script>

<style scoped lang="scss">
.warning {
    color: var(--int-red);
}

.borderRow {
    border-top: 1px solid var(--int-grey);
}

:deep(table) {
    border-collapse: collapse;

    td {
        padding: 3px 10px;
    }
}

:deep(hr) {
    margin: 10px 0;
    border: 1px dotted var(--int-grey);
}

:deep(.checkbox-container) {
    margin-bottom: 0;
}
</style>
