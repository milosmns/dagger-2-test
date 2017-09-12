package me.angrbyte.dagger2test.business;

import io.reactivex.Single;

public interface WebClient {

    boolean doBlockingRequest();

    Single<Boolean> doAsyncRequest();

}
