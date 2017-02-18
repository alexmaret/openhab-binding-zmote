/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ZMoteBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class ZMoteBindingConstants {

    public static final String BINDING_ID = "zmote";

    public final static ThingTypeUID THING_TYPE_ZMT2 = new ThingTypeUID(BINDING_ID, "zmt2");

    public final static int DISCOVERY_INTERVAL = 60;
    public final static int DISCOVERY_TIMEOUT = 30;

    public final static String CHANNEL_ONLINE = "online";
    public final static String CHANNEL_SENDCODE = "sendcode";
    public final static String CHANNEL_SENDKEY = "sendkey";

    public final static String CONFIG_UUID = "uuid";
    public final static String CONFIG_FILE = "configFile";
    public final static String CONFIG_URL = "url";
    public final static String CONFIG_RETRY = "retry";
    public final static String CONFIG_TIMEOUT = "timeout";
    public final static String CONFIG_REMOTE = "remote";

    public static final String PROP_UUID = CONFIG_UUID;
    public static final String PROP_URL = CONFIG_URL;
    public static final String PROP_MAKE = "make";
    public static final String PROP_MODEL = "model";
    public static final String PROP_REVISION = "revision";
    public static final String PROP_TYPE = "type";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ZMT2);
}
