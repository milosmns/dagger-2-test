package me.angrybyte.dagger2test;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.disposables.SerialDisposable;
import io.reactivex.schedulers.Schedulers;
import me.angrybyte.dagger2test.components.DaggerQuickComponent;
import me.angrybyte.dagger2test.components.DaggerSlowComponent;
import me.angrybyte.dagger2test.components.QuickComponent;
import me.angrybyte.dagger2test.components.SlowComponent;
import me.angrybyte.dagger2test.modules.AppContextModule;

public class Dagger2TestApplication extends Application {

    private static final String TAG = Dagger2TestApplication.class.getSimpleName();

    private SerialDisposable mAtomicDisposableSlow;
    private SerialDisposable mAtomicDisposableQuick;
    private Observable<SlowComponent> mSlowComponent;
    private Observable<QuickComponent> mQuickComponent;

    /**
     * Returns a {@link Dagger2TestApplication} instance from the given Context.
     *
     * @param context Which Context to use to fetch the application instance
     * @return The running {@link Application} instance
     */
    public static Dagger2TestApplication getInstance(@NonNull final Context context) {
        return Dagger2TestApplication.class.cast(context.getApplicationContext());
    }

    // <editor-fold desc="Dependency graph management">

    @Override
    public void onCreate() {
        super.onCreate();

        // SerialDisposable (wrapper around disposable) allows atomic disposable instance replacement
        mAtomicDisposableSlow = new SerialDisposable();
        mAtomicDisposableQuick = new SerialDisposable();

        // asynchronously initialize the slow component
        mSlowComponent = Observable
                .fromCallable(() -> {
                    Log.i(TAG, "Callable: Creating new SlowComponent instance...");

                    // prepare an artificial blocking delay
                    final long delay = 2000L + Math.round(Math.random() * 4000d);
                    final long start = System.currentTimeMillis();

                    // noinspection StatementWithEmptyBody - just actively wait here for a while
                    do {} while (System.currentTimeMillis() - start < delay);
                    Log.i(TAG, "Callable: Delayed SlowComponent creation by: " + ((float) (System.currentTimeMillis() - start) / 1000f) + "s");

                    // create instance and notify
                    final SlowComponent instance = buildSlowComponentGraph();
                    Log.i(TAG, "Callable: Instance '" + instance.toString().replace(getPackageName() + ".", "") + "' created!");
                    return instance;
                })
                .subscribeOn(Schedulers.io()) // slow running, run on I/O
                .replay(1) // re-emits the last value from the stream, if any (similar to behavior subject); also becomes connectible
                .autoConnect(0, mAtomicDisposableSlow::set); // wait for 0 subscribers, i.e. start now; also new disposable is produced, save it

        // asynchronously initialize the quick component
        mQuickComponent = Observable
                .fromCallable(() -> {
                    Log.i(TAG, "Callable: Creating new QuickComponent instance...");
                    // quickly create and notify
                    final QuickComponent instance = buildQuickComponentGraph();
                    Log.i(TAG, "Callable: Instance '" + instance.toString().replace(getPackageName() + ".", "") + "' created!");
                    return instance;
                })
                .subscribeOn(Schedulers.io())
                .replay(1)
                .autoConnect(0, mAtomicDisposableQuick::set);
    }

    /**
     * Typically called during app shut down to clean up the resources.
     * This will also dispose of the initializer callback disposables.
     */
    public void cleanup() {
        if (mAtomicDisposableSlow != null && !mAtomicDisposableSlow.isDisposed()) {
            mAtomicDisposableSlow.dispose();
            mAtomicDisposableSlow = null;
        }
        if (mAtomicDisposableQuick != null && !mAtomicDisposableQuick.isDisposed()) {
            mAtomicDisposableQuick.dispose();
            mAtomicDisposableQuick = null;
        }
    }

    /**
     * Builds the dependency graph for the {@link SlowComponent}. Note that this is <b>blocking</b>.
     *
     * @return The {@link SlowComponent} instance
     */
    private SlowComponent buildSlowComponentGraph() {
        return DaggerSlowComponent.builder().appContextModule(new AppContextModule(this)).build();
    }
    // </editor-fold>

    // <editor-fold desc="Getters">

    /**
     * Builds the dependency graph for the {@link QuickComponent}. Note that this is <b>blocking</b>.
     *
     * @return The {@link QuickComponent} instance
     */
    private QuickComponent buildQuickComponentGraph() {
        return DaggerQuickComponent.builder().appContextModule(new AppContextModule(this)).build();
    }

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
