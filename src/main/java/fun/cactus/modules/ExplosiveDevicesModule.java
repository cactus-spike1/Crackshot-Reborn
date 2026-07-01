package fun.cactus.modules;

import fun.cactus.utils.DeviceType;
import fun.cactus.utils.devices.DeviceInfo;
import fun.cactus.utils.devices.MineDeviceInfo;
import fun.cactus.utils.sound.SoundEffect;

public class ExplosiveDevicesModule {
    boolean enable;
    DeviceType deviceType;
    DeviceInfo deviceInfo;
    SoundEffect[] soundsDeploy;
    boolean remoteBypassRegions;
    String messageDisarm;
    String messageTriggerPlacer;
    String messageTriggerVictim;
    SoundEffect[] SoundsAlertPlacer;
    SoundEffect[] soundsTrigger;
}
