<template>
    <GCard :title :helpSubject>
        <template v-if="$slots.title" #title>
            <slot name="title"></slot>
        </template>

        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
        </template>

        <GSpinner v-if="loading" />

        <div id="prepend" v-if="$slots.prepend">
            <slot name="prepend"></slot>
        </div>

        <slot v-if="items && items.length === 0 && !loading" name="table-empty-instruction">
        </slot>

        <table :class="classes">
            <thead v-if="!isEmpty">
                <tr>
                    <th v-for="field in visibleFields" :key="field.key" style="text-align: center">
                        <div style="white-space: pre-line">
                            <!-- specific head -->
                            <slot :name="'head-' + field.key" :field="field">
                                <!-- generic head -->
                                <slot name="head" :field="field">{{ field.label || field.key }}</slot>
                            </slot>
                        </div>
                        <span v-if="field.sortOn">
                            <span v-if="sortedBy != field.key" class="sort-control">
                                <span @click="sortBy(field.key, false)">▲</span> |
                                <span @click="sortBy(field.key, true)">▼</span>
                            </span>
                            <span v-else class="sort-control">
                                <span v-if="!sortIsDesc" class="sort-control active">▲</span>
                                <span v-else @click="sortBy(field.key, false)" class="sort-control">▲</span>
                                <span class="sort-control active"> | </span>
                                <span v-if="sortIsDesc" class="sort-control active">▼</span>
                                <span v-else @click="sortBy(field.key, true)" class="sort-control">▼</span>
                            </span>
                        </span>
                        <span v-else></span>
                    </th>
                </tr>
            </thead>
            <tbody>
                <!-- the rows -->
                <template v-for="(item, i) in itemsToDisplay" :key="'row' + i">
                    <tr
                        @click="rowClicked(item)"
                        :class="(equal(model, item) ? 'selected' : '') + ' ' + (selectable ? 'cursor-pointer' : '')">
                        <td
                            v-for="field in visibleFields"
                            :key="field.key"
                            :style="`text-align: ${field.textAlign || 'center'};`">
                            <!-- specific cell rendering -->
                            <slot
                                :name="'cell-' + field.key"
                                :field="_field(field)"
                                :item="item"
                                :value="item[field.key] || ''">
                                <!-- generic cell rendering -->
                                <slot name="cell" :field="_field(field)" :item="item" :value="item[field.key] || ''">
                                    {{ item[field.key] }}</slot
                                >
                            </slot>
                        </td>
                    </tr>
                    <!-- details -->
                    <tr :key="'_details' + i" v-if="item._showDetails" class="details">
                        <td :colspan="visibleFields.length">
                            <slot name="_details" :item="item"></slot>
                        </td>
                    </tr>
                </template>
            </tbody>
        </table>
        <footer id="footer" v-if="!(isEmpty && !displayOnEmpty) && numPages > 1">
            <!-- page controls -->
            <div
                v-if="numPages > 1"
                id="page-controls"
                @click="$nextTick(() => $refs.test.scrollIntoView())"
                ref="test">
                <GButton plain @click="page = 1" :disabled="page == 1">1</GButton>
                <GButton plain @click="page > 1 ? (page -= 1) : null" :disabled="page == 1" title="Previous">
                    <i class="fa fa-arrow-left"></i>
                </GButton>
                <select v-model="page">
                    <option v-for="pageNumber in numPages" :key="pageNumber" :value="pageNumber">
                        {{ pageNumber }}
                    </option>
                </select>
                <GButton plain @click="page < numPages ? (page += 1) : null" :disabled="page == numPages" title="Next">
                    <i class="fa fa-arrow-right"></i>
                </GButton>
                <GButton plain @click="page = numPages" :disabled="page == numPages">{{ numPages }}</GButton>
            </div>
        </footer>
    </GCard>
</template>

<script setup lang="ts">
import type { Field } from "@/types/table"

type Item = { [key: string]: unknown }

// --- props ---
const {
	title,
	displayOnEmpty,
	columns,
	loading,
	selectable,
	sortedByColumn,
	sortDesc,
	compact,
	items,
	helpSubject,
} = defineProps<{
	title?: string
	displayOnEmpty?: boolean
	columns: Field[]
	loading: boolean
	selectable?: boolean
	sortedByColumn?: string
	sortDesc?: boolean
	compact?: boolean
	items: Item[]
	helpSubject?: string
}>()

// --- data ---
const model = defineModel<Item>()
const page = ref<number>(1)
const sortedBy = ref<string>(sortedByColumn)
const sortIsDesc = ref<boolean>(sortDesc)

// --- computed ---
const classes = computed(() => ({
	compact: compact,
	loading: loading,
	selectable: selectable,
}))
const isEmpty = computed<boolean>(() => {
	return !items || items.length === 0
})
const itemsToDisplay = computed<Item[]>(() => {
	function getPageItems(allItems: Item[]) {
		return allItems.slice(
			(page.value - 1) * pageSize.value,
			page.value * pageSize.value,
		)
	}

	// only paginate
	if (sortedBy.value === null) return getPageItems(items)

	const sortOn = columns.filter((field) => field.key == sortedBy.value)[0]
		?.sortOn //hmm
	function mapToSortProp(x: any) {
		return sortOn ? sortOn(x) : x
	}

	if (sortedBy.value === null) {
		// no sort, just paginate
		return getPageItems(items)
	} else {
		// sort and then paginate
		const allItems = items
			.slice()
			.sort(
				(a: Item, b: Item) =>
					(-1) ** (+sortIsDesc.value | 0) *
					compareAny(mapToSortProp(a), mapToSortProp(b)),
			)
		return getPageItems(allItems)
	}
})
const numPages = computed<number>(() => {
	return Math.ceil(items.length / pageSize.value)
})
const pageSize = computed<number>(() => {
	if (!items) return 20
	// We allow for some leniency since we don't want the user to go to the next page just to see one entry
	if (items.length <= 50) return items.length
	return items.length > 20 ? 20 : items.length
})
const primaryKeyFields = computed<string[]>(() => {
	return columns
		.filter((field) => field.isPrimaryField)
		.map((field) => field.key)
})
const visibleFields = computed<Field[]>(() => {
	return columns.filter((field) => !field.hidden)
})

// --- watch ---
watch(numPages, (newVal) => {
	if (page.value > newVal && newVal > 0) {
		page.value = newVal
	}
})

// --- methods ---
function anyIncludes(whole: unknown, part: unknown): boolean {
	if (!whole) return false
	return (whole as Record<string, unknown> | unknown[])
		.toString()
		.includes((part as Record<string, unknown> | unknown[]).toString())
}
function compareAny(a: unknown, b: unknown): number {
	// null and undefined are always smaller
	if (nu(a) && nu(b)) return 0
	if (nu(a)) return -1
	if (nu(b)) return 1

	// Infinity is always bigger
	if (a === Infinity) return 1
	if (b === Infinity) return -1

	if (typeof a === "number" && typeof b === "number") {
		return a - b
	} else if (typeof a === "string" && typeof b === "string") {
		return a.localeCompare(b)
	} else if (Array.isArray(a) && Array.isArray(b)) {
		if (a.length === 0 && b.length === 0) return 0
		if (a.length === 0) return -1
		if (b.length === 0) return 1
		return compareAny(a[0], b[0]) // Approximate
	} else if (typeof a === "boolean" && typeof b === "boolean") {
		if (a === b) return 0
		if (a) return 1
		return -1
	} else {
		//garbage
		return 0
	}
}
function _field(field: Field): Field {
	if (!field.label) {
		field.label = field.key
	}
	return field
}
function equal(item1: Item, item2: Item): boolean {
	// tests for equality of two items based an the primary key fields
	if (primaryKeyFields.value.length === 0) return item1 === item2
	if (nu(item1) && nu(item2)) return true
	if (nu(item1) || nu(item2)) return false
	return (
		primaryKeyFields.value
			.map((key: string) => item1[key] === item2[key])
			.filter((x) => !x).length === 0
	)
}
function nu(v: unknown) {
	return v === null || v === undefined
} //utility
function rowClicked(item: Item): void {
	if (selectable) {
		model.value = item
	}
}
function sortBy(key: string, desc: boolean): void {
	sortedBy.value = key
	sortIsDesc.value = desc
	// Reset to first page to see the effect of sorting.
	page.value = 1
}
</script>

<style scoped lang="scss">
*:deep(.table-controls) {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    justify-content: center;
    align-content: stretch;

    .table-control {
        flex-basis: 100px;
        min-height: 100px;

        &.slider {
            /*Some overrides because the slider looks bad when small*/
            flex: 1;
            min-width: 200px;
            padding: 0px 20px;
            max-width: 400px;
        }
    }
}

.cursor-pointer {
    cursor: pointer;
}

#footer {
    overflow: hidden;
    display: flex;
    justify-content: center;
}

#page-controls {
    background-color: var(--white);
    border: 1px solid none;
    color: var(--black);
    float: right;
    padding: 10px;
    -webkit-user-select: none;
    /* Safari */
    -moz-user-select: none;
    /* Firefox */
    -ms-user-select: none;
    /* IE10+/Edge */
    user-select: none;
    /* Standard */
}

#page-controls select {
    margin: 5px;
}

#page-controls span {
    margin: 5px;
}

#page-controls .inactive {
    color: var(--int-light-grey);
}

.sort-control {
    white-space: nowrap;
    color: var(--white);
    -webkit-user-select: none;
    /* Safari */
    -moz-user-select: none;
    /* Firefox */
    -ms-user-select: none;
    /* IE10+/Edge */
    user-select: none;

    /* Standard */
    span {
        cursor: pointer;
    }
}

.sort-control.active {
    color: black;
}

table.loading {
    filter: blur(5px);
}

table.loading .loading-symbol {
    opacity: 1;
    visibility: visible;
}

table .loading-symbol {
    transition:
        opacity 2s ease,
        visibility 2s ease;
    opacity: 0;
    z-index: 1;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    visibility: hidden;
}

table {
    border-collapse: collapse;
    margin: 0 auto;
    // margin-top: 5px;
    padding: 0;

    caption {
        font-size: 1.5em;
        margin: 0.5em 0 0.75em;
    }

    tr {
        border: 1px solid var(--int-very-light-grey-hover);

        &:nth-child(even) {
            background: #fff;
        }

        &:nth-child(odd) {
            background: var(--int-very-light-grey);
        }
    }

    thead {
        > tr {
            background-color: var(--int-theme) !important;
        }
    }

    th {
        padding: 0.6em;
        text-align: center;
        font-size: 0.85em;
        letter-spacing: 0.1em;
        text-transform: uppercase;
    }

    td {
        padding: 0.5em;
        text-align: center;
        min-width: 60px;
    }

    overflow-x: auto;
}

table.compact {
    td,
    th {
        padding: 0.1em 2em;
    }

    margin: 0 auto;
}

table.selectable {
    tr:hover:not(.selected) {
        background: var(--int-very-light-grey-hover);
    }
}

table tr.selected {
    background-color: var(--int-theme-lighter);
}
</style>
