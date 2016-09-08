package com.ahmedadeltito.retrofitmultiplerequests.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ahmedadeltito.retrofitmultiplerequests.DownloadResultReceiver;
import com.ahmedadeltito.retrofitmultiplerequests.MainApplication;
import com.ahmedadeltito.retrofitmultiplerequests.R;
import com.ahmedadeltito.retrofitmultiplerequests.backgroundservice.DownloadWeatherService;
import com.ahmedadeltito.retrofitmultiplerequests.model.Model;

import java.util.List;

import io.realm.Realm;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, DownloadResultReceiver.Receiver {

    private Button startServiceButton, getWeatherDataButton;
    private TextView resultTextView;
    private DownloadResultReceiver downloadResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startServiceButton = (Button) findViewById(R.id.start_service_btn);
        getWeatherDataButton = (Button) findViewById(R.id.get_weather_btn);
        resultTextView = (TextView) findViewById(R.id.result_tv);

        resultTextView.setMovementMethod(new ScrollingMovementMethod());

        getWeatherDataButton.setVisibility(View.GONE);

        downloadResultReceiver = new DownloadResultReceiver(new Handler());
        downloadResultReceiver.setReceiver(this);

        startServiceButton.setOnClickListener(this);
        getWeatherDataButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_service_btn:
                Intent intent = new Intent(Intent.ACTION_SYNC, null, MainActivity.this, DownloadWeatherService.class);
                intent.putExtra("receiver", downloadResultReceiver);
                startService(intent);
                break;
            case R.id.get_weather_btn:
                getWeatherData();
                break;
        }
    }

    private void getWeatherData() {
        Realm realm = ((MainApplication) getApplicationContext()).getRealm();
        List<Model> modelList = realm.where(Model.class).findAll();
        String modelResult = "";
        for (int i = 0; i < modelList.size(); i++) {
            modelResult += modelList.get(i).getName() + "\n";
        }
        resultTextView.setText(modelResult);
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DownloadWeatherService.STATUS_FINISHED) {
            getWeatherDataButton.setVisibility(View.VISIBLE);
        }
    }
}
