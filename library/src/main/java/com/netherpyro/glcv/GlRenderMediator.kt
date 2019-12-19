package com.netherpyro.glcv

import android.graphics.Bitmap
import com.google.android.exoplayer2.SimpleExoPlayer
import com.netherpyro.glcv.layer.ExoPLayer
import com.netherpyro.glcv.layer.ImageLayer
import com.netherpyro.glcv.layer.Layer

/**
 * @author mmikhailov on 2019-11-30.
 */
internal class GlRenderMediator(private val renderHost: RenderHost) : Invalidator, Observable {

    /**
     * The list of ready for use layers
     * */
    private val layers = mutableListOf<Layer>()

    private var nextId = 0
    private var surfaceReady = false
    private var viewportAspect = 1f
    private var addLayerAction: ((Transformable) -> Unit)? = null
    private var removeLayerAction: ((Int) -> Unit)? = null

    override fun invalidate() {
        renderHost.requestDraw()
    }

    override fun subscribe(addAction: (Transformable) -> Unit, removeAction: (Int) -> Unit): List<Transformable> {
        this.addLayerAction = addAction
        this.removeLayerAction = removeAction

        return layers.toList()
    }

    fun onSurfaceChanged(width: Int, height: Int) {
        renderHost.onSurfaceChanged(width, height)
    }

    @Synchronized
    fun onSurfaceCreated() {
        surfaceReady = true

        layers.forEach { it.setup() }
    }

    @Synchronized
    fun onViewportChanged(viewport: GlViewport) {
        viewportAspect = viewport.width / viewport.height.toFloat()

        layers.forEach { it.onViewportAspectRatio(viewportAspect) }
    }

    @Synchronized
    fun onDrawFrame(fbo: FramebufferObject) {
        // todo use FBOs
        layers.forEach { it.draw() }
    }

    @Synchronized
    fun release() {
        surfaceReady = false

        layers.forEach { it.release() }
    }

    fun addVideoLayer(tag: String?, player: SimpleExoPlayer, applyLayerAspect: Boolean): Transformable {
        return ExoPLayer(nextId++, tag, this, player)
            .also { addLayer(it, applyLayerAspect) }
    }

    fun addImageLayer(tag: String?, bitmap: Bitmap, applyLayerAspect: Boolean): Transformable {
        return ImageLayer(nextId++, tag, this, bitmap)
            .also { addLayer(it, applyLayerAspect) }
    }

    fun bringLayerToFront(transformable: Transformable) {
        bringLayerToPosition(layers.lastIndex, transformable)
    }

    @Synchronized
    fun restoreLayersOrder() {
        layers.sortBy { it.id }

        invalidate()
    }

    @Synchronized
    fun removeLayer(transformable: Transformable) {
        with(layers) {
            removeAt(indexOfFirst { it.id == transformable.id }).release()
            removeLayerAction?.invoke(transformable.id)
        }

        invalidate()
    }

    @Synchronized
    private fun addLayer(layer: Layer, applyLayerAspect: Boolean) {
        if (applyLayerAspect) layer.listenAspectRatioReady { renderHost.onLayerAspectRatio(it) }

        if (surfaceReady) {
            renderHost.postAction(Runnable {
                layer.setup()
                layer.onViewportAspectRatio(viewportAspect)
                layers.add(layer)
                addLayerAction?.invoke(layer)

                invalidate()
            })
        } else {
            layers.add(layer)
            addLayerAction?.invoke(layer)
        }
    }

    @Synchronized
    fun bringLayerToPosition(position: Int, transformable: Transformable) {
        if (position >= 0) {
            val index = layers.indexOfFirst { it.id == transformable.id }
            val layer = layers.removeAt(index)
            layers.add(position, layer)

            invalidate()
        }
    }
}