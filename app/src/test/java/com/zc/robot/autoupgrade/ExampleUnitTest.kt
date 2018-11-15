package com.zc.robot.autoupgrade

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val set = HashSet<String>()
        set.add("1")
        set.add("2")
        set.add("3")
        set.add("4")
        set.add("5")
        set.add("6")
        print(set.toString())

        val iterator = set.iterator()
        while (iterator.hasNext()) {
            val it = iterator.next()

            if (it.contentEquals("3"))
                iterator.remove()
        }
        print(set.toString())

        assertEquals(4, 2 + 2)
    }
}
