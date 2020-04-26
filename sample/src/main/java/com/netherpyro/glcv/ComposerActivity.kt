package com.netherpyro.glcv

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.netherpyro.glcv.baker.renderToVideoFile
import com.netherpyro.glcv.compose.Composer
import kotlinx.android.synthetic.main.activity_compose.*
import java.io.File

/**
 * @author mmikhailov on 2019-11-30.
 */
class ComposerActivity : AppCompatActivity() {

    private val composer = Composer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        LibraryHelper.setContext(applicationContext)

        composer.setViewportColor(Color.CYAN)
        composer.setBaseColor(Color.YELLOW)
        composer.setAspectRatio(16 / 9f)
        composer.setGlView(glView)

        // todo pick content uri
        composer.addImage("image1", Uri.parse("content://media/external/file/129"), startMs = 1000L)
        composer.addImage("image2", Uri.parse("content://media/external/file/135"), startMs = 1500L)
        composer.addImage("image3", Uri.parse("content://media/external/file/136"))
        // sphere
        composer.addVideo("video1", Uri.parse("content://media/external/file/3365"), trimmedDuration = 2000L)
        // filmm
        composer.addVideo("video2", Uri.parse("content://media/external/file/4024"))
        // tiger
        //composer.addVideo("video3", Uri.parse("content://media/external/file/3370"))

        a1_1.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_1_1.value, true) }
        a3_2.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_3_2.value, true) }
        a2_3.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_2_3.value, true) }
        a4_5.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_4_5.value, true) }
        a5_4.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_5_4.value, true) }
        a9_16.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_9_16.value, true) }
        a16_9.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_16_9.value, true) }
        a18_9.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_18_9.value, true) }
        a9_18.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_9_18.value, true) }

        btn_render.setOnClickListener {
            composer.renderToVideoFile(
                    this@ComposerActivity,
                    File(cacheDir, "result.mp4").absolutePath,
                    outputMinSidePx = 1080,
                    fps = 30
            )
        }

        bottomView.alsoOnLaid { bottomView ->
            val maxHeight = container.height / 2
            bottomSeek.progress = ((bottomView.height / maxHeight.toFloat()) * 100).toInt()
            glView.setViewportMargin(bottom = bottomView.height)

            bottomSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                    val viewHeight = (maxHeight * (progress / 100f)).toInt()
                    bottomView.layoutParams = bottomView.layoutParams.apply {
                        height = viewHeight
                    }

                    glView.setViewportMargin(bottom = viewHeight)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        topView.alsoOnLaid { topView ->
            val maxHeight = container.height / 2
            topSeek.progress = ((topView.height / maxHeight.toFloat()) * 100).toInt()
            glView.setViewportMargin(top = topView.height)

            topSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val viewHeight = (maxHeight * (progress / 100f)).toInt()
                    topView.layoutParams = topView.layoutParams.apply {
                        height = viewHeight
                    }

                    glView.setViewportMargin(top = viewHeight)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    fun <T : View> T.alsoOnLaid(block: (T) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block.invoke(this@alsoOnLaid)
            }
        })
    }
}