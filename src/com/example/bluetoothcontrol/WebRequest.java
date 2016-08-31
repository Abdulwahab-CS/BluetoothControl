package com.example.bluetoothcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.text.NoCopySpan.Concrete;

public class WebRequest {
	String url;

	public WebRequest(String url) {
		this.url = url;
	}

	public String downloadUrl() throws InterruptedException {
		DownloadWebpageTask task = new DownloadWebpageTask(this);
		task.execute(url);
		while (DownloadWebpageTask.result == null){}
		// task.wait();
		String ans = DownloadWebpageTask.result;
		DownloadWebpageTask.result = null;
		return ans;
	}
}
