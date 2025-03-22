package org.valr.registry;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.Vertx;

public class AppRegistry {
    public static final Injector injector = Guice.createInjector(new AppModule());

    public static Vertx getVertxInstance() {
        return injector.getInstance(Vertx.class);
    }

}
