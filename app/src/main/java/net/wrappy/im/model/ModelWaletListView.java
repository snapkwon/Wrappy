package net.wrappy.im.model;

import android.graphics.drawable.Drawable;

/**
 * Created by PCPV on 11/29/2017.
 */

public class ModelWaletListView  {
    private int typecoin;
    private String NameCoin;
    Drawable Icon;
    private String Number;
    private String PriceCoin;
    private String Percent;
    private String Value;
    private int TextColor;

    public ModelWaletListView()
    {
        typecoin = 0;
        NameCoin = "";
        Icon = null;
        Number = "0";
        PriceCoin = "0";
        Percent = "0";
        Value = "0";
        TextColor = 0;
    }

    public int getTypeCoin() {
        return typecoin;
    }
    public void setTypeCoin(int type) {
        this.typecoin = type;
    }

    public String getNameCoin() {
        return NameCoin;
    }
    public void setNameCoin(String namecoin) {
        this.NameCoin = namecoin;
    }
    public Drawable getIcon() {
        return Icon;
    }
    public void setIcon(Drawable icon)  {
        this.Icon = icon;
    }
    public String getPriceCoin() {
        return PriceCoin;
    }
    public void setPriceCoin(String pricecoin)  {
        this.PriceCoin = pricecoin;
    }
    public String getPercent() {
        return Percent;
    }
    public void setPercent(String percent)  {
        this.Percent = percent;
    }
    public String getValue() {
        return Value;
    }
    public void setValue(String value)  {
        this.Value = value;
    }
    public int getTextColor() {
        return TextColor;
    }
    public void setTextCoin(int textcolor) {
        this.TextColor = textcolor;
    }
    public String getNumber() {
        return Number;
    }
    public void setNumber(String number) {
        this.Number = number;
    }
}
