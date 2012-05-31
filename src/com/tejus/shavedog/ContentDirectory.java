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

import org.teleal.cling.support.contentdirectory.AbstractContentDirectoryService;
import org.teleal.cling.support.contentdirectory.ContentDirectoryErrorCode;
import org.teleal.cling.support.contentdirectory.ContentDirectoryException;
import org.teleal.cling.support.contentdirectory.DIDLParser;
import org.teleal.cling.support.model.BrowseFlag;
import org.teleal.cling.support.model.BrowseResult;
import org.teleal.cling.support.model.DIDLContent;
import org.teleal.cling.support.model.DIDLObject;
import org.teleal.cling.support.model.SortCriterion;
import org.teleal.cling.support.model.container.Container;
import org.teleal.cling.support.model.item.Item;

import java.util.logging.Logger;

/**
 * @author Christian Bauer
 */
public class ContentDirectory extends AbstractContentDirectoryService {

    final private static Logger log = Logger.getLogger(ContentDirectory.class.getName());

    final protected Content content;

    public ContentDirectory(Content content) {
        this.content = content;
    }

    public Content getContent() {
        return content;
    }

    @Override
    public BrowseResult browse(String objectID, BrowseFlag browseFlag,
                               String filter,
                               long firstResult, long maxResults,
                               SortCriterion[] orderby) throws ContentDirectoryException {
        try {
            DIDLContent didl = new DIDLContent();
            getContent().findObjectWithId( objectID );
            DIDLObject obj = getContent().findObjectWithId(objectID);

            if (obj == null) {
                log.fine("Object not found: " + objectID);
                return new BrowseResult(new DIDLParser().generate(didl), 0, 0);
            }

            // TODO: filter, sorting
            int count = 0;
            int totalMatches = 0;
            if (browseFlag.equals(BrowseFlag.METADATA)) {
                if (obj instanceof Container) {
                    log.fine("Browsing metadata of container: " + obj.getId());
                    didl.addContainer((Container) obj);
                    count++;
                    totalMatches++;
                } else if (obj instanceof Item) {
                    log.fine("Browsing metadata of item: " + obj.getId());
                    didl.addItem((Item) obj);
                    count++;
                    totalMatches++;
                }
            } else if (browseFlag.equals(BrowseFlag.DIRECT_CHILDREN)) {
                if (obj instanceof Container) {
                    log.fine("Browsing children of container: " + obj.getId());
                    Container container = (Container) obj;
                    boolean maxReached = maxResults == 0;
                    totalMatches = totalMatches + container.getContainers().size();
                    for (Container subContainer : container.getContainers()) {
                        if (maxReached) break;
                        if (firstResult > 0 && count == firstResult) continue;
                        didl.addContainer(subContainer);
                        count++;
                        if (count >= maxResults) maxReached = true;
                    }
                    totalMatches = totalMatches + container.getItems().size();
                    for (Item item : container.getItems()) {
                        if (maxReached) break;
                        if (firstResult > 0 && count == firstResult) continue;
                        didl.addItem(item);
                        count++;
                        if (count >= maxResults) maxReached = true;
                    }
                }
            }
            log.fine("Browsing result count: " + count + " and total matches: " + totalMatches);
            return new BrowseResult(new DIDLParser().generate(didl), count, totalMatches);

        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            throw new ContentDirectoryException(
                    ContentDirectoryErrorCode.CANNOT_PROCESS,
                    ex.toString()
            );
        }
    }
}

