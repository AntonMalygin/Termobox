package com.example.termobox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG ="Lifecycle";
    private Disposable disposable;
private Object mmC;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_second);
        Log.d(TAG,"SecondActivity onCreate");

        listenEvent();
ImageButton btn_back = (ImageButton) findViewById(R.id.btn_back);
btn_back.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent i=new Intent(getApplicationContext(),MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);

    }
});

    }






    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"SecondActivity onRestart");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG,"SecondActivity onStart");
    }
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"SecondActivity onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG,"SecondActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"SecondActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
        Log.d(TAG,"SecondActivity onDestroy");

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
                });}

    public static void start(Context context) {
        Intent starter = new Intent(context, SecondActivity.class);
        context.startActivity(starter);

    }


    @Override
    public void onClick(View v) {

    }
}
