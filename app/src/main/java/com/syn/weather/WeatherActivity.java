package com.syn.weather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.syn.weather.gson.Forecast;
import com.syn.weather.gson.Weather;
import com.syn.weather.util.HttpUtil;
import com.syn.weather.util.Utilist;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Text;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private TextView titleCity;
    private TextView titleUpdateTime;
    private TextView degreeText;
    private TextView weatherInfoText;
    private TextView aqiTxt;
    private TextView pm25txt;
    private TextView comfortText;
    private TextView carWashText;
    private TextView sportText;
    private LinearLayout forecastLayout;
    private ImageView iv_bg;
    private String img_url;

    public DrawerLayout drawerLayout;
    public SwipeRefreshLayout refreshLayout;
    private Button navButton;
    private String weatherId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStatusTitle();
        setContentView(R.layout.activity_weather);
        initView();
        setWeather();
    }

    private void setStatusTitle() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void setWeather() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherStr = sharedPreferences.getString("weather",null);
        if (weatherStr!=null){
            Weather weather = Utilist.handleWeatherResponse(weatherStr);
            weatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        }else {
            weatherId = getIntent().getStringExtra("weather_id");
            requestWeather(weatherId);
        }

        img_url = sharedPreferences.getString("bing_pic",null);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
    }

    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="+weatherId+"&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkhttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull final IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        e.printStackTrace();
                        Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                    }
                });
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseStr = response.body().string();
                final Weather weather = Utilist.handleWeatherResponse(responseStr);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather!=null){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseStr);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this,"获取天气信息失败",Toast.LENGTH_SHORT).show();
                        }
                   }
                });
                refreshLayout.setRefreshing(false);
            }
        });
        loadPic();
    }

    private void showWeatherInfo(Weather weather) {
        titleCity.setText(weather.basic.cityName);
        titleUpdateTime.setText(weather.basic.update.updataTime.split(" ")[1]);
        degreeText.setText(weather.now.temperature+"°C");
        weatherInfoText.setText(weather.now.more.info);
        forecastLayout.removeAllViews();
        for (Forecast forecast :weather.forecastList){
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dataText = view.findViewById(R.id.data_text);
            TextView infoText = view.findViewById(R.id.info_text);
            TextView maxText = view.findViewById(R.id.max_text);
            TextView minText = view.findViewById(R.id.min_text);
            dataText.setText(forecast.date);
            infoText.setText(forecast.more.info);
            maxText.setText(forecast.temperature.max);
            minText.setText(forecast.temperature.min);
            forecastLayout.addView(view);
        }

        if(weather.aqi!=null){
            aqiTxt.setText(weather.aqi.city.aqi);
            pm25txt.setText(weather.aqi.city.pm25);
        }

        comfortText.setText("舒适度："+weather.suggestion.comf.info);
        carWashText.setText("洗车指数："+weather.suggestion.cw.info);
        sportText.setText("运动建议："+weather.suggestion.sport.info);
    }

    private void initView() {
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        weatherInfoText = findViewById(R.id.weather_info_text);
        aqiTxt = findViewById(R.id.aqi_txt);
        pm25txt = findViewById(R.id.pm25_txt);
        comfortText = findViewById(R.id.comfort_text);
        carWashText = findViewById(R.id.car_wash_text);
        sportText = findViewById(R.id.sport_text);
        forecastLayout = findViewById(R.id.forecast_layout);
        iv_bg = findViewById(R.id.iv_bg);

        if (img_url!=null){
            Glide.with(this).load(img_url).into(iv_bg);
        }else {
            loadPic();
        }

        refreshLayout = findViewById(R.id.swipeRefresh);

        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void loadPic() {
        String requestPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkhttpRequest(requestPic, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull final Response response) throws IOException {
                final String url = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic",url);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(url).into(iv_bg);
                    }
                });
            }
        });
    }
}