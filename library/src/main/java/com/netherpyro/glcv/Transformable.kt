package com.netherpyro.glcv

import android.graphics.RectF
import androidx.annotation.ColorInt
import androidx.annotation.Px

/**
 * @author mmikhailov on 2019-12-04.
 */
interface Transformable {

    val id: Int
    val tag: String?

    fun setRotation(rotationDeg: Float)
    fun setScale(scaleFactor: Float)
    fun setTranslation(@Px x: Float, @Px y: Float)
    fun setOpacity(opacity: Float)
    fun setBorder(width: Float, @ColorInt color: Int)

    fun getRotation(): Float
    fun getScale(): Float
    fun getTranslation(): Pair<Float, Float>
    fun getFrustumRect(): RectF
    fun getLayerAspect(): Float
}