package net.wrappy.im.model;
/*
* Created by Khoa.Nguyen
* */

import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import net.wrappy.im.ImApp;
import net.wrappy.im.R;
import net.wrappy.im.ui.conference.ConferenceConstant;

/**
 * Represents an instant conference type between users.
 */
public class ConferenceMessage {
    private String mFrom;
    private String mTo;
    private String roomId;
    private long mDuration;
    private boolean isGroup;
    private ConferenceType mType;
    private ConferenceState mState;

    public ConferenceMessage(String mFrom, String mTo, boolean isGroup, ConferenceType mType, ConferenceState mState) {
        this.mFrom = mFrom;
        this.mTo = mTo;
        this.mType = mType;
        this.mState = mState;
        this.isGroup = isGroup;
        this.roomId = generateRoomId();
    }

    public ConferenceMessage(String message) {
        this(message.replaceFirst(ConferenceConstant.REGEX, "").split(ConferenceConstant.REGEX));
    }

    private ConferenceMessage(String[] params) {
        if (params != null) {
            if (params.length > 2) {
                roomId = params[1].replace(ConferenceConstant.CONFERENCE_BRIDGE, "");
                this.mType = ConferenceType.getEnumByValue(params[2]);
                this.isGroup = roomId.contains("group.");
            }
            if (!ConferenceConstant.KEY.equals(params[0])) {
                this.mState = ConferenceState.END;
            } else if (params.length > 3) {
                this.mState = ConferenceState.getEnumByValue(params[3]);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        String breakChar = ConferenceConstant.REGEX;
        stringBuffer.append(ConferenceConstant.CONFERENCE_PREFIX)
                .append(breakChar)
                .append(ConferenceConstant.CONFERENCE_BRIDGE + roomId)
                .append(breakChar)
                .append(mType.type);
        if (mState != null) {
            stringBuffer.append(breakChar)
                    .append(mState.state);
        }

        return stringBuffer.toString();
    }

    public enum ConferenceType {
        AUDIO("audio"),
        VIDEO("video");

        private String type;

        ConferenceType(String type) {
            this.type = type;
        }

        static ConferenceType getEnumByValue(String value) {
            for (ConferenceType val : values()) {
                if (val.type.equals(value)) {
                    return val;
                }
            }
            return AUDIO;
        }

        public String getType() {
            return type;
        }
    }

    public enum ConferenceState {
        OUTGOING("OUTGOING", R.drawable.chat_outcall),
        INCOMING("INCOMING", R.drawable.chat_incall),
        MISSED("MISSED", R.drawable.chat_misscall),
        DECLINED("DECLINED", R.drawable.chat_incall),
        END("END", R.drawable.chat_incall);
        private String state;
        private int icon;

        ConferenceState(String state, int icon) {
            this.state = state;
            this.icon = icon;
        }

        static ConferenceState getEnumByValue(String value) {
            for (ConferenceState val : values()) {
                if (val.state.equals(value)) {
                    return val;
                }
            }
            return END;
        }

        public String getState() {
            return state.toLowerCase();
        }

        public Drawable getIcon() {
            return ContextCompat.getDrawable(ImApp.sImApp, icon);
        }
    }

    public String getmFrom() {
        return mFrom;
    }

    public String getmTo() {
        return mTo;
    }

    public String getRoomId() {
        return roomId;
    }

    public long getmDuration() {
        return mDuration;
    }

    public boolean isGroup() {
        return isGroup;
    }

    public boolean isAudio() {
        return mType == ConferenceType.AUDIO;
    }

    public boolean isDecline() {
        return mState == ConferenceState.DECLINED;
    }

    public boolean isMissedOrDeclined() {
        return mState == ConferenceState.DECLINED || mState == ConferenceState.MISSED;
    }

    public boolean isEnded() {
        return mState != null && mState == ConferenceState.END;
    }

    public ConferenceType getType() {
        return mType;
    }

    public ConferenceState getState() {
        return mState != null ? mState : ConferenceState.END;
    }

    public void endCall() {
        mState = ConferenceState.END;
    }

    public void accept() {
        mState = ConferenceState.INCOMING;
    }

    public void outgoing() {
        mState = ConferenceState.OUTGOING;
    }

    public void decline() {
        mState = ConferenceState.DECLINED;
    }

    public void missed() {
        mState = ConferenceState.MISSED;
    }

    public String generateRoomId() {
        String roomId = mFrom + "." + mTo + "." + System.currentTimeMillis();
        if (isGroup) {
            roomId = "group." + roomId;
        }
        return roomId;
    }
}
