package com.example.termobox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import es.dmoral.toasty.Toasty;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SecondActivity extends AppCompatActivity {

    private Disposable disposable;



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
                    public void accept(Object o) {
                        if (o instanceof SimpleEvent) {
                            Toasty.info(SecondActivity.this,String.valueOf(((SimpleEvent) o).getCount()),Toasty.LENGTH_LONG).show();

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
