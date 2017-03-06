/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A button as returned from the remote configuration file.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class Button {

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("tcode")
    private String tcode;

    /**
     * @return The button identifier or null.
     */
    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    /**
     * @return The button name as defined by the user or null.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return The IR code for this button or null.
     */
    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    /**
     * @return The toggle IR code for this button or null.
     */
    public String getTcode() {
        return tcode;
    }

    public void setTcode(final String tcode) {
        this.tcode = tcode;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Button)) {
            return false;
        }

        return toString().equals(other);
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        final Gson gson = new Gson();
        return gson.toJson(this);
    }
}
