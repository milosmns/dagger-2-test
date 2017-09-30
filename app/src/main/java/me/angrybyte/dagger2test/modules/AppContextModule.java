package me.angrybyte.dagger2test.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import me.angrybyte.dagger2test.scopes.ApplicationScope;

@Module
public class AppContextModule {

    private final Context mAppContext;

    public AppContextModule(Context context) {
        mAppContext = context.getApplicationContext();
    }

    @Provides
    @ApplicationScope
    public Context getAppContext() {
        return mAppContext;
    }

}
