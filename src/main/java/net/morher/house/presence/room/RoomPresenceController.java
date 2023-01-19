package net.morher.house.presence.room;

import static net.morher.house.api.config.DeviceName.combine;
import static net.morher.house.api.mqtt.payload.BooleanMessage.onOff;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.morher.house.api.config.DeviceName;
import net.morher.house.api.devicetypes.RoomSensorDevice;
import net.morher.house.api.entity.Device;
import net.morher.house.api.entity.DeviceId;
import net.morher.house.api.entity.DeviceInfo;
import net.morher.house.api.entity.DeviceManager;
import net.morher.house.api.entity.sensor.BinarySensorEntity;
import net.morher.house.api.entity.sensor.BinarySensorOptions;
import net.morher.house.api.entity.sensor.BinarySensorType;
import net.morher.house.api.mqtt.client.HouseMqttClient;
import net.morher.house.api.mqtt.client.Topic;
import net.morher.house.api.mqtt.payload.BooleanMessage;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.api.subscription.Subscribable;
import net.morher.house.presence.config.PresenceConfig;
import net.morher.house.presence.config.PresenceConfig.ExitOpportunityConfig;
import net.morher.house.presence.config.PresenceConfig.MotionSensorConfig;
import net.morher.house.presence.config.PresenceConfig.RoomConfig;

@RequiredArgsConstructor
public class RoomPresenceController {
  private final HouseScheduler scheduler;
  private final DeviceManager deviceManager;
  private final HouseMqttClient mqtt;

  public void configure(PresenceConfig config) {
    for (Map.Entry<String, RoomConfig> room : config.getRooms().entrySet()) {
      configureRoom(room.getKey(), room.getValue());
    }
  }

  private void configureRoom(String roomName, RoomConfig config) {
    DeviceId deviceId =
        combine(config.getDevice(), new DeviceName(roomName, "Presence detector")).toDeviceId();

    Device device = deviceManager.device(deviceId);
    DeviceInfo deviceInfo = new DeviceInfo();
    deviceInfo.setManufacturer("House");
    deviceInfo.setModel("Presence Adapter");
    device.setDeviceInfo(deviceInfo);

    Room room =
        new Room(scheduler, device)
            .allExitsObserved(config.isAllExitsObserved())
            .cooldown(Duration.ofSeconds(config.getCooldown()));
    configureMotionSensors(room, config.getMotionSensors());
    config.getExitOpportunities().forEach(c -> this.configureExitOpertunity(room, c));
  }

  private void configureMotionSensors(Room room, Collection<MotionSensorConfig> motionSensors) {
    for (MotionSensorConfig sensorConfig : motionSensors) {
      configureMotionSensor(room, sensorConfig);
    }
  }

  private void configureMotionSensor(Room room, MotionSensorConfig config) {
    Subscribable<Boolean> topic = mqtt.topic(config.getTopic(), BooleanMessage.onOff());
    room.addMotionSensor(
        new MotionSensor(topic, scheduler, config.getOnDelayMs(), config.getOffDelayMs()));
  }

  private void configureExitOpertunity(Room room, ExitOpportunityConfig config) {
    Topic<Boolean> topic = mqtt.topic(config.getTopic(), onOff().inJsonField(config.getProperty()));
    BinarySensorEntity sensor = null;
    if (config.getDevice() != null && config.getAs() != null) {
      Device device =
          deviceManager.device(
              combine(config.getDevice(), new DeviceName(room.getName(), null)).toDeviceId());
      device.setDeviceInfo(new DeviceInfo());
      sensor =
          device.entity(
              RoomSensorDevice.OPENING, new BinarySensorOptions(getExitOpertunityType(config)));
    }
    new ExitOpertunity(topic, sensor);
  }

  private BinarySensorType getExitOpertunityType(ExitOpportunityConfig config) {
    String as = config.getAs();
    if (as != null) {
      switch (as.toLowerCase()) {
        case "door":
          return BinarySensorType.DOOR;

        case "window":
          return BinarySensorType.WINDOW;
      }
    }
    return BinarySensorType.OPENING;
  }
}
