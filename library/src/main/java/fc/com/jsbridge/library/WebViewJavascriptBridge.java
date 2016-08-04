package fc.com.jsbridge.library;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by can on 2016/8/1.
 */
public class WebViewJavascriptBridge {
    private WebView webView;
    Map<String, ResponseCallback> responseCallbacks = new HashMap<String, ResponseCallback>();
    Map<String, Bridge.BridgeHandler> bridgeHandlers = new HashMap<String, Bridge.BridgeHandler>();
    List<BridgeMessage> startupMessages = new ArrayList<BridgeMessage>();
    long uniqueId = 0;
    private Bridge.BridgeHandler defaultHandler = new DefaultBridgeHandler();

    void onFinish() {
        if (startupMessages != null) {
            for (BridgeMessage m : startupMessages) {
                dispatchMessage(m);
            }
            startupMessages = null;
        }
    }

    public WebViewJavascriptBridge(WebView webView) {
        this.webView = webView;
        init(webView);
    }

    private void init(WebView webView) {
        this.webView = webView;

        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.webView.addJavascriptInterface(this, BridgeUtil.getJavascriptJavaName());
        }
        this.webView.setWebViewClient(new Bridge.BridgeWebViewClient(this));
        this.webView.setWebChromeClient(new Bridge.BridgeWebChromeClient());
    }

    public void registerHandler(String handlerName, Bridge.BridgeHandler bridgeHandler) {
        bridgeHandlers.put(handlerName, bridgeHandler);
    }

    public void registerDefaultHandler(Bridge.BridgeHandler bridgeHandler) {
        defaultHandler = bridgeHandler;
    }

    public void send(String data) {
        send(data, null);
    }

    public void send(String data, ResponseCallback responseCallback) {
        send(data, responseCallback, null);
    }

    public void callHandler(String data, ResponseCallback callBack, String handlerName) {
        send(data, callBack, handlerName);
    }

    void send(String data, ResponseCallback responseCallback, String handlerName) {
        BridgeMessage message = new BridgeMessage();
        message.setData(data);
        if (responseCallback != null) {
            String callbackId = BridgeUtil.productCallbackId(++uniqueId);
            responseCallbacks.put(callbackId, responseCallback);
            message.setCallbackId(callbackId);
        }
        message.setHandlerName(handlerName);
        queueMessage(message);
    }

    @JavascriptInterface
    public void _handleMessageFromJs(String json) {
        if (TextUtils.isEmpty(json)) return;
        Log.d("_handleMessageFromJs: " + json);
        final BridgeMessage message = BridgeMessage.fromJson(json);
        if (!TextUtils.isEmpty(message.getResponseId())) {
            String responseId = message.getResponseId();
            final ResponseCallback callback = responseCallbacks.get(responseId);
            if (callback != null) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.callback(message.getResponseData());
                    }
                });
            }
            responseCallbacks.remove(responseId);
        } else {
            String callbackId = message.getCallbackId();
            JavaScriptCallback callback = null;
            if (!TextUtils.isEmpty(callbackId)) {
                callback = new JavaScriptCallback(callbackId);
            }

            Bridge.BridgeHandler bridgeHandler = null;
            String handlerName = message.getHandlerName();
            if (!TextUtils.isEmpty(handlerName)) {
                bridgeHandler = bridgeHandlers.get(handlerName);
            }
            if (bridgeHandler == null) {
                bridgeHandler = defaultHandler;
            }
            final String data = message.getData();
            final Bridge.BridgeHandler finalBridgeHandler = bridgeHandler;
            final JavaScriptCallback finalCallback = callback;
            webView.post(new Runnable() {
                @Override
                public void run() {
                    finalBridgeHandler.handler(data, finalCallback);
                }
            });
        }
    }

    private void queueMessage(BridgeMessage m) {
        if (startupMessages != null) {
            startupMessages.add(m);
        } else {
            dispatchMessage(m);
        }
    }

    private void dispatchMessage(BridgeMessage message) {
        String messageJson = message.toJson();
        Log.d("dispatchMessage: " + messageJson);
        final String javascriptCommand = BridgeUtil.getJavascriptCommand(messageJson);
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl(javascriptCommand);
            }
        });
    }

    public void clear() {
        responseCallbacks.clear();
        bridgeHandlers.clear();
        if (startupMessages != null) {
            startupMessages.clear();
        }
    }

    public class JavaScriptCallback implements ResponseCallback {
        private final String callbackId;

        JavaScriptCallback(String callbackId){
            this.callbackId = callbackId;
        }

        @Override
        public void callback(String data) {
            BridgeMessage message = new BridgeMessage();
            message.setResponseId(callbackId);
            message.setResponseData(data);
            dispatchMessage(message);
        }
    }
}
