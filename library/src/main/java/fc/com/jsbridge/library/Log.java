package fc.com.jsbridge.library;

/**
 * Created by can on 2016/8/1.
 */
public class Log {

    private static final String TAG = "JsBridge";

    public static void i(String message) {
        android.util.Log.i(TAG, message);
    }

    public static void d(String message) {
        android.util.Log.d(TAG, message);
    }

    public static void w(String message) {
        android.util.Log.w(TAG, message);
    }

    public static void e(String message) {
        android.util.Log.e(TAG, message);
    }

    public static void v(String message) {
        android.util.Log.v(TAG, message);
    }
}
