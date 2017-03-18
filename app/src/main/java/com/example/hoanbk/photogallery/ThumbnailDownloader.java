package com.example.hoanbk.photogallery;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hoanbk on 3/16/2017.
 */

public class ThumbnailDownloader<Token> extends HandlerThread {
    
    private static final String TAG = "ThumbnailDownloader";
    private static final int MESSAGE_DOWNLOAD = 0;
    private static final int PRELOAD_DOWNLOAD = 1;

    Handler mHandler;
    Map<Token, String> requestMap = Collections.synchronizedMap(new HashMap<Token, String>());
    Handler mResponseHandler;
    IListener<Token> mListener;
    Handler mPreloadHandler;
    private LruCache<String, Bitmap> mLruCache;


    public interface IListener<Token> {
        void onThumbnailDownloaded(Token token, Bitmap thumbnail);
    }

    public void setListener(IListener<Token> listener) {
        mListener = listener;
    }
    
    public ThumbnailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;

        int maxSize = (int)(Runtime.getRuntime().maxMemory()/1024);
        int cacheSize = maxSize/8;
        mLruCache = new LruCache<>(cacheSize);
    }

    @SuppressLint("HandlerLeak")
    @Override
    protected void onLooperPrepared() {
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    @SuppressLint("unchecked")
                    Token token = (Token)msg.obj;
                    Log.i(TAG, "Got a request for url: " + requestMap.get(token));
                    handleRequest(token);
                }
            }
        };

        mPreloadHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == PRELOAD_DOWNLOAD) {
                    String url = (String)msg.obj;

                    loadImage(url);
                }
            }
        };
    }

    public void queueThumbnail(Token token, String url) {
        Log.i(TAG, "Got an URL: " + url);
        requestMap.put(token, url);
        mHandler.obtainMessage(MESSAGE_DOWNLOAD, token).sendToTarget();
    }

    private void handleRequest(final Token token) {

            final String url = requestMap.get(token);
            if (url == null) {
                return;
            }

            final Bitmap bitmap = loadImage(url);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (requestMap.get(token) != url) {
                        return;
                    }

                    requestMap.remove(token);
                    mListener.onThumbnailDownloaded(token, bitmap);
                }
            });

    }

    private Bitmap loadImage(String url) {
        Bitmap bitmap = getThumbnailFromCache(url);
        if (bitmap == null) {
            try {
                byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
                bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);

                Log.i(TAG, "Bitmap created");

                addThumbnailToCache(url, bitmap);
            } catch (IOException ioe) {
                Log.e(TAG, "Error downloading image", ioe);
            }
        }

        return bitmap;
    }

    public void preloadCacheMemory(String url) {
        mPreloadHandler.obtainMessage(PRELOAD_DOWNLOAD, url).sendToTarget();
    }

    public void addThumbnailToCache(String key, Bitmap bitmap) {
        if (mLruCache.get(key) == null) {
            mLruCache.put(key, bitmap);
        }
    }

    public Bitmap getThumbnailFromCache(String key) {
        return mLruCache.get(key);
    }

    public void clearQueue() {
        mHandler.removeMessages(MESSAGE_DOWNLOAD);
        requestMap.clear();
    }
}
