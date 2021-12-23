package cat.balrog.glassjoy.sample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
        addGlassJoy {
            if (it == null) return@addGlassJoy
            if (it == InputGesture.SwipeDown) {
                finish()
            } else {
                textView.text = listOf(it, textView.text).joinToString("\n")
            }
        }
    }

    fun makeMatchParent(view: View) {
        view.updateLayoutParams {
            width = ViewGroup.LayoutParams.MATCH_PARENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    fun addGlassJoy(inputListener: (InputGesture?)->Unit) {
        val glassJoy = GlassJoyInputView(this)
        glassJoy.inputListener = inputListener
        findViewById<ViewGroup>(android.R.id.content)
            .addView(glassJoy)
        makeMatchParent(glassJoy)
    }
}