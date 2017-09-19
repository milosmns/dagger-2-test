package me.angrybyte.dagger2test.modules;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import me.angrybyte.dagger2test.scopes.ApplicationScope;

@SuppressWarnings("WeakerAccess")
@Module
public class ContextModule {

    private final Context mContext;

    public ContextModule(Context context) {
        mContext = context.getApplicationContext();
    }

    @Provides
    @ApplicationScope
    public Context getContext() {
        return mContext;
    }

}
