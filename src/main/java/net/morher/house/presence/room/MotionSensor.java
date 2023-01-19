package net.morher.house.presence.room;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.api.state.DelayedStateChange;
import net.morher.house.api.subscription.Subscribable;
import net.morher.house.api.subscription.Subscription;

public class MotionSensor implements Closeable {
  private final Subscription subscription;
  private final DelayedStateChange<Boolean> delayedChange;

  @Getter private boolean motionDetected;
  private List<MotionSensorListener> listeners = new ArrayList<>();

  public MotionSensor(
      Subscribable<Boolean> topic, HouseScheduler scheduler, long onDelayMs, long offDelayMs) {
    this.delayedChange =
        new DelayedStateChange<>(scheduler, this::handleMotionChanged, Duration.ofMillis(onDelayMs))
            .delayChangeTo(false, Duration.ofMillis(offDelayMs));
    this.subscription = topic.subscribe(delayedChange::reportState);
  }

  private void handleMotionChanged(boolean detected) {
    this.motionDetected = detected;
    listeners.forEach(l -> l.onMotionStateChanged(detected));
  }

  public void addListener(MotionSensorListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void close() throws IOException {
    subscription.unsubscribe();
  }

  public interface MotionSensorListener {
    void onMotionStateChanged(boolean motionDetectedState);
  }
}
