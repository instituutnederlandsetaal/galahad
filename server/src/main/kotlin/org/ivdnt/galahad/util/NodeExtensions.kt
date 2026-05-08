package org.ivdnt.galahad.util

import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.deepcopy(): ArrayList<Node> {
    val copy = ArrayList<Node>()
    for (i in 0 until this.length) {
        copy.add(this.item(i))
    }
    return copy
}

val Node.children: Sequence<Node>
    get() =
        object : Sequence<Node> {
            override fun iterator(): Iterator<Node> =
                object : Iterator<Node> {
                    var index = 0

                    override fun hasNext(): Boolean = index < childNodes.length

                    override fun next(): Node = childNodes.item(index++)
                }
        }

val Node.childElements: Sequence<Element>
    get() = children.mapNotNull { it.takeIf { it.nodeType == Node.ELEMENT_NODE } as Element? }

/** Whether this node is contained in a node with name [tagName] */
fun Node.containedIn(tagName: String): Boolean {
    if (this.parentNode == null) return false
    if (this.parentNode?.localName == tagName) return true
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

fun Node.child(tag: String): Node = childElements.first { it.tagName == tag }

fun Node.childOrNull(tag: String): Node? = childElements.firstOrNull { it.tagName == tag }

fun Element.childOrNull(tag: String): Element? = (this as Node).childOrNull(tag) as Element?
