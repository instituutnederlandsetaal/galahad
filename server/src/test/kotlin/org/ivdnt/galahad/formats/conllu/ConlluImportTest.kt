package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.assertPlaintextAndSourcelayer
import org.junit.jupiter.api.Test

class ConlluImportTest {

    // Conllu makes use of the TSV implementation. TSVs have headers, so the first line of the file is parsed differently.
    // In particular, we need to test whether a Conllu file that already has body values on the first line is parsed correctly.
    @Test
    fun `Parse basic Conllu with no leading newlines`() {
        val file = ConlluFile(Resource.get("conllu/basic/input.conllu"))
        assertPlaintextAndSourcelayer("conllu/basic", file)
    }

    @Test
    fun `Parse Conllu with comments`() {
        val file = ConlluFile(Resource.get("conllu/comments/input.conllu"))
        assertPlaintextAndSourcelayer("conllu/comments", file)
    }
}