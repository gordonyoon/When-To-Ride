package com.example.gordonyoon.whentoride;


import rx.Observable;
import rx.subjects.PublishSubject;

public class RxBus {

    private final PublishSubject<Object> mBus = PublishSubject.create();

    public void send(Object o) {
        mBus.onNext(o);
    }

    public Observable<Object> toObserverable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }
}
