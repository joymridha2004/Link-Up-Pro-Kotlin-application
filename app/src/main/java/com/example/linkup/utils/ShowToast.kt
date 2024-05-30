import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.example.linkup.R
import com.shashank.sony.fancytoastlib.FancyToast
import www.sanju.motiontoast.MotionToast
import www.sanju.motiontoast.MotionToastStyle

class ShowToast(private val context: Context) {

    private var toast: Toast? = null
    private val handler = Handler(Looper.getMainLooper())
    private var runnable: Runnable? = null

    fun defaultToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.DEFAULT,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun successToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.SUCCESS,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun infoToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.INFO,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun warningToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.WARNING,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun errorToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.ERROR,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun confusingToast(message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_SHORT,
                FancyToast.CONFUSING,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun customToasts(message: String, iconResId: Int) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            toast?.cancel()
            toast = FancyToast.makeText(
                context,
                message,
                FancyToast.LENGTH_LONG,
                FancyToast.CONFUSING,
                iconResId,
                false
            )
            toast?.show()
        }

        handler.post(runnable!!)
    }

    fun motionSuccessToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun motionErrorToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun motionWarningToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun motionInfoToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun motionDeleteToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.DELETE,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun colorMotionSuccessToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun colorMotionErrorToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun colorMotionWarningToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun colorMotionInfoToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun colorMotionDeleteToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.createColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.DELETE,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkSuccessToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkErrorToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkWarningToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkInfoToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkDeleteToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.DELETE,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkColorSuccessToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.SUCCESS,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkColorErrorToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.ERROR,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkColorWarningToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.WARNING,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkColorInfoToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.INFO,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

    fun darkColorDeleteToast(title: String, message: String) {
        runnable?.let { handler.removeCallbacks(it) }

        runnable = Runnable {
            MotionToast.darkColorToast(
                context as Activity,
                title,
                message,
                MotionToastStyle.DELETE,
                MotionToast.GRAVITY_BOTTOM,
                MotionToast.SHORT_DURATION,
                ResourcesCompat.getFont(context, R.font.roboto)
            )
        }

        handler.post(runnable!!)
    }

}
