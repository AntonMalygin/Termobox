package com.example.termobox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SecondActivity extends AppCompatActivity {

    private Disposable disposable;
private Object mmC;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_second);

        listenEvent();
    }

    private void listenEvent() {
        disposable = ((MainApp) getApplication()).getRxBus()
                .listen()
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Object>() {
                    @SuppressLint("CheckResult")
                    @Override
                    public void accept(final Object o) {
                        if (o instanceof SimpleEvent) {
                           //runOnUiThread(new Runnable() {
                          //     @Override
                         //      public void run() {

                         //          Toasty.info(SecondActivity.this,"Данные :" + ((SimpleEvent) o).getCount(),Toasty.LENGTH_LONG).show();

                       // }

                          // });
                        mmC=((SimpleEvent) o) .getCount();

                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }



    public static void start(Context context) {
        Intent starter = new Intent(context, SecondActivity.class);
        context.startActivity(starter);
    }



}
