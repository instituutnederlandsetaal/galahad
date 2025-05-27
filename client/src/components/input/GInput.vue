<template>
    <div :class="inline ? 'inline' : ''">
        <!-- checkbox -->
        <template v-if="type === 'checkbox'">
            <label class="clickable checkbox-container">
                <slot></slot>
                <input v-model="model" :type :placeholder />
                <span class="checkmark" tabindex="0" @keypress.space.prevent="check" @keyup.enter="check"></span>
            </label>
        </template>
        <!-- other: text -->
        <template v-else>
            <!-- text with clear button-->
            <div v-if="clearBtn" class="clear">
                <input v-model="model" :type :placeholder :disabled :list ref="inputElement"
                    @keyup.enter="$emit('enter')" />
                <input type="reset" value="&#10006;" :disabled="model === null || model.length == 0" title="Clear"
                    @click="model = ''" />
            </div>
            <!-- text without clear button-->
            <input v-else v-model="model" :type :placeholder :disabled :list ref="inputElement"
                @keyup.enter="$emit('enter')" />
        </template>

        <template v-if="validator">
            <span id="invalidFeedback" v-if="!isValid">
                <i class="fa fa-times"></i> <i>{{ validityDescriptor }}</i>
            </span>
            <span v-else id="validFeedback">
                <i class="fa fa-check"></i> <i>{{ validityDescriptor }}</i>
            </span>
        </template>
    </div>
</template>

<script setup lang="ts">
import type { SelectOption } from '@/types/ui/select';

// --- model ---
const model = defineModel()

// --- props ---
const {
    checked,
    disabled,
    inline,
    validityDescriptor,
    list,
    min,
    max,
    options,
    placeholder,
    step,
    type = "text",
    validator,
    clearBtn,
    focus,
} = defineProps<{
    checked: boolean
    disabled: boolean
    inline: boolean
    validityDescriptor: string
    list: string
    min: number
    max: number
    options: SelectOption[]
    placeholder: string
    step: number
    type: string
    validator: (value: any) => boolean
    clearBtn: boolean
    focus: boolean
}>()

// --- lifecycle ---
onMounted(() => {
    if (focus) {
        nextTick(() => {
            inputElement.value?.focus()
        })
    }
})

// --- data ---
const inputElement = ref<HTMLInputElement>()

// --- computed ---
const isValid = computed(() => validator(model.value))

// --- methods ---
function check(): void {
    model.value = !model.value
}
</script>

<style scoped lang="scss">
select,
input {
    font-style: inherit;
    font-variant-ligatures: inherit;
    font-variant-caps: inherit;
    font-variant-numeric: inherit;
    font-variant-east-asian: inherit;
    font-weight: inherit;
    font-stretch: inherit;
    font-size: inherit;
    font-family: inherit;
}

div.inline {
    display: inline-block;
}

#invalidFeedback {
    border-bottom: 2px solid var(--int-red);
    margin-left: 10px;
    padding: 2px;
}

#validFeedback {
    border-bottom: 2px solid var(--gold);
    margin-left: 10px;
    padding: 2px;
}

label {
    margin-bottom: 0px;
}

.clickable {
    cursor: pointer;
}

/* checkbox */
.checkbox-container {
    display: block;
    position: relative;
    padding: 0px 10px 0 35px;
    margin-bottom: 12px;
    cursor: pointer;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    width: fit-content;
}

/* Hide the browser's default checkbox */
.checkbox-container input {
    display: none;
}

/* Create a custom checkbox */
.checkmark {
    position: absolute;
    top: 0;
    left: 0;
    height: 25px;
    width: 25px;
    background-color: var(--int-very-light-grey);
}

/* On mouse-over, add a grey background color */
.checkbox-container:hover input~.checkmark {
    background-color: var(--int-very-light-grey-hover);
}

.checkbox-container:active input~.checkmark {
    background-color: var(--int-light-grey-hover);
}

/* When the checkbox is checked, add a INT background */
.checkbox-container input:checked~.checkmark {
    background-color: var(--int-theme);
}

.checkbox-container:hover input:checked~.checkmark {
    background-color: var(--int-theme-hover);
}

.checkbox-container:active input:checked~.checkmark {
    background-color: var(--int-theme-active);
}

/* Create the checkmark/indicator (hidden when not checked) */
.checkmark:after {
    content: "";
    position: absolute;
    display: none;
}

/* Show the checkmark when checked */
.checkbox-container input:checked~.checkmark:after {
    display: block;
}

/* Style the checkmark/indicator */
.checkbox-container .checkmark:after {
    left: 9px;
    top: 5px;
    width: 5px;
    height: 10px;
    border: solid black;
    border-width: 0 3px 3px 0;
    -webkit-transform: rotate(45deg);
    -ms-transform: rotate(45deg);
    transform: rotate(45deg);
}

/* Inputs */
input[type="text"],
input[type="url"],
input[type="reset"] {
    height: 39px;
    font-size: 1rem;
    padding-left: 5px;
    border: 1px solid #ccc;
    width: 209px;
    background-color: white;

    &:focus {
        outline: var(--int-theme-active) solid 2px;
    }
}

div.clear {
    width: 208px;
    height: 39px;
    padding: 0;
    margin: 0;

    &:focus-within {
        outline: var(--int-theme-active) solid 2px;
    }

    input {
        vertical-align: bottom;
        height: 39px;
        margin: 0;

        &:focus {
            outline: none;
        }

        &[type="text"] {
            width: 169px;
            border-right: 0;
        }

        &[type="reset"] {
            background-color: var(--int-very-light-grey-hover);
            width: 39px;
            border-left: 0;
            font-size: 1.2rem;
            cursor: pointer;

            &:disabled {
                background-color: var(--int-very-light-grey);
                color: var(--int-very-light-grey-hover);
                cursor: initial;
            }

            &:hover:not(:disabled) {
                background-color: var(--int-light-grey);
            }

            &:active:not(:disabled) {
                background-color: var(--int-light-grey-hover);
            }
        }
    }
}
</style>
