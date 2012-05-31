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

import android.content.Context;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.protocol.HttpContext;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;
import org.teleal.common.util.MimeType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class MediaStoreContent extends DIDLContent implements Content {

    final private static Logger log = Logger.getLogger(MediaStoreContent.class.getName());

    public static final String CREATOR = "System";
    public static final DIDLObject.Class CLASS_CONTAINER = new DIDLObject.Class("object.container");

    static public class ID {

        // Everyone uses $, which is a bad idea because it's a
        // reserved URI character, a dash is much better!
        public static final String SEPARATOR = "-";

        private static Random random = new Random(new Date().getTime());

        public static String random() {
            // Let's try to keep them short, not MAX_VALUE
            return Long.toString(random.nextInt(99999)); // TODO: More than 100K objects in one folder?!
        }

        public static String appendRandom(DIDLObject object) {
            return appendRandom(object.getId());
        }

        public static String appendRandom(String id) {
            return id + SEPARATOR + random();
        }
    }

    final protected Context context;
    final protected URLBuilder urlBuilder;
    final protected MediaStoreObservers observers;

    public MediaStoreContent(Context context, URLBuilder urlBuilder) {
        this.context = context;
        this.urlBuilder = urlBuilder;

        RootContainer rootContainer = new RootContainer(this);
        addContainer(rootContainer);

        this.observers = new MediaStoreObservers(context, rootContainer);
    }

    public Context getContext() {
        return context;
    }

    public URLBuilder getUrlBuilder() {
        return urlBuilder;
    }

    public RootContainer getRootContainer() {
        return (RootContainer) getContainers().get(0);
    }

    public void registerObservers() {
        observers.register();
    }

    public void unregisterObservers() {
        observers.unregister();
    }

    public void updateAll() {
        observers.updateAll();
    }

    public DIDLObject findObjectWithId(String id) {
        for (Container container : getContainers()) {
            if (container.getId().equals(id)) return container;
            DIDLObject obj = findObjectWithId(id, container);
            if (obj != null) return obj;
        }
        return null;
    }

    protected DIDLObject findObjectWithId(String id, Container current) {
        for (Container container : current.getContainers()) {
            if (container.getId().equals(id)) return container;
            DIDLObject obj = findObjectWithId(id, container);
            if (obj != null) return obj;
        }
        for (Item item : current.getItems()) {
            if (item.getId().equals(id)) return item;
        }
        return null;
    }

    public void handle(HttpRequest request,
                       HttpResponse response,
                       HttpContext context) throws HttpException, IOException {

        String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
        if (!method.equals("GET")) {
            throw new MethodNotSupportedException(method + " method not supported");
        }

        String objectId = getUrlBuilder().getObjectId(request.getRequestLine().getUri());
        log.fine("GET request for object with identifier: " + objectId);

        DIDLObject obj = findObjectWithId(objectId);
        if (obj == null) {
            log.fine("Object not found, returning 404");
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        InputStream is = openDataInputStream(obj);
        if (is == null) {
            log.fine("Data not readable, returning 404");
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            return;
        }

        long sizeInBytes = getSizeInBytes(obj);
        MimeType mimeType = getMimeType(obj);

        InputStreamEntity entity = new InputStreamEntity(is, sizeInBytes);
        entity.setContentType(mimeType.toString());
        response.setEntity(entity);
        response.setStatusCode(HttpStatus.SC_OK);
        log.fine("Streaming data bytes: " + sizeInBytes);
    }

    protected InputStream openDataInputStream(DIDLObject obj) {
        try {
            if (obj instanceof MediaStoreItem) {
                MediaStoreItem item = (MediaStoreItem) obj;
                return getContext().getContentResolver().openInputStream(item.getMediaStoreUri());
            }
        } catch (FileNotFoundException ex) {
            log.fine("Data not found, can't open input stream: " + obj);
        }
        return null;
    }

    protected long getSizeInBytes(DIDLObject obj) {
        if (obj instanceof MediaStoreItem) {
            return ((MediaStoreItem)obj).getSizeInBytes();
        }
        return 0;
    }

    protected MimeType getMimeType(DIDLObject obj) {
        if (obj instanceof MediaStoreItem) {
            return ((MediaStoreItem)obj).getMimeType();
        }
        return null;
    }
}
