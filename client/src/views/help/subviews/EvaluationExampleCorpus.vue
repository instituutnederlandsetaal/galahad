<template>
    <table class="compact">
        <thead>
            <tr>
                <th>Token</th>
                <th>Actual PoS</th>
                <th>Predicted PoS</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>lopen (to run)</td>
                <td>verb</td>
                <td>noun</td>
            </tr>
            <tr>
                <td>snel (fast)</td>
                <td>adjective</td>
                <td>adjective</td>
            </tr>
            <tr>
                <td>hond (dog)</td>
                <td>noun</td>
                <td>noun</td>
            </tr>
            <tr>
                <td>mooie (beautiful)</td>
                <td>adjective</td>
                <td>adjective</td>
            </tr>
            <tr>
                <td>spring (jump)</td>
                <td>verb</td>
                <td>verb</td>
            </tr>
            <tr>
                <td>katten (cats)</td>
                <td>noun</td>
                <td>noun</td>
            </tr>
            <tr>
                <td>lui (lazy)</td>
                <td>adjective</td>
                <td>noun</td>
            </tr>
            <tr>
                <td>vos (fox)</td>
                <td>noun</td>
                <td>noun</td>
            </tr>
            <tr>
                <td>speelde (played)</td>
                <td>verb</td>
                <td>verb</td>
            </tr>
            <tr>
                <td>blij (happy)</td>
                <td>adjective</td>
                <td>adjective</td>
            </tr>
        </tbody>
    </table>
</template>

<style scoped lang="scss">
table {
    border-collapse: collapse;
    margin: 0 auto;
    margin-top: 5px;
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
}
</style>
