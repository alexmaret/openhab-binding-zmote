package org.openhab.binding.zmote.internal.model;

public class ZMoteDevice {

    private final String make;
    private final String model;
    private final String revision;
    private final String type;
    private final String url;
    private final String uuid;

    public ZMoteDevice(final String make, final String type, final String model, final String revision,
            final String uuid, final String url) {
        this.make = make;
        this.model = model;
        this.revision = revision;
        this.type = type;
        this.uuid = uuid;
        this.url = url;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }

    public String getRevision() {
        return revision;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getUuid() {
        return uuid;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s [%s] @ %s", make, type, model, revision, uuid, url);
    }
}
