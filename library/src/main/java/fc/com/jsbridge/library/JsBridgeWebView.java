package fc.com.jsbridge.library;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;

/**
 * Created by can on 2016/8/4.
 */
public class JsBridgeWebView extends WebView {

    private WebViewJavascriptBridge bridge;

    public JsBridgeWebView(Context context) {
        super(context);
        init();
    }

    public JsBridgeWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public JsBridgeWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bridge = new WebViewJavascriptBridge(this);
    }

    public void registerHandler(String handlerName, Bridge.BridgeHandler bridgeHandler) {
        bridge.registerHandler(handlerName, bridgeHandler);
    }

    public void registerDefaultHandler(Bridge.BridgeHandler bridgeHandler) {
        bridge.registerDefaultHandler(bridgeHandler);
    }

    public void send(String data) {
        send(data, null);
    }

    public void send(String data, ResponseCallback responseCallback) {
        bridge.send(data, responseCallback, null);
    }

    public void callHandler(String data, ResponseCallback callBack, String handlerName) {
        bridge.send(data, callBack, handlerName);
    }
}
