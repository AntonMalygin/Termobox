package com.example.termobox;

import android.app.Application;



public class MainApp extends Application {

    private RxBus bus;

    @Override
    public void onCreate() {
        super.onCreate();
        /* Создаем RxBus в единственном экзмепляре */
        bus = new RxBus();
    }

    public RxBus getRxBus() {
        return bus;
    }
}
