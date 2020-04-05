package com.netherpyro.glcv.compose

import android.net.Uri

/**
 * @author mmikhailov on 03.04.2020.
 */
internal data class Sequence(
        val tag: String,
        val uri: Uri,
        var startDelayMs: Long,
        var durationMs: Long
): Controllable
