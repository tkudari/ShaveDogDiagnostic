package com.tejus.shavedog;


import org.teleal.cling.android.AndroidUpnpService;
import org.teleal.cling.android.AndroidUpnpServiceImpl;
import org.teleal.cling.model.meta.LocalDevice;
import org.teleal.common.util.URIUtil;

import com.tejus.shavedog.activity.ShaveDogActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;

public class TestMediaServerService extends Service {
    
    private static final int NOTIFICATION_ID = 1;
    protected UpnpServiceConnection upnpServiceConnection;
    protected ContentHttpServerConnection contentHttpServerConnection;




    @Override
    public IBinder onBind( Intent arg0 ) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();

        getNotificationManager().notify(NOTIFICATION_ID, createNotification());

        contentHttpServerConnection = new ContentHttpServerConnection(
                this,
                new AndroidLocalInetAddressResolver((WifiManager) getSystemService(Context.WIFI_SERVICE))
        );

        upnpServiceConnection = new UpnpServiceConnection(
                new MediaServer(contentHttpServerConnection.getContent())
        );

        contentHttpServerConnection.getContent().registerObservers();
        contentHttpServerConnection.getContent().updateAll();

        Log.d("XXXX","Binding to content HTTP server service");
        getApplicationContext().bindService(
                new Intent(this, HttpServerServiceImpl.class),
                contentHttpServerConnection,
                Context.BIND_AUTO_CREATE
        );

        Log.d("XXXX","Binding to UPnP service");
        getApplicationContext().bindService(
                new Intent(TestMediaServerService.this, AndroidUpnpServiceImpl.class),
                upnpServiceConnection,
                Context.BIND_AUTO_CREATE
        );
    }
    
    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
    
    protected Notification createNotification() {
        Notification notification = new Notification();

        notification.icon = android.R.drawable.stat_notify_sync_noanim;
        notification.tickerText = "Sash MediaServer is active";

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        notification.flags |= Notification.FLAG_NO_CLEAR;

        notification.setLatestEventInfo(
                getApplicationContext(),
                notification.tickerText,
                null,
                PendingIntent.getActivity(this, 0, new Intent(this, ShaveDogActivity.class), 0));

        return notification;
    }
    
    class UpnpServiceConnection implements ServiceConnection {

        final protected MediaServer mediaServer;
        protected AndroidUpnpService upnpService;

        UpnpServiceConnection(MediaServer mediaServer) {
            this.mediaServer = mediaServer;
        }

        public void onServiceConnected(ComponentName className, IBinder service) {
            upnpService = (AndroidUpnpService) service;

            LocalDevice mediaServerDevice =
                    upnpService.getRegistry().getLocalDevice(mediaServer.getUdn(), true);
            if (mediaServerDevice == null) {
                try {
                    Log.d("XXXX","Creating MediaServer device and registering with UPnP service");
                    mediaServerDevice = mediaServer.createDevice();
                    upnpService.getRegistry().addDevice(mediaServerDevice);
                } catch (Exception ex) {
                    Log.e("XXXX", "Creating or registering media server device failed", ex);
                }
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            upnpService = null;
        }

        public void prepareUnbind() {
            if (upnpService != null) {
                upnpService.getRegistry().removeDevice(mediaServer.getUdn());
            }
        }
    }
    
    class ContentHttpServerConnection implements ServiceConnection {

        final protected MediaStoreContent content;
        protected HttpServerService httpServerService;

        ContentHttpServerConnection(Context context, final LocalInetAddressResolver localAddressResolver) {

            Log.d("XXXX", "Creating MediaStore content");
            content = new MediaStoreContent(
                    context,
                    new URLBuilder() {
                        // TODO: this was the original, with a different package name:
//                        public String getURL(org.teleal.cling.support.model.DIDLObject object) {
//                            return URIUtil.createAbsoluteURL(
//                                    localAddressResolver.getLocalInetAddress(),
//                                    getHttpServerService().getLocalPort(),
//                                    URI.create("/" + object.getId())
//                            ).toString();
//                        }

                        public String getObjectId(String urlPath) {
                            return urlPath.substring(1); // Cut the slash
                        }

                      
                        @Override
                        public String getURL( org.teleal.cling.support.model.DIDLObject object ) {
                            // TODO Auto-generated method stub
                            return null;
                        }

//                        @Override
//                        public String getURL( DIDLObject object ) {
//                            // TODO Auto-generated method stub
//                            return null;
//                        }
                    }
            );
        }

        public MediaStoreContent getContent() {
            return content;
        }

        public HttpServerService getHttpServerService() {
            return httpServerService;
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            httpServerService = (HttpServerService) service;
            httpServerService.addHandler("*", content);
        }

        public void onServiceDisconnected(ComponentName componentName) {
            httpServerService = null;
        }

        public void prepareUnbind() {
            if (httpServerService != null) {
                httpServerService.removeHandler("*");
            }
        }
    }

}
