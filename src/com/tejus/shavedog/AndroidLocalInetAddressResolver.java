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

import java.net.InetAddress;

/**
 * @author Christian Bauer
 */
public class AndroidLocalInetAddressResolver implements LocalInetAddressResolver {

    final protected WifiManager wifiManager;

    public AndroidLocalInetAddressResolver(WifiManager wifiManager) {
        this.wifiManager = wifiManager;
    }

    public WifiManager getWifiManager() {
        return wifiManager;
    }

    public InetAddress getLocalInetAddress() {
        return Util.getWifiInetAddress(getWifiManager());

    }
}
