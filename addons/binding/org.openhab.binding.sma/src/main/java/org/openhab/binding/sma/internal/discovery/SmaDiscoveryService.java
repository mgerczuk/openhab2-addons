package org.openhab.binding.sma.internal.discovery;

import static org.openhab.binding.sma.SmaBindingConstants.*;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.sma.handler.SmaBridgeHandler;
import org.openhab.binding.sma.internal.SmaHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmaDiscoveryService extends AbstractDiscoveryService {

    private final Logger logger = LoggerFactory.getLogger(SmaDiscoveryService.class);

    SmaBridgeHandler bridgeHandler;

    public SmaDiscoveryService(SmaBridgeHandler handler) throws IllegalArgumentException {
        super(SmaHandlerFactory.DISCOVERABLE_THING_TYPES_UIDS, 10);
        logger.info("SmaDiscoveryService()");

        bridgeHandler = handler;
    }

    @Override
    protected void startScan() {
        logger.info("startScan()");

        // scheduler.schedule(new Runnable() {
        // @Override
        // public void run() {
        // try {
        // notifyDiscovery(99);
        // notifyDiscovery(113);
        // } catch (Exception e) {
        // logger.error("Error scanning for devices", e);
        //
        // if (scanListener != null) {
        // scanListener.onErrorOccurred(e);
        // }
        // }
        // }
        // }, 0, TimeUnit.SECONDS);
    }

    public void notifyDiscovery(int susyId, String label) {
        ThingUID bridgeUID = this.bridgeHandler.getThing().getUID();
        ThingUID uid = new ThingUID(THING_TYPE_INVERTER, bridgeUID, ((Integer) susyId).toString());

        Map<String, Object> properties = new HashMap<>();
        properties.put(PARAMETER_SUSYID, susyId);

        DiscoveryResult result = DiscoveryResultBuilder.create(uid).withBridge(bridgeUID).withLabel(label)
                .withProperties(properties).withRepresentationProperty(PARAMETER_SUSYID).build();
        thingDiscovered(result);
    }
}
