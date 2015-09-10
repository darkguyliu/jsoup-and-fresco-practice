package com.david.parseimage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.text.TextUtils;
import android.util.Log;

public class CrawlImagesRunnable implements Runnable {
    private CrawlImagesCallback mCallback;
    private String mUrl;
    private HttpURLConnection mConn;

    interface CrawlImagesCallback {
        void onCrawlingCompleted(ArrayList<String> imageslist);

        void onCrawlingFailed(String Url, int errorCode);
    }

    public CrawlImagesRunnable(CrawlImagesCallback callback, String Url) {
        this.mCallback = callback;
        this.mUrl = Url;
    }

    @Override
    public void run() {
        Log.d("david", "david mUrl=" + mUrl);
        String pageContent = retreiveHtmlContent(mUrl);
        Log.d("david", "david pageContent=" + pageContent);

        if ( !TextUtils.isEmpty(pageContent) ) {
            // START
            // JSoup Library used to filter urls from html body

            Document doc = Jsoup.parse(pageContent);// Jsoup.parse(pageContent.toString());
            // Document doc = Jsoup.connect(url).get();
            //Element content = doc.getElementById("content");
            //Elements images = doc.getElementsByTag("img");

            // Elements media = doc.select("[src]");
            Elements images = doc.select("img[src~=(?i)\\.(png|jpe?g)]");
            ArrayList<String> imageslist = new ArrayList<String>();
            /*
             * for (Element src : media) { if (src.tagName().equals("img"))
             * Log.d("david", "david1111 src.tagName()=" + src.tagName() +
             * ",src.attr(abs:src)=" + src.attr("abs:src") + ",src.attr(width)="
             * + src.attr("width") + ",src.attr(height)=" + src.attr("height") +
             * ",src.attr(alt)=" + src.attr("alt")); }
             */

            for ( Element src : images ) {
                // if ( src.tagName()
                // .equals("img") ) {
                if ( !TextUtils.isEmpty(src.attr("abs:src")) && TextUtils.isEmpty(src.attr("onerror"))) {
                    imageslist.add(src.attr("abs:src"));
                }
                Log.d("david", "david2222 src.tagName()=" + src.tagName()
                    + ",src.attr(abs:src)=" + src.attr("abs:src")
                    + ",src.attr(width)=" + src.attr("width")
                    + ",src.attr(height)=" + src.attr("height")
                    + ",src.attr(alt)=" + src.attr("alt")+",src.attr(onerror)="+src.attr("onerror"));

                // }
            }
            // End JSoup
            Log.d("david", "david imageslist size=" + imageslist.size());
            mCallback.onCrawlingCompleted(imageslist);
        } else {
            mCallback.onCrawlingFailed(mUrl, -1);
        }

    }

    private String retreiveHtmlContent(String Url) {
        URL httpUrl = null;
        try {
            httpUrl = new URL(Url);
        } catch ( MalformedURLException e ) {
            Log.d("david", "david e=" + e);
        }

        int responseCode = HttpURLConnection.HTTP_OK;
        StringBuilder pageContent = new StringBuilder();
        try {
            if ( httpUrl != null ) {
                mConn = (HttpURLConnection) httpUrl.openConnection();
                mConn.setConnectTimeout(5000);
                mConn.setReadTimeout(5000);
                responseCode = mConn.getResponseCode();
                Log.d("david", "david responseCode=" + responseCode);

                if ( responseCode != HttpURLConnection.HTTP_OK ) {
                    throw new IllegalAccessException(" http connection failed");
                }
                BufferedReader br = new BufferedReader(new InputStreamReader(
                    mConn.getInputStream()));
                String line = null;
                while ( ( line = br.readLine() ) != null ) {
                    pageContent.append(line);
                }
            }

        } catch ( IOException e ) {
            Log.e("david", "david e=" + e);
            mCallback.onCrawlingFailed(Url, -1);
        } catch ( IllegalAccessException e ) {
            Log.e("david", "david e=" + e);
            mCallback.onCrawlingFailed(Url, responseCode);
        } catch ( Exception e ) {
            Log.e("david", "david e=" + e);
            mCallback.onCrawlingFailed(Url, 10001);
        } finally {
            if ( mConn != null ) {
                mConn.disconnect();
            }
        }

        return pageContent.toString();
    }

}
