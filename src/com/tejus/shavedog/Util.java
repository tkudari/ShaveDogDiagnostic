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

import android.net.wifi.WifiManager;
import org.teleal.common.logging.LoggingUtil;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author Christian Bauer
 */
public class Util {

    final public static boolean ANDROID_EMULATOR;
    static {
        boolean foundEmulator = false;
        try {
            Class androidBuild = Thread.currentThread().getContextClassLoader().loadClass("android.os.Build");
            String product = (String)androidBuild.getField("PRODUCT").get(null);
            if ("google_sdk".equals(product) || ("sdk".equals(product)))
                foundEmulator = true;
        } catch (Exception ex) {
            // Ignore
        }
        ANDROID_EMULATOR = foundEmulator;
    }

    public static InetAddress getWifiInetAddress(WifiManager manager) {

        Enumeration<NetworkInterface> interfaces = null;
        try {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            return null;
        }

        int wifiIP = manager.getConnectionInfo().getIpAddress();
        int reverseWifiIP = Integer.reverseBytes(wifiIP);

        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            Enumeration<InetAddress> inetAddresses = iface.getInetAddresses();
            while (inetAddresses.hasMoreElements()) {
                InetAddress inetAddress = inetAddresses.nextElement();
                int byteArrayToInt = byteArrayToInt(inetAddress.getAddress(), 0);
                if (byteArrayToInt == wifiIP || byteArrayToInt == reverseWifiIP) {
                    return inetAddress;
                }
            }
        }
        return null;
    }

    public static int byteArrayToInt(byte[] arr, int offset) {
        if (arr == null || arr.length - offset < 4)
            return -1;

        int r0 = (arr[offset] & 0xFF) << 24;
        int r1 = (arr[offset + 1] & 0xFF) << 16;
        int r2 = (arr[offset + 2] & 0xFF) << 8;
        int r3 = arr[offset + 3] & 0xFF;
        return r0 + r1 + r2 + r3;
    }

    public static void initLogging() {
        // Fix the logging integration between java.util.logging and Android internal logging
        LoggingUtil.resetRootHandler(new FixedAndroidHandler());
    }

}
