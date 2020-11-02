package com.fpinkotlin.workingwithlaziness.exercise15

import com.fpinkotlin.common.List
import com.fpinkotlin.common.Result


sealed class Stream<out A> {

    abstract fun isEmpty(): Boolean

    abstract fun head(): Result<A>

    abstract fun tail(): Result<Stream<A>>

    abstract fun takeAtMost(n: Int): Stream<A>

    abstract fun toList(): List<A>

    fun dropAtMost(n: Int): Stream<A> = dropAtMost(n, this)

    private object Empty: Stream<Nothing>() {

        override fun takeAtMost(n: Int): Stream<Nothing> = this

        override fun head(): Result<Nothing> = Result()

        override fun tail(): Result<Nothing> = Result()

        override fun isEmpty(): Boolean = true

        override fun toList(): List<Nothing> = List()

    }

    private class Cons<out A> (internal val hd: Lazy<A>,
                               internal val tl: Lazy<Stream<A>>) : Stream<A>() {

        override fun takeAtMost(n: Int): Stream<A> = when {
            n > 0 -> cons(hd, Lazy { tl().takeAtMost(n - 1) })
            else -> Empty
        }

        override fun head(): Result<A> = Result(hd())

        override fun tail(): Result<Stream<A>> = Result(tl())

        override fun isEmpty(): Boolean = false

        override fun toList(): List<A> {
            tailrec fun toList(acc: List<A>, stream: Stream<A>): List<A> =
                    when (stream) {
                        is Empty -> acc
                        is Cons -> toList(acc.cons(stream.hd()), stream.tl())
                    }
            return toList(List(), this).reverse()
        }
    }

    companion object {

        fun <A> cons(hd: Lazy<A>, tl: Lazy<Stream<A>>): Stream<A> = Cons(hd, tl)

        operator fun <A> invoke(): Stream<A> = Empty

        fun from(i: Int): Stream<Int> = cons(Lazy { i }, Lazy { from(i + 1) })

        fun <A> repeat(f: () -> A): Stream<A> = cons(Lazy { f() }, Lazy { repeat(f) })

        tailrec fun <A> dropAtMost(n: Int, stream: Stream<A>): Stream<A> =  when {
            n > 0 -> when (stream) {
                Empty -> stream
                is Cons -> dropAtMost(n - 1, stream.tl())
            }
            else -> stream
        }
    }
}
