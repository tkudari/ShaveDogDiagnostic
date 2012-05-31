package com.tejus.shavedog;

import org.teleal.cling.support.model.DIDLObject;

/**
 * @author Christian Bauer
 */
public interface URLBuilder {

    String getURL(DIDLObject object);

    String getObjectId(String urlPath);



}
