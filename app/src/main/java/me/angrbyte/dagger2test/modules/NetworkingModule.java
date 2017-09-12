package me.angrbyte.dagger2test.modules;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import me.angrbyte.dagger2test.business.WebClient;
import me.angrbyte.dagger2test.business.WebClientDebug;
import me.angrbyte.dagger2test.business.WebClientProduction;
import me.angrbyte.dagger2test.qualifiers.Debug;
import me.angrbyte.dagger2test.qualifiers.Production;

@Module(includes = CacheModule.class)
public class NetworkingModule {

    @Debug
    @Provides
    public WebClient getDebugWebClient(File cacheFile) {
        return new WebClientDebug(cacheFile);
    }

    @Production
    @Provides
    public WebClient getProductionWebClient(File cacheFile) {
        return new WebClientProduction(cacheFile);
    }

}
