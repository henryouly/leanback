package com.github.henryouly.leanback.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class GAEHttpsConnection {

  private static final String TAG = GAEHttpsConnection.class.getSimpleName();
  private SSLSocket mSocket;
  private int mTimeout;
  private boolean mConnected = false;
  private int mStatusCode;
  private Pattern mHttpResponsePattern = Pattern.compile("HTTP/(\\d.\\d) (\\d{3}) (\\w+)");
  private DNSResolver mDnsResolver = new DNSResolver(); 

  public GAEHttpsConnection() {
    SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
    try {
      mSocket = (SSLSocket) factory.createSocket();
    } catch (IOException e) {
      e.printStackTrace();
      mSocket = null;
    }
    mTimeout = 16;
  }
  
  public boolean connect() throws IOException {
    mSocket.setReuseAddress(true);
    mSocket.setReceiveBufferSize(32 * 1024);
    mSocket.setTcpNoDelay(true);
    mSocket.setSoTimeout(mTimeout * 1000);
    SocketAddress remoteAddr = new InetSocketAddress(mDnsResolver.getGoogleIp(), 443);
    mSocket.connect(remoteAddr);
    mConnected  = mSocket.isConnected();
    Log.d(TAG, "Connected " + mConnected);
    return mConnected;
  }
  
  public boolean request(String method, String url) throws IOException {
    if (!mConnected) {
      connect();
    }
    BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "UTF8"));
    String req = "GET " + url + " HTTP/1.0\r\n";
    wr.write(req);
    wr.write("\r\n");
    wr.flush();
    
    BufferedReader in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
    String statusLine = in.readLine();    
    Log.d(TAG, statusLine);
    Matcher m = mHttpResponsePattern.matcher(statusLine);

    if (m.find()) {
      mStatusCode = Integer.parseInt(m.group(2));
      if (mStatusCode == 200) {
        return true;
      } else {
        throw new IOException("Post failed with error code " + mStatusCode);
      }
    }
    return false;
  }
  
  public int getStatusCode() {
    return mStatusCode;
  }
  
  public void disconnect() {
    try {
      if (mSocket != null) {
        mSocket.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      mSocket = null;
    }
  }
}
