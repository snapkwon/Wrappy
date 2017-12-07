package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 30/11/2017.
 */

public class SecurityQuestions implements Parcelable {
    private Integer index;
    private String question;
    private String answer;

    public SecurityQuestions(int index, String question, String answer) {
        this.index = index;
        this.question = question;
        this.answer = answer;
    }

    public SecurityQuestions() {}

    protected SecurityQuestions(Parcel in) {
        if (in.readByte() == 0) {
            index = null;
        } else {
            index = in.readInt();
        }
        question = in.readString();
        answer = in.readString();
    }

    public static final Creator<SecurityQuestions> CREATOR = new Creator<SecurityQuestions>() {
        @Override
        public SecurityQuestions createFromParcel(Parcel in) {
            return new SecurityQuestions(in);
        }

        @Override
        public SecurityQuestions[] newArray(int size) {
            return new SecurityQuestions[size];
        }
    };

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (index == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeInt(index);
        }
        parcel.writeString(question);
        parcel.writeString(answer);
    }
}
