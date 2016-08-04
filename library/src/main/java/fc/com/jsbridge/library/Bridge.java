package fc.com.jsbridge.library;

import android.net.Uri;
import android.os.Build;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by can on 2016/8/1.
 */
public class Bridge {

    public static class BridgeWebViewClient extends WebViewClient{
        protected WebViewJavascriptBridge bridge;

        public BridgeWebViewClient(WebViewJavascriptBridge bridge) {
            this.bridge = bridge;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return super.shouldOverrideUrlLoading(view, url);
            } else {
                url = Uri.decode(url);
                Log.i("shouldOverrideUrlLoading, url = " + url);
                if (url.startsWith(BridgeUtil.CUSTOM_PROTOCOL_SCHEME)) {
                    bridge._handleMessageFromJs(BridgeUtil.getDataFromUrl(url));
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                BridgeUtil.webViewLoadLocalJs(view);
            }
            bridge.onFinish();
        }
    }

    public static class BridgeWebChromeClient extends WebChromeClient{
        private boolean mIsInjectedJS;
        @Override
        public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
            Log.d(consoleMessage.message() + ", line: " + consoleMessage.lineNumber());
            return super.onConsoleMessage(consoleMessage);
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (newProgress <= 50) {
                    mIsInjectedJS = false;
                } else if (!mIsInjectedJS) {
                    BridgeUtil.webViewLoadLocalJs(view);
                    mIsInjectedJS = true;
                }
            }
        }
    }

    public static interface BridgeHandler {
        void handler(String data, WebViewJavascriptBridge.JavaScriptCallback callback);
    }
}
