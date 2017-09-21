package me.angrybyte.dagger2test.business;

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
        Log.d(TAG, "PRODUCTION! Cache file blocking " + mCacheFile.toURI().toASCIIString());
        final long start = System.currentTimeMillis();
        // noinspection StatementWithEmptyBody
        do {
        } while (System.currentTimeMillis() - start < 200);
        return false;
    }

    @Override
    public Single<Boolean> doAsyncRequest() {
        return Single.fromCallable(() -> {
            Log.d(TAG, "PRODUCTION! Cache file async " + mCacheFile.toURI().toASCIIString());
            final long start = System.currentTimeMillis();
            // noinspection StatementWithEmptyBody
            do {
            } while (System.currentTimeMillis() - start < 200);
            return true;
        });
    }

    @Override
    public String toString() {
        return TAG + " with cache in " + String.valueOf(mCacheFile);
    }

}
