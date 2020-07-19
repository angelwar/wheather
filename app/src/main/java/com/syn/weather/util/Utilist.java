package com.syn.weather.util;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.syn.weather.db.City;
import com.syn.weather.db.County;
import com.syn.weather.db.Province;
import com.syn.weather.gson.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilist {
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray all = new JSONArray(response);
                for (int i = 0;i<all.length();i++){
                    JSONObject jsonObject = (JSONObject) all.get(i);
                    Province proVince = new Province();
                    proVince.setProVinceCode(jsonObject.getInt("id"));
                    proVince.setProVinceName(jsonObject.getString("name"));
                    proVince.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return false;
    }
    public static boolean handleCityResponse(String response,int provinceId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray all = new JSONArray(response);
                for (int i = 0;i<all.length();i++){
                    JSONObject jsonObject = (JSONObject) all.get(i);
                    City city = new City();
                    city.setCityCode(jsonObject.getInt("id"));
                    city.setCityName(jsonObject.getString("name"));
                    city.setProVinceId(provinceId);
                    city.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return false;
    }
    public static boolean handleCountyResponse(String response,int cityId){
        if (!TextUtils.isEmpty(response)){
            try{
                JSONArray all = new JSONArray(response);
                for (int i = 0;i<all.length();i++){
                    JSONObject jsonObject = (JSONObject) all.get(i);
                    County county = new County();
                    county.setWeatherId(jsonObject.getString("weather_id"));
                    county.setCountyName(jsonObject.getString("name"));
                    county.setCityId(cityId);
                    county.save();
                }

                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }

        return false;
    }

    public static Weather handleWeatherResponse(String response){
        try{
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather");
            String weather = jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weather,Weather.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
