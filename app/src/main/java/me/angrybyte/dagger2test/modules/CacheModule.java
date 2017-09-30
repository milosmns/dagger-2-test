package me.angrybyte.dagger2test.modules;

import android.content.Context;

import java.io.File;
import java.util.Random;

import dagger.Module;
import dagger.Provides;
import me.angrybyte.dagger2test.scopes.ApplicationScope;

@Module(includes = AppContextModule.class)
public class CacheModule {

    @Provides
    @ApplicationScope
    public File provideCacheFile(Context context) {
        String fileCode = Integer.toHexString(Math.abs(new Random().nextInt()));

        // this is just for demo purposes
        if (context.getCacheDir() != null) {
            return new File(context.getCacheDir() + "/temp/my_data_" + fileCode + ".txt");
        } else {
            return new File(context.getExternalCacheDir(), "EXT_" + fileCode + ".txt");
        }
    }

}
