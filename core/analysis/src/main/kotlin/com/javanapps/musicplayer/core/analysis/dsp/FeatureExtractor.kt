package com.javanapps.musicplayer.core.analysis.dsp

import com.javanapps.musicplayer.core.analysis.decode.PcmAudio
import com.javanapps.musicplayer.core.model.AudioFeatures
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sqrt

class FeatureExtractor
    @Inject
    constructor() {
        private val window: FloatArray =
            FloatArray(FRAME) { 0.5f - 0.5f * cos(2.0 * PI * it / (FRAME - 1)).toFloat() }

        fun extract(pcm: PcmAudio): AudioFeatures {
            val samples = pcm.samples
            if (samples.size < FRAME) return empty(pcm)

            val acc = Accumulator()
            val onset = ArrayList<Float>()
            var previousMagnitude: FloatArray? = null
            var position = 0
            while (position + FRAME <= samples.size) {
                val magnitude = analyzeFrame(samples, position, pcm.sampleRate, acc)
                val flux = spectralFlux(previousMagnitude, magnitude)
                acc.fluxSum += flux
                onset.add(flux.toFloat())
                previousMagnitude = magnitude
                position += HOP
            }
            val tempo = estimateTempo(onset.toFloatArray(), pcm.sampleRate)
            return acc.toFeatures(pcm, tempo)
        }

        private fun analyzeFrame(
            samples: FloatArray,
            offset: Int,
            sampleRate: Int,
            acc: Accumulator,
        ): FloatArray {
            val re = FloatArray(FRAME)
            val im = FloatArray(FRAME)
            var squareSum = 0.0
            var crossings = 0
            for (i in 0 until FRAME) {
                val sample = samples[offset + i]
                re[i] = sample * window[i]
                squareSum += (sample * sample).toDouble()
                if (i > 0 && (sample >= 0f) != (samples[offset + i - 1] >= 0f)) crossings++
            }
            acc.rmsSum += sqrt(squareSum / FRAME)
            acc.zcrSum += crossings.toDouble() / FRAME
            Fft.transform(re, im)
            return spectrum(re, im, sampleRate, acc)
        }

        private fun spectrum(
            re: FloatArray,
            im: FloatArray,
            sampleRate: Int,
            acc: Accumulator,
        ): FloatArray {
            val half = FRAME / 2
            val magnitude = FloatArray(half)
            var magnitudeSum = 0.0
            var weighted = 0.0
            var highFreq = 0.0
            for (k in 0 until half) {
                val m = sqrt((re[k] * re[k] + im[k] * im[k]).toDouble()).toFloat()
                magnitude[k] = m
                val frequency = k.toDouble() * sampleRate / FRAME
                magnitudeSum += m
                weighted += frequency * m
                if (frequency > HIGH_FREQ_CUTOFF_HZ) highFreq += m
            }
            acc.centroidSum += if (magnitudeSum > 0) weighted / magnitudeSum else 0.0
            acc.rolloffSum += rolloff(magnitude, magnitudeSum, sampleRate)
            acc.highFreqSum += highFreq
            acc.magnitudeTotal += magnitudeSum
            acc.frames++
            return magnitude
        }

        private fun rolloff(
            magnitude: FloatArray,
            magnitudeSum: Double,
            sampleRate: Int,
        ): Double {
            if (magnitudeSum <= 0) return 0.0
            var cumulative = 0.0
            for (k in magnitude.indices) {
                cumulative += magnitude[k]
                if (cumulative >= ROLLOFF_RATIO * magnitudeSum) return k.toDouble() * sampleRate / FRAME
            }
            return 0.0
        }

        private fun spectralFlux(
            previous: FloatArray?,
            current: FloatArray,
        ): Double {
            if (previous == null) return 0.0
            var sum = 0.0
            for (k in current.indices) {
                val delta = current[k] - previous[k]
                if (delta > 0) sum += (delta * delta).toDouble()
            }
            return sqrt(sum)
        }

        private fun estimateTempo(
            onset: FloatArray,
            sampleRate: Int,
        ): Tempo {
            if (onset.size < MIN_ONSET_FRAMES) return Tempo(0f, 0f)
            val framesPerSecond = sampleRate.toDouble() / HOP
            val minLag = (framesPerSecond * SECONDS_PER_MINUTE / TEMPO_MAX_BPM).toInt().coerceAtLeast(1)
            val maxLag = (framesPerSecond * SECONDS_PER_MINUTE / TEMPO_MIN_BPM).toInt().coerceAtMost(onset.size - 1)
            var energy = 0.0
            for (value in onset) energy += (value * value).toDouble()
            if (energy <= 0.0) return Tempo(0f, 0f)

            var bestLag = 0
            var bestScore = 0.0
            for (lag in minLag..maxLag) {
                var score = 0.0
                for (i in lag until onset.size) score += (onset[i] * onset[i - lag]).toDouble()
                if (score > bestScore) {
                    bestScore = score
                    bestLag = lag
                }
            }
            val bpm = if (bestLag > 0) (SECONDS_PER_MINUTE * framesPerSecond / bestLag).toFloat() else 0f
            return Tempo(bpm, (bestScore / energy).coerceIn(0.0, 1.0).toFloat())
        }

        private fun empty(pcm: PcmAudio) = AudioFeatures(pcm.durationMs, pcm.sampleRate, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)

        private data class Tempo(
            val bpm: Float,
            val strength: Float,
        )

        private class Accumulator {
            var rmsSum = 0.0
            var zcrSum = 0.0
            var centroidSum = 0.0
            var rolloffSum = 0.0
            var fluxSum = 0.0
            var highFreqSum = 0.0
            var magnitudeTotal = 0.0
            var frames = 0

            fun toFeatures(
                pcm: PcmAudio,
                tempo: Tempo,
            ): AudioFeatures {
                val count = frames.coerceAtLeast(1)
                return AudioFeatures(
                    durationMs = pcm.durationMs,
                    sampleRate = pcm.sampleRate,
                    rmsEnergy = normalize(rmsSum / count, RMS_NORM_SCALE),
                    zeroCrossingRate = (zcrSum / count).toFloat(),
                    spectralCentroid = (centroidSum / count).toFloat(),
                    spectralRolloff = (rolloffSum / count).toFloat(),
                    spectralFlux = normalize(fluxSum / count, FLUX_NORM_SCALE),
                    highFreqRatio = if (magnitudeTotal > 0) (highFreqSum / magnitudeTotal).toFloat() else 0f,
                    estimatedTempoBpm = tempo.bpm,
                    beatStrength = tempo.strength,
                )
            }

            private fun normalize(
                value: Double,
                scale: Double,
            ): Float = (value / scale).coerceIn(0.0, 1.0).toFloat()
        }

        private companion object {
            const val FRAME = 2048
            const val HOP = 1024
            const val ROLLOFF_RATIO = 0.85
            const val HIGH_FREQ_CUTOFF_HZ = 5000.0
            const val RMS_NORM_SCALE = 0.3
            const val FLUX_NORM_SCALE = 5.0
            const val TEMPO_MIN_BPM = 60.0
            const val TEMPO_MAX_BPM = 180.0
            const val SECONDS_PER_MINUTE = 60.0
            const val MIN_ONSET_FRAMES = 4
        }
    }
