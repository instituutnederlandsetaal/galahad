package org.ivdnt.galahad.formats

import org.ivdnt.galahad.data.layer.Layer

interface SourceLayerableFile {

    fun sourceLayer(): Layer

}