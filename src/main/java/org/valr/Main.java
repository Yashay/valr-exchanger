package org.valr;

import io.vertx.core.Vertx;
import org.valr.registry.AppRegistry;
import org.valr.verticles.MainVerticle;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Vertx vertx = AppRegistry.injector.getInstance(Vertx.class);
        vertx.deployVerticle(new MainVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("Deployment id is: " + res.result());
            } else {
                System.err.println("Deployment failed: " + res.cause());
            }
        });
    }
}