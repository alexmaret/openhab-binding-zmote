/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.model;

import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A remote as read from the remote configuration file.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class Remote {
    @SerializedName("brand")
    private String brand;

    @SerializedName("model")
    private String model;

    @SerializedName("name")
    private String name;

    @SerializedName("keys")
    private Collection<Button> buttons;

    /**
     * @return The remote's brand name or null.
     */
    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    /**
     * @return The remote's model name or null.
     */
    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    /**
     * @return The user chosen name of the remote or null.
     */
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return A list of buttons defined for this remote or null.
     */
    public Collection<Button> getButtons() {
        return buttons;
    }

    public void setButtons(final Collection<Button> buttons) {
        this.buttons = buttons;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof Remote)) {
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
