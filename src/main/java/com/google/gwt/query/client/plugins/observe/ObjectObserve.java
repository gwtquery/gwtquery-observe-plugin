package com.google.gwt.query.client.plugins.observe;

import static com.google.gwt.query.client.GQuery.console;
import static com.google.gwt.query.client.GQuery.window;

import com.google.gwt.core.client.GWT;
import com.google.gwt.query.client.builders.JsniBundle;
import com.google.gwt.query.client.js.JsUtils;

public class ObjectObserve  {
  // private static final String FULLURL = "https://raw.githubusercontent.com/MaxArt2501/object-observe/master/dist/object-observe.js";
  private static final String FULLURL = "poly/object-observe.js";
  // private static final String LITEURL = "https://raw.githubusercontent.com/MaxArt2501/object-observe/master/dist/object-observe-lite.js";
  private static final String LITEURL = "poly/object-observe-lite.js";

  private static final String PREPEND = "$wnd.observelite=true;\n(function(window, document, console){\nvar Object = window.Object; var Array = window.Array;\n";
  private static final String POSTPEND = "\nArray.unobserve = Object.unobserve; Array.observe = Object.observe;\n}.apply($wnd, [$wnd, $doc, $wnd.console]));";

  public static class ObjectObserveNative  {
    void load() {
      if (!hasObjectObserve())
        console.log("gwtquery-observe-plugin: your browser does not support Object.observe natively consider configure your Module.gwt.xml to load a polyfill");
    }
  }

  public static class ObjectObservePolyfillFull extends ObjectObserveNative {
    interface Loader extends JsniBundle {
      @LibrarySource(value = ObjectObserve.FULLURL, prepend = ObjectObserve.PREPEND, postpend = ObjectObserve.POSTPEND)
      void load();
    }
    void load() {
      GWT.<ObjectObservePolyfillFull.Loader>create(ObjectObservePolyfillFull.Loader.class).load();
    }
  }

  public static class ObjectObservePolyfillLite extends ObjectObserveNative{
    interface Loader extends JsniBundle {
      @LibrarySource(value = ObjectObserve.LITEURL, prepend = ObjectObserve.PREPEND, postpend = ObjectObserve.POSTPEND)
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
        ObjectObserveNative n = GWT.<ObjectObserveNative>create(ObjectObserveNative.class);
        n.load();;
      }
      initialized = true;
    }
  }

  public static boolean hasObjectObserve() {
    return hasObjectObserve || (hasObjectObserve = JsUtils.hasProperty(window, "Object.observe"));
  }

  public static boolean hasArrayObserve() {
    return hasArrayObserve || (hasArrayObserve = JsUtils.hasProperty(window, "Array.observe"));
  }
}

