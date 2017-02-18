package org.openhab.binding.zmote.internal.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Button {

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    @SerializedName("code")
    private String code;

    @SerializedName("tcode")
    private String tcode;

    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

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
