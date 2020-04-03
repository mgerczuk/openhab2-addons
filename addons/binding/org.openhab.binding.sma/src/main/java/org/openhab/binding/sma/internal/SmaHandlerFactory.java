/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.internal;

import static org.openhab.binding.sma.SmaBindingConstants.*;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.sma.handler.SmaBridgeHandler;
import org.openhab.binding.sma.handler.SmaHandler;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SmaHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Gerczuk - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.sma")
@NonNullByDefault
public class SmaHandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(SmaHandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE,
            THING_TYPE_INVERTER);

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_INVERTER);

    public SmaHandlerFactory() {
        logger.debug("SmaHandlerFactory()");
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        logger.debug("SmaHandlerFactory.supportsThingType({})", thingTypeUID.toString());
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();
        logger.debug("SmaHandlerFactory.createHandler({})", thingTypeUID.toString());

        if (thingTypeUID.equals(THING_TYPE_BRIDGE)) {
            return new SmaBridgeHandler((Bridge) thing);
        }
        if (thingTypeUID.equals(THING_TYPE_INVERTER)) {
            return new SmaHandler(thing);
        }

        return null;
    }
}
