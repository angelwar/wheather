package com.syn.weather.db;

import org.litepal.crud.DataSupport;
import org.litepal.crud.DataSupport;

public class City extends DataSupport {
    private int id;
    private String cityName;
    private int cityCode;
    private int proVinceId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public int getCityCode() {
        return cityCode;
    }

    public void setCityCode(int cityCode) {
        this.cityCode = cityCode;
    }

    public int getProVinceId() {
        return proVinceId;
    }

    public void setProVinceId(int proVinceId) {
        this.proVinceId = proVinceId;
    }
}
