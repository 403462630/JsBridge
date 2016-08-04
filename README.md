# JsBridge

JsBridge的主要功能是实现javascript和android native代码的相互调用

由于android4.2以下webview 的 addJavascriptInterface方法存在安全隐患，所以本项目提供了两种实现方式，android4.2以下使用IFrame实现，android4.2及以上使用webView自带的addJavascriptInterface实现

1、Android Native 代码用法有两种
直接使用JsBridgeWebView
```
<fc.com.jsbridge.library.JsBridgeWebView
  android:id="@+id/web_view"
  android:layout_width="match_parent"
  android:layout_height="match_parent"/>
```
或者使用WebViewJavascriptBridge
```
WebViewJavascriptBridge bridge = new WebViewJavascriptBridge(WebView);
......//直接使用WebViewJavascriptBridge提供的方法进行操作
```

2、JavaScript中的用法
```
//此处主要是为了确保JsBridge对象存在并且进行一些必要的初始化操作
function connectWebViewJavascriptBridge() {
    if (window.JsBridge) {
        ......//对JsBridge对象进行一些初始化操作
    } else {
        document.addEventListener('JsBridgeReady', function() {
            ......//对JsBridge对象进行一些初始化操作
        },
        false)
    }
}
connectWebViewJavascriptBridge();
```
3、最后就调用各自的api进行交互
JsBridgeWebView 或　WebViewJavascriptBridge提供了如下api:
```
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
```
javascript 中JsBridge提供了如下用法：
```
//init方法是非必须的，并且只能调用一次
JsBridge.init(function(message, responseCallback) {...})
JsBridge.registerHandler("testHandler", function(data, responseCallback) {...});
JsBridge.callHandler('testHandlder', {
      'param': 'str1',
  },
  function(responseData) {
      alert(responseData);
  });
JsBridge.fireEvent('testHandlder', {
      'param': 'str1',
  },
  function(responseData) {
      alert(responseData);
  });
window.JsBridge.send(data, function(responseData) {
    alert(responseData);
});
```
