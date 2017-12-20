package net.wrappy.im.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by CuongDuong on 12/20/2017.
 */

public class PopUpNotice {
    @SerializedName("id")
    private Integer id;
    @SerializedName("reference")
    private String reference;
    @SerializedName("title")
    private Title title;
    @SerializedName("detail")
    private Detail detail;
    @SerializedName("purpose")
    private String purpose;
    @SerializedName("status")
    private String status;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Title getTitle() {
        return title;
    }

    public void setTitle(Title title) {
        this.title = title;
    }

    public Detail getDetail() {
        return detail;
    }

    public void setDetail(Detail detail) {
        this.detail = detail;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
