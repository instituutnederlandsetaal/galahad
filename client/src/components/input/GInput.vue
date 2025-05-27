<template>
    <!-- with clear button-->
    <fieldset class="clear" v-if="clearBtn">
        <input v-model="model" :type :placeholder :list ref="inputElement" @keyup.enter="$emit('enter')" />
        <input type="reset" value="&#10006;" :disabled="model === null || model.length == 0" title="Clear"
            @click="model = ''" />
    </fieldset>
    <!-- without clear button -->
    <input v-else v-model="model" :type :placeholder :list ref="inputElement" @keyup.enter="$emit('enter')" />
    <GValidator v-if="validator && validityDescriptor" :model :validator :validityDescriptor />
</template>

<script setup lang="ts">
const model = defineModel<string>()

const {
    type = "text",
    placeholder,
    list,
    validityDescriptor,
    validator,
    clearBtn,
    focus,
} = defineProps<{
    type?: string
    placeholder?: string
    list?: string
    validityDescriptor?: string
    validator?: (value: string) => boolean
    clearBtn?: boolean
    focus?: boolean
}>()

const inputElement = ref<HTMLInputElement>()
onMounted(() => {
    if (focus) {
        nextTick(() => {
            inputElement.value?.focus()
        })
    }
})
</script>

<style scoped lang="scss">
label {
    margin-bottom: 0px;
}

/* Inputs */
input[type="text"],
input[type="url"],
input[type="reset"] {
    font: inherit;
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

.clear {
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
