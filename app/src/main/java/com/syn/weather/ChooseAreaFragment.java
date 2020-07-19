package com.syn.weather;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.CarrierConfigManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.syn.weather.db.City;
import com.syn.weather.db.County;
import com.syn.weather.db.Province;
import com.syn.weather.util.HttpUtil;
import com.syn.weather.util.Utilist;

import org.jetbrains.annotations.NotNull;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.syn.weather.util.HttpUtil.BASE_URL;

public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;
    private ProgressDialog progressDialog;
    private TextView tv_title;
    private Button iv_back;
    private ListView listView;

    private ArrayAdapter<String> adapter;
    private List<String> dataList = new ArrayList<>();
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private Province selectProvince;
    private City selectCity;
    private int currentLevel;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        tv_title = view.findViewById(R.id.title_text);
        iv_back = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);
        adapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE){
                    selectProvince = provinceList.get(position);
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    selectCity = cityList.get(position);
                    queryCounties();
                }else if(currentLevel ==LEVEL_COUNTY){
                    String weatherId = countyList.get(position).getWeatherId();
                    if(getActivity() instanceof MainActivity){
                        Intent intent = new Intent(getActivity(),WeatherActivity.class);
                        intent.putExtra("weather_id",weatherId);
                        getActivity().startActivity(intent);
                        getActivity().finish();
                    }else if(getActivity() instanceof WeatherActivity){
                        WeatherActivity a = (WeatherActivity) getActivity();
                        a.drawerLayout.closeDrawers();
                        a.refreshLayout.setRefreshing(true);
                        a.requestWeather(weatherId);
                    }

                }
            }
        });
        iv_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });
        super.onActivityCreated(savedInstanceState);

        queryProvinces();
    }


    //先从数据库中获取数据，如果没有就到服务器上查询
    private void queryProvinces() {
        tv_title.setText("中国");
        iv_back.setVisibility(View.GONE);
        provinceList = DataSupport.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province:provinceList) {
                dataList.add(province.getProVinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else {
            queryFromServer(BASE_URL,"province");
        }
    }

    private void queryCities() {
        tv_title.setText(selectProvince.getProVinceName());
        iv_back.setVisibility(View.VISIBLE);
        cityList = DataSupport.where("provinceid = ?",String.valueOf(selectProvince.getId())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city:cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            int provinceCode = selectProvince.getProVinceCode();
            String address = BASE_URL+provinceCode;
            queryFromServer(address,"city");
        }
    }

    private void queryCounties() {
        tv_title.setText(selectCity.getCityName());
        iv_back.setVisibility(View.VISIBLE);
        countyList = DataSupport.where("cityid = ?", String.valueOf(selectCity.getId())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            int provinceCode = selectProvince.getProVinceCode();
            int cityCode = selectCity.getCityCode();
            String address = BASE_URL + provinceCode+"/"+cityCode;
            queryFromServer(address, "county");
        }
    }

    private void queryFromServer(String address,final String type){
        showProgressDialog();
        HttpUtil.sendOkhttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();
                closeProgressDialog();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败",Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseStr = response.body().string();
                boolean result = false;
                if ("province".equals(type)){
                    result = Utilist.handleProvinceResponse(responseStr);
                }else if ("city".equals(type)){
                    result =Utilist.handleCityResponse(responseStr,selectProvince.getId());
                }else if ("county".equals(type)){
                    result = Utilist.handleCountyResponse(responseStr,selectCity.getId());
                }

                if (result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeProgressDialog();
                            if ("province".equals(type)){
                                queryProvinces();
                            }else if ("city".equals(type)){
                                queryCities();
                            }else if ("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }

            }
        });
    }

    private void showProgressDialog(){
        if(progressDialog==null){
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("正在加载");
            progressDialog.setCanceledOnTouchOutside(true);
        }
        progressDialog.show();
    }

    private void closeProgressDialog(){
        if(progressDialog!=null){
            progressDialog.dismiss();
        }

    }
}
