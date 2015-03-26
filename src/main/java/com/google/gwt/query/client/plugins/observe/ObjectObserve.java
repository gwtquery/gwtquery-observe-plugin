package com.google.gwt.query.client.plugins.observe;

import static com.google.gwt.query.client.GQuery.console;
import static com.google.gwt.query.client.GQuery.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.query.client.builders.JsniBundle;
import com.google.gwt.query.client.js.JsUtils;

public class ObjectObserve  {
  
  public static class ObjectObserveNative  {
    void load() {
      if (!hasObjectObserve())
        console.log("gwtquery-observe-plugin: your browser does not support Object.observe natively consider configure your Module.gwt.xml to load a polyfill");
    }
  }
  
  public static class ObjectObservePolyfillFull extends ObjectObserveNative {
    interface Loader extends JsniBundle {
      @LibrarySource(
          value = "https://raw.githubusercontent.com/MaxArt2501/object-observe/master/dist/object-observe.js",
          prepend = "$wnd.observefull=true;\n(function(window, document, console){\nvar Object = window.Object; var Array = window.Array;\n")
      void load();
    }
    void load() {
      GWT.<ObjectObservePolyfillFull.Loader>create(ObjectObservePolyfillFull.Loader.class).load();
    }
  }
  
  public static class ObjectObservePolyfillLite extends ObjectObserveNative{
    interface Loader extends JsniBundle {
      @LibrarySource(
          value = "https://raw.githubusercontent.com/MaxArt2501/object-observe/master/dist/object-observe-lite.js",
          prepend = "$wnd.observelite=true;\n(function(window, document, console){\nvar Object = window.Object; var Array = window.Array;\n")
      void load();
    }
    void load() {
      GWT.<ObjectObservePolyfillLite.Loader>create(ObjectObservePolyfillLite.Loader.class).load();
    }
  }
  
  private static boolean initialized = false;
  private static boolean hasObjectObserve = false;
  private static boolean hasArrayObserve = false;
  
  private ObjectObserve() {
  }
  
  public static void assureLoaded() {
    if (!initialized) {
      if (!hasObjectObserve()) {
        GWT.<ObjectObserveNative>create(ObjectObserveNative.class).load();
      }
    }
  }
  
  public static boolean hasObjectObserve() {
    return hasObjectObserve || (hasObjectObserve = JsUtils.hasProperty(window, "Object.observe"));
  }

  public static boolean hasArrayObserve() {
    return hasArrayObserve || (hasArrayObserve = JsUtils.hasProperty(window, "Array.observe"));
  }
}

