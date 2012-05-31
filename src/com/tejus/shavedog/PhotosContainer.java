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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.Album;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Photo;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class PhotosContainer extends Container {

    final private static Logger log = Logger.getLogger(PhotosContainer.class.getName());

    final protected MediaStoreContent content;

    public PhotosContainer(RootContainer rootContainer) {
        this.content = rootContainer.getContent();
        setId(MediaStoreContent.ID.appendRandom(rootContainer));
        setParentID(rootContainer.getId());
        setTitle("Photos");
        setCreator(MediaStoreContent.CREATOR);
        setClazz(MediaStoreContent.CLASS_CONTAINER);
        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);

        setChildCount(2);
        addContainer(new AlbumsContainer(this));
        addContainer(new AllPhotosContainer(this));

    }

    protected URLBuilder getUrlBuilder() {
        return content.getUrlBuilder();
    }

    public AlbumsContainer getAlbumsContainer() {
        return (AlbumsContainer) getContainers().get(0);
    }

    public AllPhotosContainer getAllPhotosContainer() {
        return (AllPhotosContainer) getContainers().get(1);
    }
    public void update(ContentResolver contentResolver, Uri contentUri) {
        log.info("Querying content: " + contentUri);
        Cursor cursor = contentResolver.query(
                contentUri,
                MediaStorePhoto.PROJECTION,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " desc"
        );
        if (!cursor.moveToFirst()) return;

        AllPhotosContainer allItemsContainer = getAllPhotosContainer();
        List<Long> identifiers = new ArrayList();
        do {
            Long id = cursor.getLong(0);
            Uri mediaStoreUri = ContentUris.withAppendedId(contentUri, id);
            identifiers.add(cursor.getLong(0));
            log.finer("Result item with identifier: " + id);

            MediaStorePhoto existingItem = allItemsContainer.getItem(id);
            if (existingItem == null) {
                // CREATE
                allItemsContainer.addItem(
                        new MediaStorePhoto(
                                cursor,
                                mediaStoreUri,
                                allItemsContainer.getId(),
                                MediaStoreContent.ID.appendRandom(allItemsContainer),
                                getUrlBuilder()
                        )
                );
                log.finer("Created new item for persistent id: " + id);
            } else {
                // UPDATE
                allItemsContainer.getItems().set(
                        allItemsContainer.getItems().indexOf(existingItem),
                        new MediaStorePhoto(
                                cursor,
                                mediaStoreUri,
                                existingItem.getParentID(),
                                existingItem.getId(),
                                getUrlBuilder()
                        )
                );
                log.finer("Updated item for persistent id: " + id);
            }

        } while(cursor.moveToNext());

        // DELETE
        allItemsContainer.removeItemsNotIn(identifiers);
        allItemsContainer.setChildCount(allItemsContainer.getItems().size());

        log.finer("Total items after create/update/delete: " + allItemsContainer.getChildCount());

        updateAlbums();
    }

    protected void updateAlbums() {
        AlbumsContainer albums = getAlbumsContainer();

        for (Container album : albums.getContainers()) {
            album.getItems().clear();
        }

        Photo[] allPhotos = getAllPhotosContainer().getPhotos();
        for (Photo photo : allPhotos) {
            if (photo.getAlbum() == null) continue;
            boolean addedToExistingAlbum = false;
            for (Container album : albums.getContainers()) {
                if (album.getTitle().equals(photo.getAlbum())) {
                    album.addItem(photo);
                    addedToExistingAlbum = true;
                    break;
                }
            }
            if (!addedToExistingAlbum) {
                Album newAlbum =
                        new Album(
                                MediaStoreContent.ID.appendRandom(this),
                                this,
                                photo.getAlbum(),
                                MediaStoreContent.CREATOR,
                                0
                        );
                albums.addContainer(newAlbum);
                newAlbum.addItem(photo);
            }
        }

        Iterator<Container> it = albums.getContainers().iterator();
        while (it.hasNext()) {
            Container album = it.next();
            album.setChildCount(album.getItems().size());
            if (album.getItems().size() == 0) {
                it.remove();
            }
        }
        albums.setChildCount(albums.getContainers().size());
    }

}
