package net.wrappy.im.model;

import java.util.ArrayList;

/**
 * Created by ben on 30/11/2017.
 */

public class Registration {
    private WpKAuthDto wpKAuthDto;
    private WpKMemberDto wpKMemberDto;
    ArrayList<SecurityQuestions> securityQuestions;

    public Registration(WpKAuthDto wpKAuthDto, WpKMemberDto wpKMemberDto, ArrayList<SecurityQuestions> securityQuestions) {
        this.wpKAuthDto = wpKAuthDto;
        this.wpKMemberDto = wpKMemberDto;
        this.securityQuestions = securityQuestions;
    }

    public Registration() {}

    public WpKAuthDto getWpKAuthDto() {
        return wpKAuthDto;
    }

    public void setWpKAuthDto(WpKAuthDto wpKAuthDto) {
        this.wpKAuthDto = wpKAuthDto;
    }

    public WpKMemberDto getWpKMemberDto() {
        return wpKMemberDto;
    }

    public void setWpKMemberDto(WpKMemberDto wpKMemberDto) {
        this.wpKMemberDto = wpKMemberDto;
    }

    public ArrayList<SecurityQuestions> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(ArrayList<SecurityQuestions> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }
}
