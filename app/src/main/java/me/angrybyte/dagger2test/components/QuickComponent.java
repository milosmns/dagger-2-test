package me.angrybyte.dagger2test.components;

import dagger.Component;
import me.angrybyte.dagger2test.business.WebClient;
import me.angrybyte.dagger2test.modules.CommunicationModule;
import me.angrybyte.dagger2test.qualifiers.Production;
import me.angrybyte.dagger2test.scopes.ApplicationScope;

@ApplicationScope
@Component(modules = CommunicationModule.class)
public interface QuickComponent {

    @Production
    WebClient getWebClient();

}
