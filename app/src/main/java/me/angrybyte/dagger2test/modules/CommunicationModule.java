package me.angrybyte.dagger2test.modules;

import java.io.File;

import dagger.Module;
import dagger.Provides;
import me.angrybyte.dagger2test.business.WebClient;
import me.angrybyte.dagger2test.business.WebClientDebug;
import me.angrybyte.dagger2test.business.WebClientProduction;
import me.angrybyte.dagger2test.qualifiers.Debug;
import me.angrybyte.dagger2test.qualifiers.Production;

@SuppressWarnings("WeakerAccess")
@Module(includes = CacheModule.class)
public class CommunicationModule {

    @Provides
    @Debug public WebClient getDebugWebClient(File cacheFile) { return new WebClientDebug(cacheFile); }

    @Provides
    @Production public WebClient getProductionWebClient(File cacheFile) { return new WebClientProduction(cacheFile); }

}
