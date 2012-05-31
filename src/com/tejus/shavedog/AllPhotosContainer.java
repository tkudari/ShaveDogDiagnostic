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

import org.teleal.cling.support.model.WriteStatus;
import org.teleal.cling.support.model.container.PhotoAlbum;
import org.teleal.cling.support.model.item.Item;
import org.teleal.cling.support.model.item.Photo;

import java.util.Iterator;
import java.util.List;

/**
 * @author Christian Bauer
 */
public class AllPhotosContainer extends PhotoAlbum {

    public AllPhotosContainer(PhotosContainer photoContainer) {
        setId(MediaStoreContent.ID.appendRandom(photoContainer));
        setParentID(photoContainer.getId());
        setTitle("All");
        setCreator(MediaStoreContent.CREATOR);
        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

    public MediaStorePhoto getItem(long mediaStoreId) {
        for (Item item : getItems()) {
            if (item instanceof MediaStorePhoto) {
                MediaStorePhoto photo = (MediaStorePhoto) item;
                if (photo.getMediaStoreId() == mediaStoreId)
                    return photo;
            }
        }
        return null;
    }

    public boolean containsItem(long mediaStoreId) {
        for (Photo p : getPhotos()) {
            if (((MediaStorePhoto)p).getMediaStoreId() == mediaStoreId) return true;
        }
        return false;
    }

    public void removeItemsNotIn(List<Long> mediaStoreIds) {
        Iterator<Item> it = getItems().iterator();
        while (it.hasNext()) {
            MediaStorePhoto photo = (MediaStorePhoto)it.next();
            boolean present = false;
            for (long mediaStoreId : mediaStoreIds) {
                if (photo.getMediaStoreId() == mediaStoreId) present = true;
            }
            if (!present) it.remove();
        }
    }

}
