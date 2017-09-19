package me.angrybyte.dagger2test.business;

import android.util.Log;

import java.io.File;

import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

public class WebClientDebug implements WebClient {

    private static final String TAG = WebClientDebug.class.getSimpleName();

    private final File mCacheFile;

    public WebClientDebug(final File cacheFile) {
        mCacheFile = cacheFile;
        Log.d(TAG, "Constructed " + TAG);
    }

    @Override
    public boolean doBlockingRequest() {
        Log.d(TAG, "DEBUG! Cache file blocking " + mCacheFile.toURI().toASCIIString());
        final long start = System.currentTimeMillis();
        // noinspection StatementWithEmptyBody
        do {} while (System.currentTimeMillis() - start < 1000);
        return false;
    }

    @Override
    public Single<Boolean> doAsyncRequest() {
        return Single.fromCallable(() -> {
            Log.d(TAG, "DEBUG! Cache file async " + mCacheFile.toURI().toASCIIString());
            final long start = System.currentTimeMillis();
            // noinspection StatementWithEmptyBody
            do {} while (System.currentTimeMillis() - start < 1000);
            return true;
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public String toString() {
        return TAG + " with cache in " + String.valueOf(mCacheFile);
    }

}
