<template>
    <h2>Step 1: Corpora</h2>
    <ol>
        <li>Click <code class="green">new</code> to create a new corpus.</li>
        <li>Select a corpus and click <code class="orange">edit</code> to modify the corpus metadata</li>
        <li>Select a corpus and click <code class="red">delete</code> to remove the selected corpus</li>
    </ol>
</template>

<style scoped lang="scss">
code {
    font-size: 1rem;
    padding: 0.2rem 0.3rem;

    &.green{
        background-color: var(--int-green);
    }
    &.orange{
        background-color: var(--int-orange);
    }
    &.red{
        background-color: var(--int-red);
    }
}
</style>
