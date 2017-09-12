package me.angrbyte.dagger2test;

import android.app.Application;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.angrbyte.dagger2test.components.DaggerSlowComponent;
import me.angrbyte.dagger2test.components.SlowComponent;
import me.angrbyte.dagger2test.modules.ContextModule;

public class Dagger2TestApplication extends Application {

    private static final String TAG = Dagger2TestApplication.class.getSimpleName();

    private Observable<SlowComponent> mComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // behavior leaves the last value inside (good for singleton access)
        BehaviorSubject<SlowComponent> creator = BehaviorSubject.create();
        mComponent = creator.subscribeOn(Schedulers.computation());

        // async component loading
        final Disposable loaderDisposable = Completable.create(e -> {
            // artificial non-blocking delay
            final long delay = 2000L + Math.round(Math.random() * 4000d);
            final long start = System.currentTimeMillis();
            do {} while (System.currentTimeMillis() - start < delay);

            // create and notify
            final SlowComponent instance = DaggerSlowComponent.builder().contextModule(new ContextModule(this)).build();
            creator.onNext(instance);
            e.onComplete();
        }).subscribeOn(Schedulers.computation()).subscribe();

        // diagnostics and unsubscription from the loader
        creator.subscribeOn(Schedulers.computation()).subscribe(new Observer<SlowComponent>() {
            @Override
            public void onSubscribe(@NonNull final Disposable d) {
                Log.d(TAG, "Creator::onSubscribe()");
            }

            @Override
            public void onNext(@NonNull final SlowComponent slowComponent) {
                Log.d(TAG, "Creator::onNext()");
                loaderDisposable.dispose();
            }

            @Override
            public void onError(@NonNull final Throwable e) {
                Log.d(TAG, "Creator::onError()");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Creator::onComplete()");
            }
        });
    }

    public Observable<SlowComponent> getSlowComponent() {
        return mComponent;
    }

}
