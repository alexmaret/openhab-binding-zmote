/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zmote.internal.handler;

import static org.openhab.binding.zmote.ZMoteBindingConstants.THING_TYPE_ZMT2;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.openhab.binding.zmote.handler.ZMoteHandler;
import org.openhab.binding.zmote.internal.discovery.IZMoteDiscoveryService;
import org.openhab.binding.zmote.internal.service.IZMoteService;

/**
 * The {@link ZMoteHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Alexander Maret-Huskinson - Initial contribution
 */
public class ZMoteHandlerFactory extends BaseThingHandlerFactory {

    private final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ZMT2);

    private IZMoteService zmoteService = null;
    private IZMoteDiscoveryService zmoteDiscoveryService = null;

    @Override
    public boolean supportsThingType(final ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected ThingHandler createHandler(final Thing thing) {

        if ((zmoteService == null) || (zmoteDiscoveryService == null)) {
            throw new IllegalStateException("Cannot create ZMote thing as the required services are not available!");
        }

        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_ZMT2)) {
            return new ZMoteHandler(thing, zmoteService, zmoteDiscoveryService);
        }

        return null;
    }

    // used by OSGI to set the service
    protected void setZMoteDiscoveryService(final IZMoteDiscoveryService zmoteDiscoveryService) {
        this.zmoteDiscoveryService = zmoteDiscoveryService;
    }

    // used by OSGI to set the service
    protected void setZMoteService(final IZMoteService service) {
        zmoteService = service;
    }

    // used by OSGI to unset the service
    protected void unsetZMoteDiscoveryService(final IZMoteDiscoveryService zmoteDiscoveryService) {
        this.zmoteDiscoveryService = null;
    }

    // used by OSGI to unset the service
    protected void unsetZMoteService(final IZMoteService service) {
        zmoteService = null;
    }
}
