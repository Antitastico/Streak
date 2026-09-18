package io.github.antitastico.streak.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import io.github.antitastico.streak.R

/**
 * Reproduce un sonido corto y suave al marcar un cumplido. Ligero (SoundPool),
 * pensado para efectos breves.
 */
class SoundFx(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(2)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private var loaded = false
    private val soundId = pool.load(context.applicationContext, R.raw.check, 1)

    init {
        pool.setOnLoadCompleteListener { _, _, status -> loaded = status == 0 }
    }

    fun play() {
        if (loaded) pool.play(soundId, 0.8f, 0.8f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}
