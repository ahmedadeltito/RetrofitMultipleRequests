package com.ahmedadeltito.retrofitmultiplerequests.backgroundservice;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.ahmedadeltito.retrofitmultiplerequests.MainApplication;
import com.ahmedadeltito.retrofitmultiplerequests.internetconnectivity.ConnectivityReceiver;
import com.ahmedadeltito.retrofitmultiplerequests.model.Model;
import com.ahmedadeltito.retrofitmultiplerequests.service.ServiceFactory;
import com.ahmedadeltito.retrofitmultiplerequests.service.WeatherService;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Ahmed Adel on 9/7/16.
 */
public class DownloadWeatherService extends IntentService implements ConnectivityReceiver.ConnectivityReceiverListener {

    private static final String TAG = "DownloadWeatherService";
    public static final int STATUS_FINISHED = 1;

    private boolean isFirstBatchFinished, isSecondBatchFinished, isThirdBatchFinished;
    private int firstBatchIndex, secondBatchIndex, thirdBatchIndex;
    private ResultReceiver receiver;

    private List<String> firstBatch = new ArrayList<String>() {{
        add("Afghanistan");
        add("Albania");
        add("Algeria");
        add("Andorra");
        add("Angola");
        add("Argentina");
        add("Armenia");
        add("Australia");
        add("Austria");
        add("Azerbaijan");
    }};

    private List<String> secondBatch = new ArrayList<String>() {{
        add("Bahamas");
        add("Bahrain");
        add("Bangladesh");
        add("Barbados");
        add("Belarus");
        add("Belgium");
        add("Belize");
        add("Benin");
        add("Bhutan");
        add("Bolivia");
        add("Botswana");
        add("Brazil");
        add("Brunei");
        add("Bulgaria");
    }};

    private List<String> thirdBatch = new ArrayList<String>() {{
        add("Macedonia");
        add("Madagascar");
        add("Malawi");
        add("Malaysia");
        add("Maldives");
        add("Mali");
        add("Malta");
        add("Mauritania");
        add("Mauritius");
        add("Mexico");
        add("Micronesia");
        add("Moldova");
        add("Monaco");
        add("Mongolia");
        add("Montenegro");
        add("Morocco");
        add("Mozambique");
    }};

    private List<Model> modelList = new ArrayList<>();

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public DownloadWeatherService() {
        super(DownloadWeatherService.class.getName());
        MainApplication.getInstance().setConnectivityListener(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Service Started!");
        receiver = intent.getParcelableExtra("receiver");

        PackageManager pm = this.getPackageManager();
        ComponentName componentName = new ComponentName(this, ConnectivityReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

        callBatches(0, firstBatch, BatchType.firstBatch);
        callBatches(0, secondBatch, BatchType.secondBatch);
        callBatches(0, thirdBatch, BatchType.thirdBatch);

    }

    private void callBatches(int counter, final List<String> batchList, final BatchType batchType) {
        WeatherService service = ServiceFactory.createRetrofitService(WeatherService.class);
        service.getWeatherData(batchList.get(counter), "c6afdab60aa89481e297e0a4f19af055")
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Model>() {
                    @Override
                    public final void onCompleted() {
                        // do nothing
                    }

                    @Override
                    public final void onError(Throwable e) {
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public final void onNext(Model response) {

                        switch (batchType) {
                            case firstBatch:
                                firstBatchIndex++;
                                Log.d(TAG, "firstBatchIndex : " + firstBatchIndex);
                                Log.e(batchType.toString(), response.getName() + " ==> " + batchList.get(firstBatchIndex - 1));
                                modelList.add(response);
                                if (firstBatchIndex == (batchList.size())) {
                                    isFirstBatchFinished = true;
                                    closeService();
                                } else {
                                    callBatches(firstBatchIndex, batchList, batchType);
                                }
                                break;
                            case secondBatch:
                                secondBatchIndex++;
                                Log.d(TAG, "secondBatchIndex : " + secondBatchIndex);
                                Log.e(batchType.toString(), response.getName() + " ==> " + batchList.get(secondBatchIndex - 1));
                                modelList.add(response);
                                if (secondBatchIndex == (batchList.size())) {
                                    isSecondBatchFinished = true;
                                    closeService();
                                } else {
                                    callBatches(firstBatchIndex, batchList, batchType);
                                }
                                break;
                            case thirdBatch:
                                thirdBatchIndex++;
                                Log.d(TAG, "thirdBatchIndex : " + thirdBatchIndex);
                                Log.e(batchType.toString(), response.getName() + " ==> " + batchList.get(thirdBatchIndex - 1));
                                modelList.add(response);
                                if (thirdBatchIndex == (batchList.size())) {
                                    isThirdBatchFinished = true;
                                    closeService();
                                } else {
                                    callBatches(firstBatchIndex, batchList, batchType);
                                }
                                break;
                        }
                    }
                });
    }

    private void closeService() {
        if (isFirstBatchFinished && isSecondBatchFinished && isThirdBatchFinished) {
            stopSelf();
            Log.e(TAG, "service is closed!!");
            saveIntoRealmDatabase();
        }
    }

    private void saveIntoRealmDatabase() {
        Realm realm = ((MainApplication) getApplicationContext()).getRealm();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(modelList);
        realm.commitTransaction();
        receiver.send(STATUS_FINISHED, Bundle.EMPTY);

        PackageManager pm = this.getPackageManager();
        ComponentName componentName = new ComponentName(this, ConnectivityReceiver.class);
        pm.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        Log.i(TAG, "internet connection : " + isConnected);
        if (isConnected)
            if (firstBatchIndex != 0 && secondBatchIndex != 0 && thirdBatchIndex != 0) {
                callBatches(firstBatchIndex, firstBatch, BatchType.firstBatch);
                callBatches(secondBatchIndex, secondBatch, BatchType.secondBatch);
                callBatches(thirdBatchIndex, thirdBatch, BatchType.thirdBatch);
            }
    }

    enum BatchType {
        firstBatch("firstBatch"),
        secondBatch("secondBatch"),
        thirdBatch("thirdBatch");

        private final String type;

        BatchType(String type) {
            this.type = type;
        }

        public String toString() {
            return this.type;
        }
    }
}
