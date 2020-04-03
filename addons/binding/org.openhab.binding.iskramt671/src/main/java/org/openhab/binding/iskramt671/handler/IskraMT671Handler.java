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
package org.openhab.binding.iskramt671.handler;

import static org.openhab.binding.iskramt671.IskraMT671BindingConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.iskramt671.internal.config.IskraMT671Config;
import org.openhab.binding.iskramt671.internal.connection.Connection;
import org.openhab.binding.iskramt671.internal.connection.DataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.CommPortIdentifier;

/**
 * The {@link IskraMT671Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Martin Gerczuk - Initial contribution
 */
// @NonNullByDefault
public class IskraMT671Handler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(IskraMT671Handler.class);

    ScheduledFuture<?> refreshJob = null;

    public IskraMT671Handler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // if (command instanceof RefreshType) {
        // Map<String, DataSet> dataSetMap = read();
        //
        // switch (channelUID.getId()) {
        // case CHANNEL_ID:
        // String id = dataSetMap.get("1-0:0.0.0*255").getValue();
        // logger.info("id = {}", id);
        // updateState(channelUID, new StringType(id));
        // break;
        // case CHANNEL_CONSUMPTION:
        // String consumption = dataSetMap.get("1-0:1.8.1*255").getValue();
        // logger.info("consumption = {}", consumption);
        // updateState(channelUID, new DecimalType(consumption));
        // break;
        // }

        // TODO: handle command

        // Note: if communication with thing fails for some reason,
        // indicate that by setting the status with detail information
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
        // "Could not control device at IP address x.x.x.x");
        // }
    }

    @Override
    public void initialize() {
        logger.info("Initializing ISKRA MT671 handler.");

        IskraMT671Config config = getConfigAs(IskraMT671Config.class);

        // TODO: Initialize the thing. If done set status to ONLINE to indicate proper working.
        // Long running initialization should be done asynchronously in background.
        updateStatus(ThingStatus.ONLINE);

        startAutomaticRefresh(config.refresh);
        // Note: When initialization can NOT be done set the status with more details for further
        // analysis. See also class ThingStatusDetail for all available status details.
        // Add a description to give user information to understand why thing does not work
        // as expected. E.g.
        // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
        // "Can not access device as username and/or password are invalid");
    }

    @Override
    public void dispose() {
        if (refreshJob != null) {
            refreshJob.cancel(true);
        }
    }

    private void startAutomaticRefresh(int refresh) {
        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            synchronized (CommPortIdentifier.class) {
                try {
                    Map<String, DataSet> dataSetMap = read();

                    String id = dataSetMap.get("1-0:0.0.0*255").getValue();
                    logger.debug("id = {}", id);
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_ID), new StringType(id));

                    String consumption = dataSetMap.get("1-0:1.8.1*255").getValue();
                    logger.debug("consumption = {}", consumption);
                    updateState(new ChannelUID(getThing().getUID(), CHANNEL_CONSUMPTION), new DecimalType(consumption));

                    updateStatus(ThingStatus.ONLINE);
                } catch (Exception e) {
                    logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        }, 0, refresh, TimeUnit.SECONDS);
    }

    /**
     * Reads data from meter
     *
     * @return a map of DataSet objects with the obis as key.
     */
    public Map<String, DataSet> read() {

        logger.debug("IskraMT671Handler.read");

        // the frequently executed code (polling) goes here ...
        Map<String, DataSet> dataSetMap = new HashMap<String, DataSet>();

        IskraMT671Config config = getConfigAs(IskraMT671Config.class);
        Connection connection = new Connection(config.port, null /* config.getInitMessage() */, false, 0);
        try {
            try {
                connection.open();
            } catch (IOException e) {
                logger.error("Failed to open serial port {}: {}", config.port, e.getMessage());
                return dataSetMap;
            }

            List<DataSet> dataSets = null;
            try {
                dataSets = connection.read();
                for (DataSet dataSet : dataSets) {
                    logger.debug("DataSet: {};{};{}", dataSet.getId(), dataSet.getValue(), dataSet.getUnit());
                    dataSetMap.put(dataSet.getId(), dataSet);
                }
            } catch (IOException e) {
                logger.error("IOException while trying to read: {}", e.getMessage());
            } catch (TimeoutException e) {
                logger.error("Read attempt timed out");
            }
        } finally {
            connection.close();
        }

        return dataSetMap;
    }
}
