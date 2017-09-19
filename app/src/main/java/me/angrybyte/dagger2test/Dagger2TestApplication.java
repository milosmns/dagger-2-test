package me.angrybyte.dagger2test;

import android.app.Application;
import android.util.Log;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import me.angrybyte.dagger2test.components.DaggerQuickComponent;
import me.angrybyte.dagger2test.components.DaggerSlowComponent;
import me.angrybyte.dagger2test.components.QuickComponent;
import me.angrybyte.dagger2test.components.SlowComponent;
import me.angrybyte.dagger2test.modules.ContextModule;

public class Dagger2TestApplication extends Application {

    private static final String TAG = Dagger2TestApplication.class.getSimpleName();

    private Observable<SlowComponent> mSlowComponent;
    private Observable<QuickComponent> mQuickComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        // behavior subject saves the last value inside (great for singleton access)
        BehaviorSubject<SlowComponent> slowCreator = BehaviorSubject.create();
        mSlowComponent = slowCreator;
        BehaviorSubject<QuickComponent> quickCreator = BehaviorSubject.create();
        mQuickComponent = quickCreator;

        // async singleton loading
        Completable.create(emitter -> {
            Log.i(TAG, "onCreate: Creating new SlowComponent instance...");

            // prepare an artificial blocking delay
            final long delay = 2000L + Math.round(Math.random() * 4000d);
            final long start = System.currentTimeMillis();

            // noinspection StatementWithEmptyBody - just actively wait here
            do {
            } while (System.currentTimeMillis() - start < delay);
            Log.i(TAG, "onCreate: Delayed SlowComponent creation by: " + ((float) (System.currentTimeMillis() - start) / 1000f) + "s");

            // create and notify
            final SlowComponent instance = buildSlowComponentGraph();
            Log.i(TAG, "onCreate: Instance '" + instance.toString().replace(getPackageName() + ".", "") + "' created!");

            // finally produce the event to all observers
            slowCreator.onNext(instance);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).subscribe();

        Completable.create(emitter -> {
            Log.i(TAG, "onCreate: Creating new QuickComponent instance...");
            // quickly create and notify
            final QuickComponent instance = buildQuickComponentGraph();
            Log.i(TAG, "onCreate: Instance '" + instance.toString().replace(getPackageName() + ".", "") + "' created!");

            // finally produce the event to all observers
            quickCreator.onNext(instance);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).subscribe();
    }

    // <editor-fold desc="Graph builders">

    /**
     * Builds the dependency graph for the {@link SlowComponent}. Note that this is <b>blocking</b>.
     *
     * @return The {@link SlowComponent} instance
     */
    private SlowComponent buildSlowComponentGraph() {
        return DaggerSlowComponent.builder().contextModule(new ContextModule(this)).build();
    }

    /**
     * Builds the dependency graph for the {@link QuickComponent}. Note that this is <b>blocking</b>.
     *
     * @return The {@link QuickComponent} instance
     */
    private QuickComponent buildQuickComponentGraph() {
        return DaggerQuickComponent.builder().contextModule(new ContextModule(this)).build();
    }
    // </editor-fold>

    // <editor-fold desc="Getters">

    /**
     * A public API to the singleton slow component initializer.
     *
     * @return The {@link Observable} that will eventually produce a {@link SlowComponent}
     */
    public Observable<SlowComponent> getSlowComponentObservable() {
        return mSlowComponent;
    }

    /**
     * A public API to the singleton quick component initializer.
     *
     * @return The {@link Observable} that will eventually produce a {@link QuickComponent}
     */
    public Observable<QuickComponent> getQuickComponentObservable() {
        return mQuickComponent;
    }
    // </editor-fold>

}
