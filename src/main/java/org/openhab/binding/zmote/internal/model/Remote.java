package org.openhab.binding.zmote.internal.model;

import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

/**
 * A remote as read from the remote configuration file.
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

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(final String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

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
