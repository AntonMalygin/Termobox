package com.example.termobox;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * Мост для передачи
 */
public final class RxBus {

    public RxBus() {}

    private PublishSubject<Object> bus = PublishSubject.create();

    /**
     * Отправить данные
     * @param o
     */
    public void send(Object o) {
        bus.onNext(o);
    }

    /**
     * Надлюдать за данными
     * @return
     */
    public Observable<Object> listen() {
        return bus;
    }
}