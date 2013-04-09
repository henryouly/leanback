package com.github.henryouly.leanback.util;

import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.UnknownHostException;

public class GAEStreamHandler extends URLStreamHandler {

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    return new GAEHttpURLConnection(u, getDefaultPort());
  }
}
