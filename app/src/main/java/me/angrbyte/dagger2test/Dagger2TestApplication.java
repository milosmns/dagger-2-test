package me.angrbyte.dagger2test;

import android.app.Application;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Observable;
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

        // behavior subject saves the last value inside (great for singleton access)
        BehaviorSubject<SlowComponent> creator = BehaviorSubject.create();
        mComponent = creator;

        // async singleton loading
        Completable.create(emitter -> {
            Log.i(TAG, "onCreate: Creating new " + TAG + " instance...");

            // prepare an artificial blocking delay
            final long delay = 2000L + Math.round(Math.random() * 4000d);
            final long start = System.currentTimeMillis();

            // noinspection StatementWithEmptyBody - just actively wait here
            do {} while (System.currentTimeMillis() - start < delay);
            Log.i(TAG, "onCreate: Delayed " + TAG + " creation by: " + ((float) (System.currentTimeMillis() - start) / 1000f) + "s");

            // create and notify
            final SlowComponent instance = buildSlowComponentGraph();
            Log.i(TAG, "onCreate: Instance '" + instance.toString().replace(getPackageName() + ".", "") + "' created!");

            // finally produce the event to all observers
            creator.onNext(instance);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    /**
     * Builds the dependency graph for the {@link SlowComponent}. Note that this is <b>blocking</b>.
     *
     * @return The {@link SlowComponent} instance
     */
    private SlowComponent buildSlowComponentGraph() {
        return DaggerSlowComponent.builder().contextModule(new ContextModule(this)).build();
    }

    /**
     * A public API to the singleton initializer.
     *
     * @return The {@link Observable} that will eventually produce a {@link SlowComponent}
     */
    public Observable<SlowComponent> getSingletonInitializer() {
        return mComponent;
    }

}
