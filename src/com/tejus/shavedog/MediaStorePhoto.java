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

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import org.teleal.cling.support.model.ProtocolInfo;
import org.teleal.cling.support.model.Res;
import org.teleal.cling.support.model.item.Photo;
import org.teleal.common.util.MimeType;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Christian Bauer
 */
public class MediaStorePhoto extends Photo implements MediaStoreItem {

    /*
    12-03 07:12:53.152: INFO/System.out(4444): ### '_id' => 79
12-03 07:12:53.152: INFO/System.out(4444): ### '_data' => /mnt/sdcard/DCIM/Camera/IMG_20101203_071252.jpg
12-03 07:12:53.152: INFO/System.out(4444): ### '_size' => 737666
12-03 07:12:53.152: INFO/System.out(4444): ### '_display_name' => IMG_20101203_071252.jpg
12-03 07:12:53.152: INFO/System.out(4444): ### 'mime_type' => image/jpeg
12-03 07:12:53.152: INFO/System.out(4444): ### 'title' => IMG_20101203_071252
12-03 07:12:53.152: INFO/System.out(4444): ### 'date_added' => 1291356772
12-03 07:12:53.152: INFO/System.out(4444): ### 'date_modified' => null
12-03 07:12:53.152: INFO/System.out(4444): ### 'description' => null
12-03 07:12:53.152: INFO/System.out(4444): ### 'picasa_id' => null
12-03 07:12:53.152: INFO/System.out(4444): ### 'isprivate' => null
12-03 07:12:53.162: INFO/System.out(4444): ### 'latitude' => null
12-03 07:12:53.162: INFO/System.out(4444): ### 'longitude' => null
12-03 07:12:53.162: INFO/System.out(4444): ### 'datetaken' => 1291356772964
12-03 07:12:53.162: INFO/System.out(4444): ### 'orientation' => 0
12-03 07:12:53.162: INFO/System.out(4444): ### 'mini_thumb_magic' => null
12-03 07:12:53.162: INFO/System.out(4444): ### 'bucket_id' => 1506676782
12-03 07:12:53.162: INFO/System.out(4444): ### 'bucket_display_name' => Camera
     */

    protected SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private long mediaStoreId;
    private Uri mediaStoreUri;
    private long sizeInBytes;
    private MimeType mimeType;

    public static final String[] PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.MIME_TYPE,
    };

    public MediaStorePhoto(Cursor cursor, Uri mediaStoreUri, String parentId, String transientId, final URLBuilder urlBuilder) {
        this.mediaStoreId = cursor.getLong(0); // Persistent identifier
        this.mediaStoreUri = mediaStoreUri;

        setId(transientId);
        setParentID(parentId);

        setCreator(MediaStoreContent.CREATOR);

        if (!cursor.isNull(1))
            setDate(dateFormat.format(new Date(cursor.getLong(1)*1000)));

        if (!cursor.isNull(2))
            setAlbum(cursor.getString(2));

        if (!cursor.isNull(3))
            setTitle(cursor.getString(3));
        else
            setTitle(cursor.getString(4));

        this.sizeInBytes = cursor.getLong(5);

        this.mimeType = org.teleal.common.util.MimeType.valueOf(cursor.getString(6));

        Res resource = new Res() {
            @Override
            public String getValue() {
                return urlBuilder.getURL(MediaStorePhoto.this);
            }
        };
        resource.setProtocolInfo(new ProtocolInfo(mimeType));
        resource.setSize(sizeInBytes);
        addResource(resource);
    }

    public long getMediaStoreId() {
        return mediaStoreId;
    }

    public Uri getMediaStoreUri() {
        return mediaStoreUri;
    }

    public long getSizeInBytes() {
        return sizeInBytes;
    }

    public MimeType getMimeType() {
        return mimeType;
    }

    @Override
    public String toString() {
        return "(" + getClass().getSimpleName() + ") " + getMediaStoreUri();
    }
}
