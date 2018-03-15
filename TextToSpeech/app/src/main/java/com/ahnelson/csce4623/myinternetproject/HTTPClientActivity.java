package com.ahnelson.csce4623.myinternetproject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;


public class HTTPClientActivity extends AppCompatActivity implements DownloadCallback, TextToSpeech.OnInitListener {

    // Keep a reference to the NetworkFragment which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    private TextToSpeech tts;
    private ImageButton speakButton;

    protected WebView mWebView;

    private final int CHECK_CODE = 0x1;
    private String mResultString = "";

    private String fullHTML = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_httpclient);
        String website = this.getIntent().getStringExtra("webpage");
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), website);
        mWebView = (WebView)findViewById(R.id.wvHttpClientView);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.loadUrl(website);

        //allow network connections on main thread
        //not good, but willing to accept the consequences on this simple app
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        System.out.println("WEBSITE " + website);
        try {
            Document doc = Jsoup.connect(website).get();
            fullHTML = doc.body().text();
        } catch (IOException e) {
            e.printStackTrace();
        }

        checkTTS();
        startDownload();

        tts = new TextToSpeech(this, this);

        speakButton = (ImageButton) findViewById(R.id.textToSpeech);
        speakButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg) {
                speakText();
            }
        });
    }


    @Override
    protected void onDestroy(){

        // Don't forget to shutdown tts!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void checkTTS() {

        Intent check = new Intent();
        check.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(check, CHECK_CODE);
    }


    private void startDownload() {

        if (!mDownloading && mNetworkFragment != null) {

            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }
    @Override
    public void updateFromDownload(String result) {
        mResultString+=result;
        System.out.println(result);
        if (result != null) {
            //mWebView.loadData(result,"text/html",null);
            Log.d("HTTPClient","here1");
        } else {
            //mDataText.setText(getString(R.string.connection_error));
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            Log.d("HTTPClient","Here3");
            mNetworkFragment.cancelDownload();
        }
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {
        switch(progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                break;
        }
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
                speakButton.setEnabled(false);
            } else {
                speakButton.setEnabled(true);
            }

        } else {
            Log.e("TTS", "Initialization Failed!");
        }
    }

    private void speakText() {

        //parse the resulting html from the page
        Document doc = Jsoup.parse(mResultString);
        String entireText = doc.body().text();

        //speak the text on the page
        tts.speak(fullHTML, TextToSpeech.QUEUE_FLUSH, null);
    }
}
