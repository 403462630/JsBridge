(function() {
    if (window.JsBridge) {
        console.log("window.JsBridge is already exits");
        return
    }

    var CUSTOM_PROTOCOL_SCHEME = 'jsbridge';
    var CUSTOM_PROTOCOL_HOST_DISPATCH_MESSAGE = '__DISPATCH_MESSAGE__';

    var bridgeIFrame;
    var messageHandlers = {};
    var responseCallbacks = {};
    var uniqueId = 1;

    function defaultMessageHandler(data, callback) {
        if (callback) {
            callback(data);
        }
    }

    function _createQueueReadyIFrame(doc) {
        bridgeIFrame = doc.createElement('iframe');
        bridgeIFrame.style.display = 'none';
        doc.documentElement.appendChild(bridgeIFrame);

    }

    function init(messageHandler) {
        if (JsBridge._messageHandler) { throw new Error('WebViewJavascriptBridge.init called twice') }
        JsBridge._messageHandler = messageHandler;
        console.log("WebViewJavascriptBridge: init");
    }

    function registerHandler(handlerName, handler) {
        messageHandlers[handlerName] = handler;
    }

    function callHandler(handlerName, message, responseCallback) {
        _doSend({handlerName : handlerName, data : message}, responseCallback);
    }

    function send(message, responseCallback) {
        _doSend({data : message}, responseCallback);
    }
    function _doSend(message, responseCallback) {
        if (responseCallback) {
            var callbackId = 'js_cb_' + (uniqueId++) + '_' + new Date().getTime();
            responseCallbacks[callbackId] = responseCallback;
            message['callbackId'] = callbackId;
        }
        handlerJsBridgeMessage(message);
    }

    function handlerJsBridgeMessage(message) {
        var messageQueueString = JSON.stringify(message);
        bridgeIFrame.src = CUSTOM_PROTOCOL_SCHEME + '://' + CUSTOM_PROTOCOL_HOST_DISPATCH_MESSAGE +'/' + encodeURIComponent(messageQueueString);
    }

    function _handleJsBridgeMessageFromJava(messageJson) {
        var message = JSON.parse(messageJson);
        var messageHandler;
        if (message.responseId) {
            var responseCallback = responseCallbacks[message.responseId];
            if (responseCallback) {
                responseCallback(message.responseData);
                delete responseCallbacks[message.responseId];
            }
        } else {
            var javaCallback;
            if (message.callbackId) {
                var javaCallbackId = message.callbackId;
                javaCallback = function(responseData) {
                    _doSend({responseId : javaCallbackId, responseData : responseData});
                }
            }

            var handler = JsBridge._messageHandler;
            if (message.handlerName) {
                handler = messageHandlers[message.handlerName];
            }
            if (!handler) {
                handler = defaultMessageHandler;
            }
            try {
                handler(message.data, javaCallback);
            } catch (exception) {
                if (typeof console != 'undefined') {
                    console.log("WebViewJavascriptBridge: WARNING: javascript handler threw.", message, exception);
                }
            }
        }
    }
    window.JsBridge = {
        init: init,
        send: send,
        registerHandler: registerHandler,
        addEventListener: registerHandler,
        callHandler: callHandler,
        fireEvent: callHandler,
        _handleJsBridgeMessageFromJava: _handleJsBridgeMessageFromJava
    };

    var doc=document;
    _createQueueReadyIFrame(doc);
    var readyEvent = doc.createEvent('Events');
    readyEvent.initEvent('JsBridgeReady');
    readyEvent.bridge=JsBridge;
    doc.dispatchEvent(readyEvent);
})();