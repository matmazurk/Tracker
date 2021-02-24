package com.mat.tracker

import org.junit.Test
import java.lang.IllegalArgumentException

class UtilsTest {

    @Test(expected = IllegalArgumentException::class)
    fun test_long_conversion_to_HMMSS_wrong_value() {
        val wrongValue = -1L
        wrongValue.toHMMSS()
    }

    @Test
    fun test_long_conversion_to_HMMSS() {
        var testedValue = 61000L
        assert("0:01:01" == testedValue.toHMMSS())
        testedValue = 3601000L
        assert("1:00:01" == testedValue.toHMMSS())
        testedValue = 36124000L
        assert("10:02:04" == testedValue.toHMMSS())
    }

    @Test(expected = IllegalArgumentException::class)
    fun test_long_conversion_to_GPX_time_wrong_value() {
        val wrongValue = -3L
        wrongValue.toGpxTime()
    }

    @Test
    fun test_long_conversion_to_GPX_time() {
        var testedValue = 1614017794000L
        assert("2021-2-22T18:16:34Z" == testedValue.toGpxTime())
        testedValue = 945906562000L
        assert("1999-12-22T23:49:22Z" == testedValue.toGpxTime())
    }
}