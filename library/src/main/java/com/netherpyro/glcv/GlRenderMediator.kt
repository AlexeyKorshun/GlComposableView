package com.netherpyro.glcv

import android.graphics.Bitmap
import com.google.android.exoplayer2.SimpleExoPlayer
import com.netherpyro.glcv.layer.ExoPLayer
import com.netherpyro.glcv.layer.ImageLayer
import com.netherpyro.glcv.layer.Layer

/**
 * @author mmikhailov on 2019-11-30.
 */
internal class GlRenderMediator(private val renderHost: RenderHost) : Invalidator {

    private val layers = mutableListOf<Layer>()

    override fun invalidate() {
        renderHost.requestDraw()
    }

    fun onSurfaceCreated() {
        layers.forEach { it.onGlPrepared() }
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        renderHost.onSurfaceChanged(width, height)
    }

    fun addExoPlayerLayer(player: SimpleExoPlayer, applyLayerAspect: Boolean): Transformable {
        return ExoPLayer(player, this)
            .also { addLayer(it, applyLayerAspect) }
    }

    fun addImageLayer(bitmap: Bitmap, applyLayerAspect: Boolean): Transformable {
        return ImageLayer(bitmap, this)
            .also { addLayer(it, applyLayerAspect) }
    }

    fun onViewportChanged(viewport: GlViewport) {
        val aspect = viewport.width / viewport.height.toFloat()
        layers.forEach { it.onViewportAspectRatioChanged(aspect) }
    }

    fun onDrawFrame(fbo: FramebufferObject) {
        // todo use FBOs
        layers.forEach { it.onDrawFrame() }
    }

    fun release() {
        layers.forEach { it.release() }
    }

    private fun addLayer(layer: Layer, applyLayerAspect: Boolean) {
        if (applyLayerAspect) layer.listenAspectRatioReady { renderHost.onLayerAspectRatio(it) }

        layers.add(layer)
    }
}