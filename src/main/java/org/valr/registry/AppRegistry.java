package org.valr.registry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;

public class AppRegistry {
    // not final for testing purposes
    public static Injector injector = Guice.createInjector(new AppModule());

    public static Vertx getVertxInstance() {
        return injector.getInstance(Vertx.class);
    }

}
