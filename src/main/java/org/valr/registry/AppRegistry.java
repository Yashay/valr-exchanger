package org.valr.registry;

import com.google.inject.Guice;
import com.google.inject.Injector;

public class AppRegistry {
    // not final for testing purposes
    public static Injector injector = Guice.createInjector(new AppModule());

}
