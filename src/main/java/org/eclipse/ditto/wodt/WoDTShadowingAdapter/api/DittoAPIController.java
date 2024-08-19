package org.eclipse.ditto.wodt.WoDTShadowingAdapter.api;

import org.eclipse.ditto.wodt.common.WebServerController;

import io.javalin.http.Context;
import io.javalin.websocket.WsConfig;

public interface DittoAPIController extends WebServerController {
    
    /*
     * Read observe attribute
     */
    void routeGetThingAttribute(Context context);
    
    void routeObserveThingAttribute(WsConfig wsContext);

    /*
     * Read observe feature property
     */
    void routeGetFeatureProperty(Context context);

    void routeObserveFeatureProperty(WsConfig wsContext);


    /*
     * Invoke action
     */
    void routeInvokeAction(Context context);



    // forse

    /*
     * Observe an event
     */
    void routeObserveThingEvents(WsConfig wsContext);

    /*
     * Read observe relationship
     */
    void routeGetThingRelationship(Context context);

    void routeObserveThingRelationship(WsConfig wsContext);

}