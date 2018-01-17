package net.wrappy.im.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import net.wrappy.im.R;

import butterknife.BindView;

/**
 * Created by ben on 17/01/2018.
 */

public class WebViewActivity extends BaseActivity {

    @BindView(R.id.webView) WebView webView;
    String url = "";

    public static void start(Activity activity, String title, String url) {
        Intent intent = new Intent(activity, WebViewActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("title",title);
        bundle.putString("url",url);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.webview_activity);
        super.onCreate(savedInstanceState);
        url = getIntent().getExtras().getString("url","");
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_ab_arrow_back);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("title",""));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        webView.getSettings().setSupportMultipleWindows(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("wrappy.net/faq.html")) {
                    webView.loadUrl("javascript:$('div.index div.index_middle').css('margin-top','0'); $('div.index header.index_header').css('display','none');");
                }
                if (url.contains("wrappy.net/#about")) {
                    webView.loadUrl("javascript:$('div.index div.index_middle').css('margin-top','0'); $('div.index header.index_header').css('display','none');");
                }

            }
        });
        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl(url);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }
}
