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
import org.teleal.cling.support.model.container.Container;

/**
 * @author Christian Bauer
 */
public class AlbumsContainer extends Container {

    public AlbumsContainer(PhotosContainer photoContainer) {
        setId(MediaStoreContent.ID.appendRandom(photoContainer));
        setParentID(photoContainer.getId());
        setTitle("Albums");
        setCreator(MediaStoreContent.CREATOR);
        setClazz(MediaStoreContent.CLASS_CONTAINER);
        setRestricted(true);
        setSearchable(false);
        setWriteStatus(WriteStatus.NOT_WRITABLE);
    }

}
