package com.github.henryouly.leanback.util;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class DNSResolver {
  private static final String TAG = DNSResolver.class.getSimpleName();
  private static final String[] GOOGLE_CN_DOMAIN_LIST = {
    "www.google.cn", "www.g.cn"
  };
  
  private final InetAddress[] mGoogleIpList;
  private static final int MAX_TRY = 3;

  private static final String[] GOOGLE_CN_IP_LIST = new String[] {
    "203.208.46.131", "203.208.46.132", "203.208.46.133", "203.208.46.134",
    "203.208.46.135", "203.208.46.136", "203.208.46.137", "203.208.46.138",
  };
  
  private static final String BLACKLIST_IP[] = new String[] {
                   // for ipv6
                   "1.1.1.1", "255.255.255.255",
                   // for google+
                   "74.125.127.102", "74.125.155.102", "74.125.39.113", "209.85.229.138",
                   // other ip list
                   "128.121.126.139", "159.106.121.75", "169.132.13.103", "192.67.198.6",
                   "202.106.1.2", "202.181.7.85", "203.161.230.171", "203.98.7.65",
                   "207.12.88.98", "208.56.31.43", "209.145.54.50", "209.220.30.174",
                   "209.36.73.33", "211.94.66.147", "213.169.251.35", "216.221.188.182",
                   "216.234.179.13", "243.185.187.39", "37.61.54.158", "4.36.66.178",
                   "46.82.174.68", "59.24.3.173", "64.33.88.161", "64.33.99.47",
                   "64.66.163.251", "65.104.202.252", "65.160.219.113", "66.45.252.237",
                   "72.14.205.104", "72.14.205.99", "78.16.49.15", "8.7.198.45", "93.46.8.89",
                   };

  
  public DNSResolver() {
    Set<InetAddress> ipList = new HashSet<InetAddress>();
    try {
      for (String googleIp: GOOGLE_CN_IP_LIST) {
        InetAddress address = InetAddress.getByName(googleIp);
        ipList.add(address);
      }
      for (String domain: GOOGLE_CN_DOMAIN_LIST) {
        for (int i = 0; i < MAX_TRY; i++) {
            InetAddress[] address = InetAddress.getAllByName(domain);
            if (address.length > 1 && !hasBadIp(address)) {
              ipList.addAll(Arrays.asList(address));
              break;
            }
        }
      }
    } catch (UnknownHostException e) {
      e.printStackTrace();
    }
    mGoogleIpList = ipList.toArray(new InetAddress[0]);
    Log.d(TAG, "The google ip list: " + Arrays.toString(mGoogleIpList));
  }
  
  private boolean hasBadIp(InetAddress[] address) {
    for (InetAddress addr: address) {
      for (String badIp: BLACKLIST_IP) {
        if (addr.getHostAddress() == badIp) {
          return true;
        }
      }
    }
    return false;
  }

  public InetAddress getGoogleIp() {
    Random random = new Random(System.currentTimeMillis());
    int index = random.nextInt(mGoogleIpList.length);
    Log.d(TAG, "Selected ip address: " + mGoogleIpList[index]);
    return mGoogleIpList[index];
  }
}
