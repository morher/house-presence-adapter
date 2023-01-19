package net.morher.house.presence;

import net.morher.house.api.context.HouseAdapter;
import net.morher.house.api.context.HouseMqttContext;
import net.morher.house.api.schedule.HouseScheduler;
import net.morher.house.presence.config.PresenceAdapterConfig;
import net.morher.house.presence.room.RoomPresenceController;

public class PresenceAdapter implements HouseAdapter {

  public static void main(String[] args) throws Exception {
    new PresenceAdapter().run(new HouseMqttContext("presence-adapter"));
  }

  @Override
  public void run(HouseMqttContext ctx) {
    PresenceAdapterConfig config = ctx.loadAdapterConfig(PresenceAdapterConfig.class);
    new RoomPresenceController(HouseScheduler.get(), ctx.deviceManager(), ctx.client())
        .configure(config.getPresence());
  }
}
