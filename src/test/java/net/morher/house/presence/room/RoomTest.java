package net.morher.house.presence.room;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static net.morher.house.api.mqtt.payload.BooleanMessage.onOff;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.morher.house.api.devicetypes.RoomSensorDevice;
import net.morher.house.api.entity.Device;
import net.morher.house.api.entity.DeviceId;
import net.morher.house.api.entity.DeviceManager;
import net.morher.house.api.entity.EntityManager;
import net.morher.house.api.mqtt.client.HouseMqttClient;
import net.morher.house.api.mqtt.client.Topic;
import net.morher.house.api.subscription.Subscribable;
import net.morher.house.test.client.TestHouseMqttClient;
import net.morher.house.test.schedule.TestHouseScheduler;
import org.junit.Test;

public class RoomTest {
  private final TestHouseScheduler scheduler = new TestHouseScheduler();
  private final HouseMqttClient client = TestHouseMqttClient.loopback();
  private final EntityManager entityManager = new EntityManager(client);
  private final DeviceManager deviceManager = new DeviceManager(entityManager);

  private final Topic<Boolean> motionTopic = client.topic("sensor/motion", onOff());
  private final Device device = deviceManager.device(new DeviceId("room", "device"));
  private final List<Boolean> motionStates =
      capture(device.entity(RoomSensorDevice.MOTION).state());
  private final List<Boolean> presenceStates =
      capture(device.entity(RoomSensorDevice.PRESENCE).state());

  @Test
  public void testReportMotion() {

    Room room = new Room(scheduler, device);
    room.addMotionSensor(new MotionSensor(motionTopic, scheduler, 0, 1000));

    motionTopic.publish(true);

    scheduler.runWaitingTasks();

    assertThat(motionStates, hasItems(true));
    assertThat(presenceStates, hasItems(true));
  }

  @Test
  public void testReportNoMotion() {
    Room room = new Room(scheduler, device);
    room.cooldown(Duration.ofSeconds(15));
    room.addMotionSensor(new MotionSensor(motionTopic, scheduler, 0, 1000));

    motionTopic.publish(true);

    scheduler.skipAhead(10, SECONDS);
    motionTopic.publish(false);

    scheduler.skipAhead(1000, MILLISECONDS);
    assertThat(motionStates, hasItems(true));
    assertThat(presenceStates, hasItems(true));

    scheduler.runWaitingTasks();
    assertThat(motionStates, hasItems(true, false));
    assertThat(presenceStates, hasItems(true));

    scheduler.skipAhead(15, SECONDS);
    assertThat(presenceStates, hasItems(true));

    scheduler.runWaitingTasks();
    assertThat(motionStates, hasItems(true, false));
    assertThat(presenceStates, hasItems(true, false));
  }

  private <T> List<T> capture(Subscribable<T> topic) {
    List<T> captor = new ArrayList<>();
    topic.subscribe(captor::add);
    return captor;
  }
}
