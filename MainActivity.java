import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;


public class MainActivity extends AppCompatActivity {

    private WebView contentWebView;
    private ProgressBar progressBar;
    private ProgressBar progressBarError;
    private final int MaxBackPressed = 4;
    private int BackPressed = 0;

    private String StartPage = "https://www.YourWebsite.com/";
    private View includeContentScreen;
    private String ErrorPage = "file:///android_res/raw/no_network.html";
    private View includeErrorScreen;
    private String SplashPage = "file:///android_res/raw/splash.html";
    private View includeSplashtScreen;

    private boolean LoadingError = false;
    private boolean FirstStart = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);


        initSplashscreen();
        initContent();
        initErrorscreen();
    }

    /*
    Initialize Splashscreen
    Splashscreen will show on startup until your page is loaded

    */
    private void initSplashscreen() {
        includeSplashtScreen = findViewById(R.id.include_splash_screen);
        WebView WV = (WebView) includeSplashtScreen.findViewById(R.id.SplashWebView);
        WV.loadUrl(SplashPage);
    }

    /*
    Initialize Main Content

    */
    private void initContent() {
        includeContentScreen = findViewById(R.id.include_content);
        contentWebView = (WebView) includeContentScreen.findViewById(R.id.webview);
        progressBar = (ProgressBar) includeContentScreen.findViewById(R.id.progressBar);
        contentWebView.setWebChromeClient(new ModifiedChromeViewClient());
        contentWebView.setWebViewClient(new ModifiedWebViewClient());
        contentWebView.getSettings().setJavaScriptEnabled(true);
        contentWebView.loadUrl(StartPage);
    }

    /*
    Initialize Error Screen

    */
    private void initErrorscreen() {
        includeErrorScreen = findViewById(R.id.include_error_screen);
        WebView WV = (WebView) includeErrorScreen.findViewById(R.id.ErrorWebview);
        progressBarError = (ProgressBar) includeErrorScreen.findViewById(R.id.ErrorProgressBar);
        WV.loadUrl(ErrorPage);
        FloatingActionButton fab = (FloatingActionButton) includeErrorScreen.findViewById(R.id.ErrorActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                contentWebView.reload();
            }
        });
    }


    /*
    Quick, dirty and functional way of not instantly reacting to back button presses to not close the app
    Variable "BackPressed" will be reset on succsessfuly loaded page

    */
    public void onBackPressed() {
        if (contentWebView.canGoBack()) {
            contentWebView.goBack();
        } else {
            BackPressed++;
            if (BackPressed%MaxBackPressed == 0) {
                super.onBackPressed();
            }
        }
    }

    /*
    Custom WebViewClient

    */
    private class ModifiedWebViewClient extends WebViewClient {

        /*
        Heandling of Links to Android System Functions like tel: or mailto:

         */
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            // Heandle HTTP / HTTPS requensts
            if (url.startsWith("http:") || url.startsWith("https:")) {
                return false;
            }
            // Otherwise let the OS to handle it
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception a)
            {
                // Wasnt sure how to catch this exception in i good way
            }

            return true;
        }

        /*
        Starting the Progressbar, rotating circle in this case, so i dont have to update it

        */
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            // Show Progress Circle
            progressBar.setVisibility(View.VISIBLE);
            progressBarError.setVisibility(View.VISIBLE);


            // Look at function onBackPressed for details of this variable
            BackPressed = 0;
        }

        /*
        Hiding Splashscreen
        Hiding Progress Circle
        Hiding Error Screen in case of error

        */
        public void onPageFinished(WebView view, String url) {

            // Hide Splashscreen
            if(FirstStart == true) {
                includeSplashtScreen.setVisibility(View.INVISIBLE);
                includeContentScreen.setVisibility(View.VISIBLE);
                FirstStart = false;
            }

            // Hide Progress Circle
            progressBar.setVisibility(View.INVISIBLE);
            progressBarError.setVisibility(View.INVISIBLE);

            // hiding Errorscreen if needed
            if(LoadingError == false)
            {
                includeContentScreen.setVisibility(View.VISIBLE);
                includeErrorScreen.setVisibility(View.INVISIBLE);
            }
            LoadingError = false;
        }

        /*
        If the requested page is unreachable or an other error occures

        */
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            LoadingError = true;
            includeContentScreen.setVisibility(View.INVISIBLE);
            includeErrorScreen.setVisibility(View.VISIBLE);
        }
    }

    /*
    This part last was added to open an filechooser to upload pictures and shit, not my code, it works dont care.

    */
    private ValueCallback<Uri> uploadMessage;
    private ValueCallback<Uri[]> uploadMessageAboveL;
    private final static int FILE_CHOOSER_RESULT_CODE = 10000;

    private class ModifiedChromeViewClient extends WebChromeClient {
        // For Android < 3.0
        public void openFileChooser(ValueCallback<Uri> valueCallback) {
            uploadMessage = valueCallback;
            openImageChooserActivity();
        }

        // For Android >= 3.0
        public void openFileChooser(ValueCallback valueCallback, String acceptType) {
            uploadMessage = valueCallback;
            openImageChooserActivity();
        }

        //For Android >= 4.1
        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
            uploadMessage = valueCallback;
            openImageChooserActivity();
        }

        // For Android >= 5.0
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            uploadMessageAboveL = filePathCallback;
            openImageChooserActivity();
            return true;
        }
    };

    private void openImageChooserActivity() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        startActivityForResult(Intent.createChooser(i, "Image Chooser"), FILE_CHOOSER_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_CHOOSER_RESULT_CODE) {
            if (null == uploadMessage && null == uploadMessageAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (uploadMessageAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (uploadMessage != null) {
                uploadMessage.onReceiveValue(result);
                uploadMessage = null;
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent intent) {
        if (requestCode != FILE_CHOOSER_RESULT_CODE || uploadMessageAboveL == null)
            return;
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (intent != null) {
                String dataString = intent.getDataString();
                ClipData clipData = intent.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadMessageAboveL.onReceiveValue(results);
        uploadMessageAboveL = null;

    }
}




