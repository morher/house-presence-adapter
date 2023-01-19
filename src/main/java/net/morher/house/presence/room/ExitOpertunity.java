package net.morher.house.presence.room;

import java.io.Closeable;
import java.io.IOException;
import net.morher.house.api.entity.sensor.BinarySensorEntity;
import net.morher.house.api.subscription.Subscribable;
import net.morher.house.api.subscription.Subscription;

public class ExitOpertunity implements Closeable {
  private final Subscription subscription;
  private final BinarySensorEntity sensor;

  public ExitOpertunity(Subscribable<Boolean> topic, BinarySensorEntity sensor) {
    this.subscription = topic.subscribe(this::onStateChange);
    this.sensor = sensor;
  }

  private void onStateChange(boolean state) {
    if (sensor != null) {
      sensor.state().publish(state);
    }
  }

  @Override
  public void close() throws IOException {
    subscription.unsubscribe();
  }
}
