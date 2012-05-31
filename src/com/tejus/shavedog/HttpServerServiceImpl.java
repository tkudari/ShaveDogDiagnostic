/*
 * Copyright (C) 2011 4th Line GmbH, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tejus.shavedog;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import org.apache.http.protocol.HttpRequestHandler;

/**
 * @author Christian Bauer
 */
public class HttpServerServiceImpl extends Service {

    protected Binder binder = new Binder();
    protected class Binder extends android.os.Binder implements HttpServerService {
        public int getLocalPort() {
            return httpServer.getLocalPort();
        }

        public void addHandler(String pattern, HttpRequestHandler handler) {
            httpServer.getHandlerRegistry().register(pattern, handler);
        }

        public void removeHandler(String pattern) {
            httpServer.getHandlerRegistry().unregister(pattern);
        }

    }

    protected BroadcastReceiver connectivityReceiver =
            new BroadcastReceiver() {
                @Override
                synchronized public void onReceive(Context c, Intent intent) {
                    if (!intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) return;
                    if (httpServer == null) return;
                    final ConnectivityManager connectivityManager =
                            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    // We can't listen to "is available" or simply "is switched on", we have to make sure it's connected
                    if (!wifiInfo.isConnected()) {
                        httpServer.stopServer();
                    } else {
                        httpServer.startServer();
                    }
                }
            };

    protected HttpServer httpServer;

    @Override
    public void onCreate() {
        super.onCreate();

        final WifiManager wifiManager =
                (WifiManager) getSystemService(Context.WIFI_SERVICE);

        httpServer = new HttpServer(new AndroidLocalInetAddressResolver(wifiManager));

        if (!Util.ANDROID_EMULATOR) {
            registerReceiver(connectivityReceiver, new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE"));
        }
    }


    @Override
    public void onDestroy() {
        if (!Util.ANDROID_EMULATOR) {
            unregisterReceiver(connectivityReceiver);
        }
        httpServer.stopServer();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
