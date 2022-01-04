package cat.balrog.glassjoy.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import cat.balrog.glassjoy.GlassJoyInputView
import cat.balrog.glassjoy.InputGesture

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        val frameLayout = FrameLayout(this)
        frameLayout.addView(textView)
        makeMatchParent(textView)
        setContentView(frameLayout)
        configureGlassJoy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("GlassJoy", "Key Down: $keyCode")
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("GlassJoy", "Key Up: $keyCode")
        return super.onKeyUp(keyCode, event)
    }

    fun configureGlassJoy() {
        addGlassJoy { oldValue, newValue ->
            if (newValue == InputGesture.HardwareButton && oldValue == InputGesture.SwipeDown) {
                finish()
                return@addGlassJoy
            }
            oldValue?.keyCode?.let { onKeyUp(it, null) }
            newValue?.keyCode?.let { onKeyDown(it, null) }
        }
    }

    fun addGlassJoy(inputListener: (oldValue: InputGesture?, newValue: InputGesture?)->Unit) {
        val glassJoy = GlassJoyInputView(this)
        glassJoy.inputListener = inputListener
        findViewById<ViewGroup>(android.R.id.content)
            .addView(glassJoy)
        makeMatchParent(glassJoy)
        glassJoy.requestFocus()
    }

    fun makeMatchParent(view: View) {
        view.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }
}