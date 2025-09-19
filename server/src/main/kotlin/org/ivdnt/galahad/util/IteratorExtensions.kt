package org.ivdnt.galahad.util

import org.ivdnt.galahad.annotations.Term

/** An iterator that saves the current item. */
class TermIterator : Iterator<Term?> {
    var current: Term? = null
        private set
    var chars: Int = 0
        private set

    private val iter: Iterator<Term?>

    constructor(iter: Iterator<Term?>) {
        this.iter = iter
        next()
    }

    override fun hasNext(): Boolean = iter.hasNext()

    override fun next(): Term? {
        // add length of now previous term
        if (current != null) {
            chars += (current as Term).token.count { !it.isWhitespace() }
        }
        current = if (iter.hasNext()) iter.next() else null
        return current
    }
}