package org.ivdnt.galahad.util

fun <K, V> Map<K, V>.merge(other: Map<K, V>, mapping: (V, V) -> V): Map<K, V> =
    this.toMutableMap().apply { other.forEach { (k, v) -> merge(k, v!!, mapping) } }