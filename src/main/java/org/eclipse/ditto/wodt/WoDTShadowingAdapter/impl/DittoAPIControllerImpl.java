package org.eclipse.ditto.wodt.WoDTShadowingAdapter.impl;

import org.eclipse.ditto.wodt.WoDTShadowingAdapter.api.DittoAPIController;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Header;
import io.javalin.http.HttpStatus;
import io.javalin.websocket.WsConfig;

public class DittoAPIControllerImpl implements DittoAPIController {    
    //private final DittoBase client;

    // TO DO modifica

    public DittoAPIControllerImpl() {
        //this.client = new DittoBase();
    }

    @Override
    public void routeGetThingAttribute(Context context) {
        context.status(HttpStatus.OK);
        context.header(Header.CONTENT_TYPE, "application/td+json");
        context.result("Ciao");
    }

    @Override
    public void routeObserveThingAttribute(WsConfig wsContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeGetFeatureProperty(Context context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeObserveFeatureProperty(WsConfig wsContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeInvokeAction(Context context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeObserveThingEvents(WsConfig wsContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeGetThingRelationship(Context context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void routeObserveThingRelationship(WsConfig wsContext) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void registerRoutes(Javalin app) {
        app.get("/things/prova", this::routeGetThingAttribute);
    }
    
}