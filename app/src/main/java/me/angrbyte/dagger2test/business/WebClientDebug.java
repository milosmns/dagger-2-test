package me.angrbyte.dagger2test.business;

import android.util.Log;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class WebClientDebug implements WebClient {

    private static final String TAG = WebClientDebug.class.getSimpleName();

    private final File mCacheFile;

    public WebClientDebug(final File cacheFile) {
        mCacheFile = cacheFile;
        Log.d(TAG, "Constructed " + toString());
    }

    @Override
    public boolean doBlockingRequest() {
        Log.d(TAG, "DEBUG! Cache file blocking " + mCacheFile.toURI().toASCIIString());
        final long start = System.currentTimeMillis();
        do {} while (System.currentTimeMillis() - start < 1000);
        return false;
    }

    @Override
    public Single<Boolean> doAsyncRequest() {
        return Single.fromCallable(() -> {
            Log.d(TAG, "DEBUG! Cache file async " + mCacheFile.toURI().toASCIIString());
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {}
            return true;
        }).subscribeOn(Schedulers.io());
    }

}
