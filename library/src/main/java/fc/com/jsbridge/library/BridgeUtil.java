package fc.com.jsbridge.library;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.webkit.WebView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by can on 2016/8/1.
 */
public class BridgeUtil {
    final static String CUSTOM_PROTOCOL_SCHEME = "jsbridge://";
    final static String CUSTOM_PROTOCOL_HOST_DISPATCH_MESSAGE = "__DISPATCH_MESSAGE__";
    final static String JAVASCRIPT_PATH = "WebViewJavascriptBridge.js";
    final static String JAVASCRIPT_SUPPORT_PATH = "WebViewJavascriptBridgeForSupport.js";
    final static String JAVASCRIPT_JAVA_NAME = "_WebViewJavascriptBridge";
    final static String CALLBACK_ID_FORMAT = "JAVA_CB_%s";
    final static String JAVASCRIPT_COMMAND_FORMAT = "javascript:JsBridge._handleJsBridgeMessageFromJava('%s');";

    public static String escapeString(String json) {
        String result = json;
        result = result.replaceAll("(\\\\)([^utrn])", "\\\\\\\\$1$2");
        result = result.replaceAll("(?<=[^\\\\])(\")", "\\\\\"");
        return result;
    }

    public static String productCallbackId(long l) {
        return String.format(CALLBACK_ID_FORMAT, l + "_" + SystemClock.currentThreadTimeMillis());
    }

    public static String getJavascriptJavaName() {
        return JAVASCRIPT_JAVA_NAME;
    }

    public static String getJavascriptCommand(String json) {
        String command = String.format(JAVASCRIPT_COMMAND_FORMAT, escapeString(json));
        Log.d(command);
        return command;
    }

    public static String getDataFromUrl(String url) {
        String temp = url.replace(CUSTOM_PROTOCOL_SCHEME + CUSTOM_PROTOCOL_HOST_DISPATCH_MESSAGE, "");
        int start = temp.indexOf("/");
        String data = null;
        try {
            data = temp.substring(start + 1);
            Log.d(data);
        } catch (IndexOutOfBoundsException e) {}
        return data;
    }

    public static void webViewLoadLocalJs(WebView view) {
        String script = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            script = assetFile2Str(view.getContext(), JAVASCRIPT_PATH);
        } else {
            script = assetFile2Str(view.getContext(), JAVASCRIPT_SUPPORT_PATH);
        }
        view.loadUrl("javascript:" + script);
    }

    public static String assetFile2Str(Context c, String urlStr){
        InputStream in = null;
        try{
            in = c.getAssets().open(urlStr);

            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();

            // byte buffer into a string
            String text = new String(buffer);
            return text;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(in != null){
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
