package fc.com.jsbridge.library;

/**
 * Created by can on 2016/8/1.
 */
class DefaultBridgeHandler implements Bridge.BridgeHandler {
    @Override
    public void handler(String data, WebViewJavascriptBridge.JavaScriptCallback callback) {
//        Log.d("DefaultBridgeHandler------" + data);
        if (callback != null) {
            callback.callback(data);
        }
    }
}
