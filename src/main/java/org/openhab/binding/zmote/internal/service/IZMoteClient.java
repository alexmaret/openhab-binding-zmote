package org.openhab.binding.zmote.internal.service;

public interface IZMoteClient {

    void sendir(String uuid, String code, int timeout);
}
