package org.ivdnt.galahad.util

object UserHeader

object SpringUtil

fun String.addUrlParams(params: Map<String, String>): String {
    return this + "?" + params.map { (k, v) -> "$k=$v" }.joinToString("&")
}