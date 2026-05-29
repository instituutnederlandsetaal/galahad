<template>
    <fieldset>
        <input v-model="model" :type :placeholder :list ref="inputElement" />
        <input type="reset" value="&#10006;" :disabled="!model?.length" title="Clear" @click="model = ''" />
    </fieldset>
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
    focus,
} = defineProps<{
    type?: string
    placeholder?: string
    list?: string
    validityDescriptor?: string
    validator?: (value: unknown) => boolean
    focus?: boolean
}>()

const inputElement = useTemplateRef<HTMLInputElement>("inputElement")
onMounted(() => {
    if (focus) {
        nextTick(() => {
            inputElement.value?.focus()
        })
    }
})
</script>

<style scoped lang="scss">
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

fieldset {
    display: inline;
    vertical-align: inherit;
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

        &[type="text"],
        &[type="url"] {
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
