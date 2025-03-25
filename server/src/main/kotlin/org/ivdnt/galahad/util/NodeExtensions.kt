package org.ivdnt.galahad.util

import org.ivdnt.galahad.formats.xml.tagName
import org.w3c.dom.Element
import org.w3c.dom.Node

val Node.children: Sequence<Node>
    get() = object : Sequence<Node> {
    override fun iterator(): Iterator<Node> = object : Iterator<Node> {
        var index = 0
        override fun hasNext(): Boolean = index < childNodes.length
        override fun next(): Node = childNodes.item(index++)
    }
}

val Node.childElements: Sequence<Element>
    get() = object : Sequence<Element> {
    override fun iterator(): Iterator<Element> = object : Iterator<Element> {
        var index = 0
        override fun hasNext(): Boolean = index < childNodes.length
        override fun next(): Element {
            val node = childNodes.item(index++)
            if (node.nodeType == Node.ELEMENT_NODE) {
                return node as Element
            }
            return next()
        }
    }
}

/** Whether this node is contained in a node with name [tagName]*/
fun Node.containedIn(tagName: String): Boolean {
    if (this.parentNode == null)
        return false
    if (this.parentNode?.tagName() == tagName)
        return true
    // Recursion
    return this.parentNode.containedIn(tagName)
}

/** Insert [newChild] as a child of this node, placed after [refChild]. */
fun Node.insertAfter(newChild: Node, refChild: Node) {
    if (refChild.nextSibling != null) {
        this.insertBefore(newChild, refChild.nextSibling)
    } else {
        this.appendChild(newChild)
    }
}

/** Insert [newChild] as the first child of this node. */
fun Node.insertFirst(newChild: Node) {
    this.insertBefore(newChild, this.firstChild)
}

/** Returns the next sibling of the node that is not text. */
fun Node.nextElementSibling(): Element? {
    var next = this.nextSibling
    while (next != null && next.nodeType != Node.ELEMENT_NODE) {
        next = next.nextSibling
    }
    return next as Element?
}

/** Looks for the first child node, 1 deep, or null. */
fun Node.childOrNull(childTag: String, recurse: Boolean = false): Node? {
    for (i in 0 until this.childNodes.length) {
        if (this.childNodes.item(i).nodeType == Node.ELEMENT_NODE) {
            if ((this.childNodes.item(i) as Element).tagName == childTag) {
                return this.childNodes.item(i)
            }
            val childReturn = this.childNodes.item(i).childOrNull(childTag, recurse)
            if (childReturn != null) {
                return childReturn
            }
        }
    }
    return null
}

/** Looks for the first child node, 1 deep, or null. */
fun Element.childOrNull(childTag: String): Element? = (this as Node).childOrNull(childTag) as Element?
