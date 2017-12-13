package net.wrappy.im.enums;

import net.wrappy.im.R;
import net.wrappy.im.model.Presence;

/**
 * Created by Khoa.Nguyen on 12/13/2017.
 */

public enum EnumPresenceStatus {
    OFFLINE(Presence.OFFLINE, R.color.holo_grey_dark, "offline"),
    INVISIBLE(Presence.INVISIBLE, android.R.color.transparent, "invisible"),
    AWAY(Presence.AWAY, R.color.holo_orange_light, "away"),
    IDLE(Presence.IDLE, R.color.holo_green_dark, "idle"),
    DO_NOT_DISTURB(Presence.DO_NOT_DISTURB, R.color.holo_red_dark, "do not distub"),
    AVAILABLE(Presence.AVAILABLE, R.color.holo_green_light, "available");

    int presense;
    int color;
    String status;

    EnumPresenceStatus(int presense, int color, String status) {
        this.presense = presense;
        this.color = color;
        this.status = status;
    }

    public int getColor() {
        return color;
    }

    public String getStatus() {
        return status;
    }

    public static EnumPresenceStatus getStatus(int presense) {
        for (EnumPresenceStatus status : values()) {
            if (presense == status.presense) return status;
        }
        return OFFLINE;
    }
}
