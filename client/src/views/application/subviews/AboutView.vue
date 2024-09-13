<template>
    <div>
        <GCard title="About">
            <template #help>
                Here you can find information about the application and the exact version you are
                currently running.
            </template>
            <p style="text-align: center;">
                GaLAHaD is created by the <ExternalLink href="https://www.ivdnt.org">Dutch Language Institute
                </ExternalLink>.
                <br />
                For this work, funding was received from NWO.
                <br />
                (Clariah Plus project 184.034.023 & SSHOC-NL 184.036.020)
            </p>

            <div class="logo">
                <a href="https://www.ivdnt.org">
                    <img src="@/assets/ivdNt-logo-3regels.svg" alt="Logo Instituut voor de Nederlandse taal"
                        title="Instituut voor de Nederlandse taal" />
                </a>

                <a href="https://www.clariah.nl">
                    <img src="@/assets/clariah.png" alt="Clariah logo" title="Clariah" />
                </a>
                <a href="https://www.nwo.nl/projecten/184036020">
                    <img src="@/assets/sshoc-nl.jpg" alt="SSHOC-NL logo" title="SSHOC-NL" />
                </a>
            </div>

            <h2>Version</h2>
            <KeyValues :data="versionYaml" />
        </GCard>
    </div>
</template>

<script setup lang='ts'>
import versionYaml from '@/../version.yml'
import { GCard, KeyValues } from '@/components'
</script>

<style scoped="true">
img {
    width: 200px;
}

.logo {
    display: flex;
    flex-direction: row;
    justify-content: left;
    gap: 1em;
    align-items: center;
    flex-wrap: wrap;
}

:deep(.content-wrapper)>.content {
    display: flex;
    flex-direction: column;
    align-items: center;
}
</style>
