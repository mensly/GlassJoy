package cat.balrog.glassjoy

import android.annotation.SuppressLint
import android.content.Context
import android.gesture.Gesture
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan

class GlassJoyInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private companion object {
        private val ANGLE_THRESHOLD = tan(PI / 4)
    }
    private val _inputs = MutableStateFlow<InputGesture?>(null)
    val inputs: Flow<InputGesture?> get() = _inputs

    private val listener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return performClick()
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            _inputs.value = InputGesture.DoubleTap
            _inputs.value = null
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            val deltaX = e2.x - e1.x
            val deltaY = e2.y - e1.y
            val tan =
                if (deltaX != 0f) abs(deltaY / deltaX).toDouble() else Double.MAX_VALUE

            if (tan > ANGLE_THRESHOLD) {
                _inputs.value = if (deltaY > 0) InputGesture.SwipeDown else InputGesture.SwipeUp
            } else {
                _inputs.value = if (deltaX > 0) InputGesture.SwipeRight else InputGesture.SwipeLeft
            }
            return true
        }
    }

    private val gestureDetector = GestureDetector(context, listener)

    init {
        focusable = FOCUSABLE
        isFocusableInTouchMode = true
        isClickable = true
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent?): Boolean {
        _inputs.value = InputGesture.HardwareButtonHeld
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (_inputs.value != InputGesture.HardwareButtonHeld) {
            _inputs.value = InputGesture.HardwareButton
        }
        _inputs.value = null
        return super.onKeyUp(keyCode, event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        _inputs.value = InputGesture.Tap
        _inputs.value = null
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(event)
        return true
    }
}