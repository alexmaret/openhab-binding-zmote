package org.openhab.binding.zmote.internal.service;

public interface IZMoteClient {

    void dispose();

    void initialize();

    void sendir(String uuid, String code);
}
