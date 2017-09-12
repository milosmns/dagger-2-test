package me.angrbyte.dagger2test.business;

import android.util.Log;

import java.io.File;

import io.reactivex.Single;

public class WebClientProduction implements WebClient {

    private static final String TAG = WebClientProduction.class.getSimpleName();

    private final File mCacheFile;

    public WebClientProduction(final File cacheFile) {
        mCacheFile = cacheFile;
        Log.d(TAG, "Constructed " + toString());
    }

    @Override
    public boolean doBlockingRequest() {
        Log.d(TAG, "Cache file blocking " + mCacheFile.toURI().toASCIIString());
        return false;
    }

    @Override
    public Single<Boolean> doAsyncRequest() {
        return Single.fromCallable(() -> {
            Log.d(TAG, "Cache file async " + mCacheFile.toURI().toASCIIString());
            return true;
        });
    }

}
