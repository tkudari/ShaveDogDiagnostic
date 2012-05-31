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
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.MediaStore;

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class MediaStoreObservers {

    final private static Logger log = Logger.getLogger(MediaStoreContent.class.getName());

    final protected Context context;
    final private RootContainer rootContainer;

    public MediaStoreObservers(Context context, RootContainer rootContainer) {
        this.context = context;
        this.rootContainer = rootContainer;
    }

    public Context getContext() {
        return context;
    }

    public RootContainer getRootContainer() {
        return rootContainer;
    }

    final protected ContentObserver intPhotosContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getPhotosContainer().update(
                            getContext().getContentResolver(), MediaStore.Images.Media.INTERNAL_CONTENT_URI
                    );
                }
            };

    final protected ContentObserver extPhotosContentObserver =
            new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getRootContainer().getPhotosContainer().update(
                            getContext().getContentResolver(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                    );
                }
            };

    public void register() {
        log.fine("Registering content observers");
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI, false, intPhotosContentObserver
        );
        getContext().getContentResolver().registerContentObserver(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, extPhotosContentObserver
        );
    }

    public void unregister() {
        log.fine("Unregistering content observers");
        getContext().getContentResolver().unregisterContentObserver(intPhotosContentObserver);
        getContext().getContentResolver().unregisterContentObserver(extPhotosContentObserver);
    }

    public void updateAll() {
        intPhotosContentObserver.onChange(false);
        extPhotosContentObserver.onChange(false);
    }
}
