package org.ivdnt.galahad.formats.naf

import org.ivdnt.galahad.formats.Resource
import org.ivdnt.galahad.formats.assertPlaintextAndSourcelayer
import org.junit.jupiter.api.Test

class NafImportTest {
    @Test
    fun `Import a NAF`() {
        assertPlaintextAndSourcelayer("naf/import", NAFFile(Resource.get("naf/import/input.naf.xml")))
    }
}