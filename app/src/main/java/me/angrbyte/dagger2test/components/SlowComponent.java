package me.angrbyte.dagger2test.components;

import dagger.Component;
import me.angrbyte.dagger2test.business.WebClient;
import me.angrbyte.dagger2test.modules.NetworkingModule;
import me.angrbyte.dagger2test.qualifiers.Debug;
import me.angrbyte.dagger2test.scopes.ActivityScope;

@ActivityScope
@Component(modules = NetworkingModule.class)
public interface SlowComponent {

    @Debug
    WebClient getWebClient();

}
