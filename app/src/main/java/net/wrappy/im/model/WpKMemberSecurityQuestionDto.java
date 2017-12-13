package net.wrappy.im.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ben on 08/12/2017.
 */

public class WpKMemberSecurityQuestionDto extends T implements Parcelable {

    String answer;
    int id;
    int index;
    int member;
    String question;
    String reference;

    public WpKMemberSecurityQuestionDto() {}

    protected WpKMemberSecurityQuestionDto(Parcel in) {
        answer = in.readString();
        id = in.readInt();
        index = in.readInt();
        member = in.readInt();
        question = in.readString();
        reference = in.readString();
    }

    public static final Creator<WpKMemberSecurityQuestionDto> CREATOR = new Creator<WpKMemberSecurityQuestionDto>() {
        @Override
        public WpKMemberSecurityQuestionDto createFromParcel(Parcel in) {
            return new WpKMemberSecurityQuestionDto(in);
        }

        @Override
        public WpKMemberSecurityQuestionDto[] newArray(int size) {
            return new WpKMemberSecurityQuestionDto[size];
        }
    };

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getMember() {
        return member;
    }

    public void setMember(int member) {
        this.member = member;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(answer);
        parcel.writeInt(id);
        parcel.writeInt(index);
        parcel.writeInt(member);
        parcel.writeString(question);
        parcel.writeString(reference);
    }
}
