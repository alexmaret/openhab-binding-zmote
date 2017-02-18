package org.openhab.binding.zmote.internal.service;

import org.openhab.binding.zmote.internal.model.ZMoteConfig;

public interface IZMoteService {

    boolean sendCode(ZMoteConfig config, String code);

    boolean sendCode(ZMoteConfig config, String code, int repeat);

    boolean sendKey(ZMoteConfig config, String button);

    boolean sendKey(ZMoteConfig config, String button, int repeat);

    void registerConfiguration(ZMoteConfig config);

    void unregisterConfiguration(ZMoteConfig config);
}
