package me.angrbyte.dagger2test.modules;

import android.content.Context;

import java.io.File;
import java.util.Random;

import dagger.Module;
import dagger.Provides;
import me.angrbyte.dagger2test.scopes.ActivityScope;

@Module(includes = ContextModule.class)
public class CacheModule {

    @Provides
    @ActivityScope
    public File provideCacheFile(Context context) {
        return new File(context.getExternalCacheDir(), "temp/my_data_" + new Random().nextInt() + ".txt");
    }

}
