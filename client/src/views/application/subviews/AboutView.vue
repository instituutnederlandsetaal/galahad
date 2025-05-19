<template>
    <div>
        <GCard title="About">
            <template #help>
                Here you can find information about the application and the exact version you are currently running.
            </template>
            <section>
                <p>
                    GaLAHaD is created by the
                    <ExternalLink href="https://www.ivdnt.org">Dutch Language Institute </ExternalLink>. For this work,
                    funding was received from NWO.
                    <br />
                    (Clariah Plus project 184.034.023 & SSHOC-NL 184.036.020)
                </p>

                <div class="logo">
                    <a href="https://www.ivdnt.org">
                        <img
                            src="@/assets/ivdNt-logo-3regels.svg"
                            alt="Logo Instituut voor de Nederlandse taal"
                            title="Instituut voor de Nederlandse taal"
                        />
                    </a>

                    <a href="https://www.clariah.nl">
                        <img src="@/assets/clariah.png" alt="Clariah logo" title="Clariah" />
                    </a>
                    <a href="https://www.nwo.nl/projecten/184036020">
                        <img src="@/assets/sshoc-nl.jpg" alt="SSHOC-NL logo" title="SSHOC-NL" />
                    </a>
                </div>
            </section>
            <section>
                <h2>Colofon</h2>
                <h3>Over het logo</h3>
                <i>Verbaasd laat Galahad de graal uit zijn handen vallen als hij ziet wat erin zit.</i>
                <br />
                Voor deze aquarel liet Karina van Dalen-Oskam zich inspireren door een detail van een miniatuur uit een
                handschrift van
                <ExternalLink href="https://manuscripts.kb.nl/zoom/BYVANCKB%3Amimi_ka20%3A052r_min_b-c">
                    Jacob van Maerlants Spiegel Historiael (Den Haag, Koninklijke Bibliotheek, KA XX fol. 52r) </ExternalLink
                >.
            </section>
            <section>
                <h2 id="version">Version</h2>
                <KeyValues :data="versionYaml" />
            </section>
        </GCard>
    </div>
</template>

<script setup lang="ts">
import versionYaml from "@/../version.yml"
import { GCard, KeyValues } from "@/components"
</script>

<style scoped="true">
.logo {
    display: flex;
    flex-direction: row;
    gap: 1em;
    align-items: center;
    flex-wrap: wrap;
    justify-content: center;

    img {
        width: 200px;
    }
}

:deep(div.title) h3 {
    margin-bottom: 0;
}

h3 {
    margin-top: -0.8em;
    margin-bottom: 0.5em;
}

h2#version {
    margin-bottom: 0;
}

:deep(div.content) {
    max-width: 800px;
}
</style>
