package me.angrbyte.dagger2test.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import me.angrbyte.dagger2test.scopes.ActivityScope;

@Module
public class ContextModule {

    private final Context mContext;

    public ContextModule(Context context) {
        mContext = context.getApplicationContext();
    }

    @Provides
    @ActivityScope
    public Context getContext() {
        return mContext;
    }

}
