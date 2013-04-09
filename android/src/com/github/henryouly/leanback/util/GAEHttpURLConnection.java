/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.github.henryouly.leanback.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;

class GAEHttpURLConnection extends HttpURLConnection {

  @Override
  public int getResponseCode() throws IOException {
    if (!connected) {
      connect();
    }
    if (doOutput) {
      BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF8"));
      String req = "GET " + url.toString() + " HTTP/1.0\r\n";
      wr.write(req);
      //wr.write("Content-Length: " + this.fixedContentLength + "\r\n");
      //wr.write("Content-Type: application/x-www-form-urlencoded;charset=UTF-8\r\n");
      wr.write("\r\n");
      //wr.write(output.toString());
      wr.flush();
    }
    
    InputStreamReader is = new InputStreamReader(s.getInputStream());
    char[] buffer = new char[50];
    is.read(buffer, 0, 50);
    StringBuilder sb = new StringBuilder();
    sb.append(buffer);
    
    Log.d("HAHA", sb.toString());
    
    return 200;
  }
  
  private static final byte[] GOOGLE_IP = new byte[] {(byte) 203, (byte) 208, (byte) 46, (byte) 131};
  private StringBuilder output = new StringBuilder();
  
  @Override
  public InputStream getInputStream() throws IOException {
    return s.getInputStream();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return new OutputStream() {
      @Override
      public void write(int oneByte) throws IOException {
        output.append((char) oneByte);
      }
    };
  }

    private final int defaultPort = 80;
    private Socket s;

    protected GAEHttpURLConnection(URL url, int port) {
        super(url);
    }

    @Override
    public boolean usingProxy() {
      return false;
    }

    @Override
    synchronized public void connect() throws IOException {
      int port;
      if ( (port = url.getPort()) == -1 ) 
        port = defaultPort;
      String host = url.getHost();
      if (host.endsWith(".appspot.com")) {
        //host = "203.208.46.131";
        host = "www.google.cn";
      }
      //s = new Socket(host, port);
      s = new Socket();
      s.setReuseAddress(true);
      s.setReceiveBufferSize(32*1024);
      s.setTcpNoDelay(true);
      s.setSoTimeout(16 * 1000);
      SocketAddress remoteAddr = new InetSocketAddress("www.google.cn", port);//InetAddress.getByAddress(GOOGLE_IP), port);
      s.connect(remoteAddr);
      connected = s.isConnected();
    }

    @Override
    synchronized public void disconnect() {
      try {
        s.close();
      } catch (IOException e) {
        // TODO(henryou): Auto-generated catch block
        e.printStackTrace();
      } finally {
        connected = false;        
      }
    }
}
