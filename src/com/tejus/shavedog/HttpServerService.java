
package com.tejus.shavedog;

import org.apache.http.protocol.HttpRequestHandler;


public interface HttpServerService {

    public int getLocalPort();
    public void addHandler(String pattern, HttpRequestHandler handler);
    public void removeHandler(String pattern);
}
