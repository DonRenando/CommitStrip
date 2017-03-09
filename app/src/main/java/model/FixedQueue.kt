package model

import java.util.*

/**
 * Created by carnaude on 08/03/2017.
 */

class FixedQueue<T>(val maxSize: Int) : LinkedList<T>() {

    override fun add(element: T): Boolean {
        val r = super.add(element)
        if (this.size > maxSize) {
            removeRange(0, this.size - maxSize - 1)
        }
        return r
    }

    override fun push(e: T) {
        super.push(e)
        if (this.size > maxSize) {
            removeLast()
        }
    }
}
