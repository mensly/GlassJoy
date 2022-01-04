package cat.balrog.glassjoy

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.os.SystemClock
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.tan

enum class InputGesture(val keyCode: Int? = null) {
    JoyUp(KeyEvent.KEYCODE_DPAD_UP),
    JoyDown(KeyEvent.KEYCODE_DPAD_DOWN),
    JoyLeft(KeyEvent.KEYCODE_DPAD_LEFT),
    JoyRight(KeyEvent.KEYCODE_DPAD_RIGHT),
    JoyNeutral,
    Tap(KeyEvent.KEYCODE_SPACE),
    DoubleTap(KeyEvent.KEYCODE_ENTER),
    SwipeUp(KeyEvent.KEYCODE_NUMPAD_8),
    SwipeDown(KeyEvent.KEYCODE_NUMPAD_2),
    SwipeLeft(KeyEvent.KEYCODE_NUMPAD_4),
    SwipeRight(KeyEvent.KEYCODE_NUMPAD_6),
    HardwareButton,
    HardwareButtonHeld(KeyEvent.KEYCODE_ESCAPE),
}

val Number.toPx get() = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this.toFloat(),
    Resources.getSystem().displayMetrics)

class GlassJoyInputView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private companion object {
        private const val NO_TOUCH = Long.MIN_VALUE
        private val ANGLE_THRESHOLD = tan(PI / 4)
    }
    private var input: InputGesture? = null
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != value) {
                inputListener(oldValue, value)
            }
        }

    var inputListener: (oldValue: InputGesture?, newValue: InputGesture?)->Unit = { _,_ -> }
    var deadZone = 16.toPx
    var deadTime = 300L
    var interceptingTouch = true
    var buttonTogglesTouch = true
    private var joyCenterX = 0f
    private var joyCenterY = 0f
    private var touchDown = NO_TOUCH
    private val now get() = SystemClock.elapsedRealtime()
    private val joystickActive get() = touchDown != NO_TOUCH && touchDown + deadTime < now

    private val listener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            return performClick()
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            input = InputGesture.DoubleTap
            input = null
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
                input = if (deltaY > 0) InputGesture.SwipeDown else InputGesture.SwipeUp
            } else {
                input = if (deltaX > 0) InputGesture.SwipeRight else InputGesture.SwipeLeft
            }
            input = null
            return true
        }
    }

    private val gestureDetector = GestureDetector(context, listener)

    init {
        focusable = FOCUSABLE
        isFocusableInTouchMode = true
        isClickable = true
    }

    override fun onKeyLongPress(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            input = InputGesture.HardwareButtonHeld
            return true
        }
        return super.onKeyLongPress(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_CAMERA) {
            if (input != InputGesture.HardwareButtonHeld) {
                input = InputGesture.HardwareButton
            }
            input = null
            if (buttonTogglesTouch) {
                interceptingTouch = !interceptingTouch
            }
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        input = InputGesture.Tap
        input = null
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!interceptingTouch) return false
        if (event.actionIndex == 0) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    touchDown = now
                    joyCenterX = event.x
                    joyCenterY = event.y
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (joystickActive) {
                        input = InputGesture.JoyNeutral
                    }
                    touchDown = NO_TOUCH
                }
                MotionEvent.ACTION_MOVE -> {
                    val dX = event.x - joyCenterX
                    val dY = event.y - joyCenterY
                    if (joystickActive) {
                        if (abs(dX) > abs(dY) && abs(dX) > deadZone) {
                            input =
                                if (dX > 0) InputGesture.JoyRight else InputGesture.JoyLeft
                        } else if (abs(dY) > deadZone) {
                            input = if (dY > 0) InputGesture.JoyDown else InputGesture.JoyUp
                        } else {
                            input = InputGesture.JoyNeutral
                        }
                    } else if (abs(dX) > deadZone || abs(dY) > deadZone) {
                        touchDown = NO_TOUCH
                    }
                }
            }
        } else if (event.actionMasked == MotionEvent.ACTION_POINTER_DOWN && joystickActive) {
            input = InputGesture.Tap
            input = null
        }
        gestureDetector.onTouchEvent(event)
        return true
    }
}