/**
 * Copyright (c) 2014,2017 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.iskramt671.internal;

import static org.openhab.binding.iskramt671.IskraMT671BindingConstants.THING_TYPE_METER;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.iskramt671.handler.IskraMT671Handler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link IskraMT671HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Martin Gerczuk - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.iskramt671")
@NonNullByDefault
public class IskraMT671HandlerFactory extends BaseThingHandlerFactory {

    private final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IskraMT671HandlerFactory.class);

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_METER);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {

        boolean result = SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
        logger.debug("IskraMT671HandlerFactory.supportsThingType = {}", result);
        return result;
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        logger.debug("IskraMT671HandlerFactory.createHandler");

        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_METER)) {
            return new IskraMT671Handler(thing);
        }

        return null;
    }
}
