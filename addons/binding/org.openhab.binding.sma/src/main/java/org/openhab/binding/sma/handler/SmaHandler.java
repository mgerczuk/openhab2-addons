/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.sma.handler;

import static org.openhab.binding.sma.SmaBindingConstants.PARAMETER_SUSYID;

import java.math.BigDecimal;
import java.util.Map.Entry;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.sma.internal.hardware.devices.BluetoothSolarInverterPlant.Data;
import org.openhab.binding.sma.internal.hardware.devices.SmaDevice.LRIDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmaHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Gerczuk - Initial contribution
 */
// @NonNullByDefault
public class SmaHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmaHandler.class);
    private int susyId;
    private SmaBridgeHandler bridgeHandler;

    public SmaHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (channelUID.getId().equals(CHANNEL_ETODAY)) {
        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {

        Bridge bridge = getBridge();
        bridgeHandler = bridge == null ? null : (SmaBridgeHandler) bridge.getHandler();

        BigDecimal dec = (BigDecimal) getConfig().get(PARAMETER_SUSYID);
        susyId = dec.intValue();

        bridgeHandler.registerInverter(susyId, this);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        // updateStatus(ThingStatus.INITIALIZING);

        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        bridgeHandler.unregisterInverter(susyId, this);
        super.dispose();
    }

    public void dataReceived(Data inv) {
        logger.debug("dataReceived");

        for (Entry<LRIDefinition, State> entry : inv.getEntries()) {
            updateState(new ChannelUID(getThing().getUID(), entry.getKey().getChannelId()), entry.getValue());
        }

        updateStatus(ThingStatus.ONLINE);
    }

    public void setOffline() {
        updateStatus(ThingStatus.OFFLINE);
    }
}
