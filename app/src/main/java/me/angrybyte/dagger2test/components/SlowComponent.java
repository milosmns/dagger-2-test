package me.angrybyte.dagger2test.components;

import dagger.Component;
import me.angrybyte.dagger2test.business.WebClient;
import me.angrybyte.dagger2test.modules.WebClientModule;
import me.angrybyte.dagger2test.qualifiers.Debug;
import me.angrybyte.dagger2test.scopes.ApplicationScope;

@ApplicationScope
@Component(modules = WebClientModule.class)
public interface SlowComponent {

    @Debug
    WebClient getWebClient();

}
