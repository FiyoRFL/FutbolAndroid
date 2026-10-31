package com.fiyorfl.futbol;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory;
import androidx.media3.datasource.DefaultHttpDataSource;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.PlayerView;

public class MainActivity extends Activity {
    private FrameLayout root;
    private WebView webView;
    private PlayerView playerView;
    private ExoPlayer player;
    private boolean playingNative = false;
    private boolean pageReady = false;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(14, 35, 64));
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(14, 35, 64));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webView.setBackgroundColor(Color.rgb(14, 35, 64));
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AndroidPlayerBridge(), "AndroidPlayer");
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        playerView = new PlayerView(this);
        playerView.setUseController(true);
        playerView.setControllerAutoShow(true);
        playerView.setControllerHideOnTouch(true);
        playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
        playerView.setVisibility(View.GONE);
        root.addView(playerView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        setContentView(root);
        webView.loadUrl("file:///android_asset/index.html");
        webView.setFocusable(true);
        webView.requestFocus();
        initPlayer();
    }

    private void initPlayer() {
        DefaultHttpDataSource.Factory httpFactory = new DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true)
                .setConnectTimeoutMs(15000)
                .setReadTimeoutMs(25000)
                .setUserAgent("Mozilla/5.0 (Linux; Android) AppleWebKit/537.36 Chrome/120 Mobile Safari/537.36");
        player = new ExoPlayer.Builder(this)
                .setMediaSourceFactory(new DefaultMediaSourceFactory(httpFactory))
                .build();
        playerView.setPlayer(player);
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_BUFFERING) notifyPage("buffering");
                else if (state == Player.STATE_READY && player.getPlayWhenReady()) notifyPage("playing");
            }
            @Override public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) notifyPage("playing");
            }
            @Override public void onPlayerError(PlaybackException error) {
                playingNative = false;
                playerView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
                notifyPage("error");
            }
        });
    }

    private void notifyPage(String state) {
        if (webView != null) {
            final String js = "window.onNativePlayerState && window.onNativePlayerState('" + state + "')";
            webView.post(() -> webView.evaluateJavascript(js, null));
        }
    }

    private class AndroidPlayerBridge {
        @JavascriptInterface public void play(String url, String title) {
            runOnUiThread(() -> startNativePlayback(url));
        }
        @JavascriptInterface public void stop() {
            runOnUiThread(() -> stopNativePlayback());
        }
    }

    private void startNativePlayback(String url) {
        if (player == null) initPlayer();
        playingNative = true;
        webView.setVisibility(View.GONE);
        playerView.setVisibility(View.VISIBLE);
        playerView.setKeepScreenOn(true);
        try {
            player.stop();
            player.clearMediaItems();
            player.setMediaItem(MediaItem.fromUri(url));
            player.prepare();
            player.play();
        } catch (Exception ex) {
            playingNative = false;
            playerView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
            notifyPage("error");
        }
    }

    private void stopNativePlayback() {
        playingNative = false;
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }
        playerView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        webView.requestFocus();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (playingNative) {
            if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                webView.evaluateJavascript("salir()", null);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_CHANNEL_UP) {
                webView.evaluateJavascript("zapping(-1)", null);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN || keyCode == KeyEvent.KEYCODE_CHANNEL_DOWN) {
                webView.evaluateJavascript("zapping(1)", null);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                if (playerView.isControllerFullyVisible()) playerView.hideController();
                else playerView.showController();
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override public void onBackPressed() {
        if (playingNative) webView.evaluateJavascript("salir()", null);
        else super.onBackPressed();
    }

    @Override protected void onPause() {
        super.onPause();
        if (player != null) player.pause();
    }

    @Override protected void onResume() {
        super.onResume();
        if (playingNative && player != null) player.play();
    }

    @Override protected void onDestroy() {
        if (player != null) { player.release(); player = null; }
        if (webView != null) { webView.removeJavascriptInterface("AndroidPlayer"); webView.destroy(); }
        super.onDestroy();
    }
}
