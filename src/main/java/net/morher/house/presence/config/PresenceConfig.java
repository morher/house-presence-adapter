package net.morher.house.presence.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import net.morher.house.api.config.DeviceName;

@Data
public class PresenceConfig {
  private Map<String, RoomConfig> rooms = new HashMap<>();

  @Data
  public static class RoomConfig {
    private DeviceName device;
    private boolean allExitsObserved;
    private long cooldown;

    @JsonProperty("motion-sensors")
    private Collection<MotionSensorConfig> motionSensors = new ArrayList<>();

    @JsonProperty("exit-opportunities")
    private Collection<ExitOpportunityConfig> exitOpportunities = new ArrayList<>();
  }

  @Data
  public static class MotionSensorConfig {
    private String topic;

    @JsonProperty("on-delay-ms")
    private long onDelayMs;

    @JsonProperty("off-delay-ms")
    private long offDelayMs;
  }

  @Data
  public static class ExitOpportunityConfig {
    private String topic;
    private String property;
    private DeviceName device;
    private String as;
  }
}
