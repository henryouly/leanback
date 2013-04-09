package com.github.henryouly.leanback.util;

import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class GAEHttpsConnection {

  private static final String TAG = GAEHttpsConnection.class.getSimpleName();
  private SSLSocket mSocket;
  private int mTimeout;
  private boolean mConnected = false;

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
    SocketAddress remoteAddr = new InetSocketAddress("www.google.cn", 443);
    mSocket.connect(remoteAddr);
    mConnected  = mSocket.isConnected();
    Log.d(TAG, "Connected " + mConnected);
    return mConnected;
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
