package fc.com.jsbridge;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.widget.EditText;

import fc.com.jsbridge.library.Bridge;
import fc.com.jsbridge.library.JsBridgeWebView;
import fc.com.jsbridge.library.ResponseCallback;
import fc.com.jsbridge.library.WebViewJavascriptBridge;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_UPLOAD_FILE = 100;

    private JsBridgeWebView webView;
    private EditText contentView;
    private EditText resultView;
    private ValueCallback<Uri[]> mFilePathCallbacks;
    private ValueCallback<Uri> mFilePathCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (JsBridgeWebView) findViewById(R.id.web_view);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        }
        contentView = (EditText) findViewById(R.id.et_content);
        resultView = (EditText) findViewById(R.id.result);
        resultView.setText("welcome!!");
        findViewById(R.id.bt_send_message).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.send(contentView.getText().toString(), new ResponseCallback() {
                    @Override
                    public void callback(String data) {
                        resultView.setText(data);
                    }
                });
            }
        });
        findViewById(R.id.bt_call_handler).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                webView.callHandler(contentView.getText().toString(), new ResponseCallback() {
                    @Override
                    public void callback(String data) {
                        resultView.setText(data);
                    }
                }, "testHandler");
            }
        });
        initWebView();
        webView.loadUrl("file:///android_asset/demo.html");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_UPLOAD_FILE) {
            if (resultCode == RESULT_OK) {
                Uri result = data == null ? null
                        : data.getData();
                if (mFilePathCallbacks != null) {
                    mFilePathCallbacks.onReceiveValue(new Uri[]{result});
                    mFilePathCallbacks = null;
                }
                if (mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(result);
                    mFilePathCallback = null;
                }

            }
        }
    }

    private void initWebView() {
        webView.registerDefaultHandler(new Bridge.BridgeHandler() {
            @Override
            public void handler(String data, WebViewJavascriptBridge.JavaScriptCallback callback) {
                resultView.setText(data);
                if (callback != null) {
                    callback.callback(data);
                }
            }
        });
        webView.registerHandler("testHandler", new Bridge.BridgeHandler() {
            @Override
            public void handler(String data, WebViewJavascriptBridge.JavaScriptCallback callback) {
                resultView.setText(data);
                if (callback != null) {
                    callback.callback(data);
                }
            }
        });
        webView.setWebChromeClient(new Bridge.BridgeWebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (Build.VERSION.SDK_INT >= 21) {
                    mFilePathCallbacks = filePathCallback;
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(Intent.createChooser(intent, "File Browser"), REQUEST_CODE_UPLOAD_FILE);
                    return true;
                }
                return false;
            }

            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, null);
            }

            // file upload callback (Android 3.0 (API level 11) -- Android 4.0 (API level 15)) (hidden method)
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                openFileChooser(uploadMsg, acceptType, null);
            }

            // file upload callback (Android 4.1 (API level 16) -- Android 4.3 (API level 18)) (hidden method)
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mFilePathCallback = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), REQUEST_CODE_UPLOAD_FILE);
            }

        });
    }
}
