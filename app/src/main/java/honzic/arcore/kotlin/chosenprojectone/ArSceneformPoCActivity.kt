package honzic.arcore.kotlin.chosenprojectone

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.widget.Toast
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode

/**
 * This is an example activity that uses the Sceneform UX package to make common AR tasks easier.
 */
class ArSceneformPoCActivity : AppCompatActivity(){

    private var arFragment: ArFragment? = null
    private var andyRenderable: ModelRenderable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            return
        }

        setContentView(R.layout.activity_ux)
        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?

        // When you build a Renderable, Sceneform loads its resources in the background while returning
        // a CompletableFuture. Call thenAccept(), handle(), or check isDone() before calling get().
        ModelRenderable.builder()
                .setSource(this, R.raw.andy)
                .build()
                .thenAccept { renderable -> andyRenderable = renderable }
                .exceptionally {
                    val toast = Toast.makeText(this, "Unable to load andy renderable", Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                    null
                }

        arFragment!!.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (andyRenderable == null) {
                return@setOnTapArPlaneListener
            }

            // Create the Anchor.
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment!!.arSceneView.scene)

            // Create the transformable andy and add it to the anchor.
            val andy = TransformableNode(arFragment!!.transformationSystem)
            andy.setParent(anchorNode)
            andy.renderable = andyRenderable
            andy.select()
        }
    }

    companion object {
        private val TAG = ArSceneformPoCActivity::class.java.simpleName
        private const val MIN_OPENGL_VERSION = 3.0

        /**
         * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
         * on this device.
         *
         *
         * Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
         *
         *
         * Finishes the activity if Sceneform can not run
         */
        fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {
            if (Build.VERSION.SDK_INT < VERSION_CODES.N) {
                Log.e(TAG, "Sceneform requires Android N or later")
                Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show()
                activity.finish()
                return false
            }
            val openGlVersionString = (activity.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
                    .deviceConfigurationInfo
                    .glEsVersion
            if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
                Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later")
                Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                        .show()
                activity.finish()
                return false
            }
            return true
        }
    }
}