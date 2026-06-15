package com.javanapps.musicplayer.core.analysis.dsp

import kotlin.math.cos
import kotlin.math.sin

object Fft {
    fun transform(
        re: FloatArray,
        im: FloatArray,
    ) {
        val n = re.size
        require(n and (n - 1) == 0) { "FFT length must be a power of two, was $n" }
        bitReverse(re, im)
        var len = 2
        while (len <= n) {
            butterfly(re, im, len)
            len = len shl 1
        }
    }

    private fun bitReverse(
        re: FloatArray,
        im: FloatArray,
    ) {
        val n = re.size
        var j = 0
        for (i in 1 until n) {
            var bit = n shr 1
            while (j and bit != 0) {
                j = j xor bit
                bit = bit shr 1
            }
            j = j or bit
            if (i < j) {
                re[i] = re[j].also { re[j] = re[i] }
                im[i] = im[j].also { im[j] = im[i] }
            }
        }
    }

    private fun butterfly(
        re: FloatArray,
        im: FloatArray,
        len: Int,
    ) {
        val angle = -2.0 * Math.PI / len
        val wRe = cos(angle).toFloat()
        val wIm = sin(angle).toFloat()
        var i = 0
        while (i < re.size) {
            var curRe = 1f
            var curIm = 0f
            for (k in 0 until len / 2) {
                val a = i + k
                val b = a + len / 2
                val vRe = re[b] * curRe - im[b] * curIm
                val vIm = re[b] * curIm + im[b] * curRe
                re[b] = re[a] - vRe
                im[b] = im[a] - vIm
                re[a] += vRe
                im[a] += vIm
                val nextRe = curRe * wRe - curIm * wIm
                curIm = curRe * wIm + curIm * wRe
                curRe = nextRe
            }
            i += len
        }
    }
}
