package net.wrappy.im.model;

import android.graphics.drawable.Drawable;

import java.util.Date;

/**
 * Created by PCPV on 11/29/2017.
 */

public class ModelHistoryTransaction {
    private String Title;
    private Drawable Icon;
    private String Time;
    private Date Datetime;
    private String Number;
    private String Address;
    private long ConfirmNumber;
    private String toUSD;
    private String txHash;

    public ModelHistoryTransaction()
    {
        Title ="";
        Icon = null;
        Time = "";
        Datetime =null;
        Number = "0";
        Address = "";
        txHash = "";
        ConfirmNumber =0;
    }

    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        this.Title = title;
    }
    public Drawable getIcon() {
        return Icon;
    }
    public void setIcon(Drawable icon)  {
        this.Icon = icon;
    }
    public String getTime() {
        return Time;
    }
    public void setTime(String time)  {
        this.Time = time;
    }
    public String getNumber() {
        return Number;
    }
    public void setNumber(String number)  {
        this.Number = number;
    }
    public String getAddress() {
        return Address;
    }
    public void setAddress(String address)  {
        this.Address = address;
    }
    public long getConfirmNumber() {
        return ConfirmNumber;
    }
    public void setConfirmNumber(long confirmnumber)  {
        this.ConfirmNumber = confirmnumber;
    }
    public Date getDate() {
        return Datetime;
    }
    public void setDate(Date datetime)  {
        this.Datetime = datetime;
    }
    public String getToUSD() {
        return toUSD;
    }
    public void setToUSD(String toUSD)  {
        this.toUSD = toUSD;
    }
    public String getTXHash() {
        return txHash;
    }
    public void setTXHash(String txHash)  {
        this.txHash = txHash;
    }
}
