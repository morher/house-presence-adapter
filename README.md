# Presence adapter

Detects and estimates presence in a given room, or an agregation of rooms based on a range of sensors.

Note: This adapter do not perform any action or automations, it simply reports the estimated presence, and optionally individual sensor states.


## Inspiratios & scenarios

Presence can be detected based on:
- Motion in a room, detected by a PIR or microwave radar.
- Active use of a device inside the room, such as a computer, lightswitch, remote control, hairdryer or electric toothbrush.
- Door lock state. The bathroom is occupied when the door is locked. The storage room is occupied when the door is unlocked.
- Preasure sensors in furniture.

Presence can be estimated based on:
- Cooldown time - People usually don't just dissapear in thin air.
- Exit options - If all doors are closed and presence is detected, the room will likely be occupied until one of the doors opens.
- Detected exit - If all adjacent areas have presence detection the room will likely be occupied until presence is detected in one of those areas.

Keep in mind that motion sensors can detect activity through doors, windows and other openings. They can also report false positives. Presence estimates should therefore time out after a certain amount of time. A false detection in a storage room should not mark the room as occupied forever.

There can be multiple people in the room, thus it can be hard to detect when the room is fully vacated.

### Room with motion sensor
A hallway can easily be fitted with a PIR sensor, providing near instant feedback when someone enters the room. Standing still however, the PIR sensor will no longer detect you presence. This can be mitigated with a cooldown period.

```yaml
presence:
  rooms:
    - device:
        room: Living room
        name: Presence sensor
      motion-sensors:
        - topic: stat/livingroom-pir/POWER1
          cooldown: 3000
```


### Room with multiple sensors
Imagine a PIR-sensor in one ceiling corner of a kitchen. It will for sure trigger when you enter, but it might quickly stop detecting you while you prepare you breakfast over the kitchen bench. Having multiple PIR-sensors in the kitchen, or add a microwave sensor, might help improve accuracy without extending the cooldown period too much.

```yaml
presence:
  rooms:
    - device:
        room: Living room
        name: Presence sensor
      motion-sensors:
        - topic: stat/kitchen-ceiling-pir/POWER1
          cooldown: 3000
        - topic: stat/kitchen-radar/POWER1
          cooldown: 3000
        - topic: stat/kitchen-bench-pir1/POWER1
          cooldown: 3000
```

### Room with doorlock sensor
We can assume that the bathroom is occupied when the door is locked. Presence detected can also be 