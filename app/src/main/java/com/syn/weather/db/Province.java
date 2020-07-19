package com.syn.weather.db;

import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    private int id;
    private String proVinceName;
    private int proVinceCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProVinceName() {
        return proVinceName;
    }

    public void setProVinceName(String proVinceName) {
        this.proVinceName = proVinceName;
    }

    public int getProVinceCode() {
        return proVinceCode;
    }

    public void setProVinceCode(int proVinceCode) {
        this.proVinceCode = proVinceCode;
    }
}
