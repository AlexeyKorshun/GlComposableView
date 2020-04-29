package com.netherpyro.glcv

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.netherpyro.glcv.baker.BakeProgressReceiver
import com.netherpyro.glcv.baker.Cancellable
import com.netherpyro.glcv.baker.renderToVideoFile
import com.netherpyro.glcv.baker.renderToVideoFileInSeparateProcess
import com.netherpyro.glcv.compose.Composer
import com.netherpyro.glcv.touches.LayerTouchListener
import kotlinx.android.synthetic.main.activity_compose.*
import java.io.File

/**
 * @author mmikhailov on 2019-11-30.
 */
// todo handle broadcast receiver when lifecycle changes
// todo request storage permission
class ComposerActivity : AppCompatActivity() {

    private val mediaRequestCode = 7879
    private val composer = Composer()

    private val progressReceiver = BakeProgressReceiver { progress, completed ->
        handleProgress(progress, completed)
    }

    private val transformableList = mutableListOf<Transformable>()

    private var bakeProcess: Cancellable? = null
    private var isReceiverRegistered = false
    private var startTimeNsec: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        LibraryHelper.setContext(applicationContext)

        glView.enableGestures = true
        composer.setViewportColor(Color.CYAN)
        composer.setBaseColor(Color.YELLOW)
        composer.setAspectRatio(16 / 9f)
        composer.setGlView(glView)

        // tiger
        /*composer.addVideo(
                "video3",
                Uri.parse("content://media/external/file/3370"),
                trimmedDuration = 2000L
        ) { transformable -> transformableList.add(transformable) }*/

        /*composer.addImage(
                "image1",
                Uri.parse("content://media/external/file/129"),
                startMs = 1000L
        ) { transformable -> transformableList.add(transformable) }

        composer.addImage(
                "image2",
                Uri.parse("content://media/external/file/135"),
                startMs = 1500L
        ) { transformable -> transformableList.add(transformable) }

        composer.addImage(
                "image3",
                Uri.parse("content://media/external/file/136")
        ) { transformable -> transformableList.add(transformable) }

        // sphere
        composer.addVideo(
                "video1",
                Uri.parse("content://media/external/file/3365")
        ) { transformable -> transformableList.add(transformable) }*/

        // filmm
        /*composer.addVideo(
                "video2",
                Uri.parse("content://media/external/file/4024")
        ) { transformable -> transformableList.add(transformable) }*/

        //  with audio
        /*composer.addVideo(
                "video4",
                Uri.parse("content://media/external/file/3371")
        ) { transformable -> transformableList.add(transformable) }*/

        // harlem shake
        /*composer.addVideo(
                "video5",
                Uri.parse("content://media/external/file/342")
        ) { transformable -> transformableList.add(transformable) }*/

        // audio video sync
        /*composer.addVideo(
                "video6",
                Uri.parse("content://media/external/file/3366")
        ) { transformable -> transformableList.add(transformable) }*/

        // rabbit
        /*composer.addVideo(
                "video7",
                Uri.parse("content://media/external/file/3372")
        ) { transformable -> transformableList.add(transformable) }*/

        a1_1.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_1_1.value, true) }
        a3_2.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_3_2.value, true) }
        a2_3.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_2_3.value, true) }
        a4_5.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_4_5.value, true) }
        a5_4.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_5_4.value, true) }
        a9_16.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_9_16.value, true) }
        a16_9.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_16_9.value, true) }
        a18_9.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_18_9.value, true) }
        a9_18.setOnClickListener { composer.setAspectRatio(AspectRatio.RATIO_9_18.value, true) }

        btn_pick.setOnClickListener {
            startActivityForResult(
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        .apply { type = "image/* video/*" },
                    mediaRequestCode
            )
        }

        btn_render.setOnClickListener {
            startTimeNsec = System.nanoTime()
            // todo show dialog with cancel action
            bakeProcess = composer.renderToVideoFile(
                    this@ComposerActivity,
                    File(cacheDir, "result.mp4").absolutePath,
                    outputMinSidePx = 1080,
                    fps = 30,
                    verboseLogging = true,
                    progressListener = { progress: Float, completed: Boolean ->
                        runOnUiThread { handleProgress(progress, completed) }
                    }
            )
        }

        btn_render_service.setOnClickListener {
            startTimeNsec = System.nanoTime()
            // todo show dialog with cancel action
            registerReceiver(progressReceiver, IntentFilter(BakeProgressReceiver.ACTION_PUBLISH_PROGRESS))
            isReceiverRegistered = true
            bakeProcess = composer.renderToVideoFileInSeparateProcess(
                    this@ComposerActivity,
                    File(cacheDir, "result.mp4").absolutePath,
                    outputMinSidePx = 1080,
                    fps = 30,
                    verboseLogging = true
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

        glView.listenTouches(object : LayerTouchListener {
            override fun onLayerTap(transformable: Transformable): Boolean {
                transformableList.forEach {
                    val clicked = it.id == transformable.id
                    it.enableGesturesTransform = clicked
                    it.setBorder(if (clicked) 1f else 0f, Color.GREEN)

                    if (clicked) it.setLayerPosition(transformableList.lastIndex)
                }

                return true
            }

            override fun onViewportInsideTap(): Boolean {
                transformableList.forEach {
                    it.enableGesturesTransform = false
                    it.setBorder(0f, Color.GREEN)
                }

                return true
            }

            override fun onViewportOutsideTap(): Boolean {
                transformableList.forEach {
                    if (it.enableGesturesTransform) {
                        it.setBorder(1f, Color.BLUE)
                    }
                }

                return true
            }
        })
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (intent != null && requestCode == mediaRequestCode && resultCode == Activity.RESULT_OK) {
            val mediaUri = intent.data!!
            composer.addMedia(mediaUri.toString(), mediaUri) { transformable ->
                transformableList.add(transformable)
            }
        }
    }

    private fun Long.toSeconds() = this / 1_000_000_000f

    fun <T : View> T.alsoOnLaid(block: (T) -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                block.invoke(this@alsoOnLaid)
            }
        })
    }

    private fun handleProgress(value: Float, completed: Boolean) {
        Log.d("ComposerActivity", "handleProgress::$value : $completed")

        if (completed) {
            Log.d("ComposeActivity", "handleProgress::completed for ${(System.nanoTime() - startTimeNsec).toSeconds()} seconds")

            if (isReceiverRegistered) {
                isReceiverRegistered = false
                unregisterReceiver(progressReceiver)
            }
        }
    }
}