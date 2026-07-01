package fun.cactus.modules;

import fun.cactus.utils.FirearmActionsType;
import fun.cactus.utils.sound.SoundEffect;

public class FirearmActionModule {
    FirearmActionsType type;
    int openDuration;
    int closeDuration;
    int closeShootDelay;
    int reloadOpenDelay;
    int reloadCloseDelay;
    SoundEffect[] soundOpen;
    SoundEffect[] soundClose;
}
