package com.google.gwt.query.client.plugins.observe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQ;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.builders.JsonBuilder;
import com.google.gwt.query.client.builders.Name;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.query.client.plugins.Events;
import com.google.gwt.query.client.plugins.Plugin;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.ChangeRecord;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.MutationRecord;
import com.google.gwt.user.client.Window;

/**
 * Observe for changes in Element, Object, Array.
 *
 * For mutation changes it always relays in native MutationObserver.
 *
 * For Object and Array changes it use native implementation if available
 * otherwise the plugin integrates the object-observer polyfill:
 * https://github.com/MaxArt2501/object-observe
 *
 * @author Manolo Carrasco
 */
public class Observe extends Events {

  private static final HashMap<Object, JavaScriptObject> map = new HashMap<Object, JavaScriptObject>();
  private static final HashMap<Object, List<Object>> observers = new HashMap<Object, List<Object>>();

  public static final Class<Observe> Observe = registerPlugin(Observe.class, new Plugin<Observe>() {
    public Observe init(GQuery gq) {
      return new Observe(gq);
    }
  });

  public interface ObjectObserverInit extends JsonBuilder {
    ObjectObserverInit add(boolean b);
    ObjectObserverInit update(boolean b);
    ObjectObserverInit delete(boolean b);
    ObjectObserverInit reconfigure(boolean b);
    ObjectObserverInit setPrototype(boolean b);
    ObjectObserverInit preventExtensions(boolean b);
    ObjectObserverInit splice(boolean b);
    boolean add();
    boolean update();
    boolean delete();
    boolean reconfigure();
    boolean setPrototype();
    boolean preventExtensions();
    boolean splice();
  }

  public interface MutationObserverInit extends JsonBuilder {
    /**
     * Set to true if additions and removals of the target node's child elements (including text
     * nodes) are to be observed.
     */
    MutationObserverInit childList(boolean b);

    boolean childList();

    /**
     * Set to true if mutations to target's attributes are to be observed.
     */
    MutationObserverInit attributes(boolean b);

    boolean attributes();

    /**
     * Set to true if mutations to target's data are to be observed.
     */
    MutationObserverInit characterData(boolean b);

    boolean characterData();

    /**
     * Set to true if mutations to not just target, but also target's descendants are to be
     * observed.
     */
    MutationObserverInit subtree(boolean b);

    boolean subtree();

    /**
     * Set to true if attributes is set to true and target's attribute value before the mutation
     * needs to be recorded.
     */
    MutationObserverInit attributeOldValue(boolean b);

    boolean attributeOldValue();

    /**
     * Set to true if characterData is set to true and target's data before the mutation needs to be
     * recorded.
     */
    MutationObserverInit characterDataOldValue(boolean b);

    boolean characterDataOldValue();

    /**
     * Set to an array of attribute local names (without namespace) if not all attribute mutations
     * need to be observed.
     */
    MutationObserverInit attributeFilter(String... l);

    List<String> attributeFilter();
  }

  public interface Changes extends JsonBuilder {

    /**
     * Object representing a change in an Object or Array.
     */
    public interface ChangeRecord extends JsonBuilder {
      /**
       * Returns the name of the property changed
       */
      String name();

      /**
       * Returns the object changed.
       */
      JavaScriptObject object();

      /**
       * Returns the change type.
       */
      String type();

      /**
       * Returns the old value of the property.
       */
      Object oldValue();
    }

    /**
     * Object representing mutation in an Element.
     */
    public interface MutationRecord extends JsonBuilder {
      /**
       * Returns attributes if the mutation was an attribute mutation, characterData if it was a
       * mutation to a CharacterData node, and childList if it was a mutation to the tree of nodes.
       */
      String type();

      /**
       * Returns the node the mutation affected, depending on the type. For attributes, it is the
       * element whose attribute changed. For characterData, it is the CharacterData node. For
       * childList, it is the node whose children changed.
       */
      Node target();

      /**
       * Return the nodes added. Will be an empty NodeList if no nodes were added.
       */
      NodeList<Node> addedNodes();

      /**
       * Return the nodes removed. Will be an empty NodeList if no nodes were removed.
       */
      NodeList<Node> removedNodes();

      /**
       * Return the previous sibling of the added or removed nodes, or null.
       */
      Node previousSibling();

      /**
       * Return the next sibling of the added or removed nodes, or null.
       */
      Node nextSibling();

      /**
       * Returns the local name of the changed attribute, or null.
       */
      String attributeName();

      /**
       * Returns the namespace of the changed attribute, or null.
       */
      String attributeNameSpace();

      /**
       * The return value depends on the type. For attributes, it is the value of the changed
       * attribute before the change. For characterData, it is the data of the changed node before
       * the change. For childList, it is null.
       */
      String oldValue();
    }

    List<ChangeRecord> changes();

    @Name("changes")
    List<MutationRecord> mutations();
  }


  private static JsArrayString config2JsList(Properties p) {
    JsArrayString ret = JsArrayString.createArray().cast();
    for (String s : p.keys()) {
      if (p.getBoolean(s)) {
        ret.push(s);
      }
    }
    return ret;
  }

  private static JsArrayString string2JsList(String p) {
    JsArrayString ret = JsArrayString.createArray().cast();
    for (String s : p.split("[, ]+")) {
      ret.push(s);
    }
    return ret;
  }

  public interface MutationListener {
    void onMutation(List<MutationRecord> mutations);
  }

  public interface ObserveListener {
    void onChange(List<ChangeRecord> changes);
  }

  public Observe(GQuery gq) {
    super(gq);
  }

  public static MutationObserverInit createMutationInit() {
    return GQ.create(MutationObserverInit.class);
  }

  public static ObjectObserverInit createObserveInit() {
    return GQ.create(ObjectObserverInit.class);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * cfg accepts a set of properties separated by comma, valid values are: attributes,
   * characterData, childList, subtree.
   */
  public Observe mutation(String cfg, final MutationListener l) {
    return mutation(parseCfg(cfg), l);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * config: accepts a set of properties separated by comma, valid values are: attributes,
   * characterData, childList, subtree.
   *
   * func: receives a list of MutationListener as first argument
   */
  public Observe mutation(String config, Function func) {
    return mutation(parseCfg(config), func);
  }

  public Observe mutation(MutationObserverInit init, MutationListener f) {
    return mutation(init.<Properties> getDataImpl(), f);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * func: receives a list of MutationListener as first argument
   */
  public Observe mutation(MutationObserverInit init, Function f) {
    return mutation(init.<Properties> getDataImpl(), f);
  }

  private Observe mutation(Properties p, Object handler) {
    if (JsUtils.hasProperty(window, "MutationObserver")) {
      for (Element e : elements()) {
        registerHandler(e, handler, p);
      }
    } else {
      console.log("ERROR: this browser does not support MutationObserver: " + Window.Navigator.getUserAgent());
    }
    return this;
  }

  private Properties parseCfg(String cfg) {
    Properties p = Properties.create();
    for (String s : cfg.split("[\\s,]+")) {
      p.setBoolean(s, true);
    }
    return p;
  }

  /**
   * Remove all observers in the elements selected.
   */
  public Observe disconnect() {
    return disconnect(null);
  }

  public Observe disconnect(Object handler) {
    for (Element e : elements()) {
      unregisterHandler(e, handler);
    }
    return this;
  }

  public static void observe(JavaScriptObject o, Object handler) {
    observe(o, createObserveInit().add(true).delete(true).update(true).splice(true), handler);
  }

  public static void observe(JavaScriptObject o, ObjectObserverInit cfg, Object handler) {
    observe(o, config2JsList(cfg.<Properties>getDataImpl()), handler);
  }

  public static void observe(JavaScriptObject o, String cfg, Object handler) {
    observe(o, string2JsList(cfg), handler);
  }

  public static void observe(JavaScriptObject o, JsArrayString cfg, Object handler) {
    ObjectObserve.assureLoaded();
    registerHandler(o, handler, cfg);
  }

  public static void unobserve(JavaScriptObject o) {
    unobserve(o, null);
  }

  public static void unobserve(JavaScriptObject o, Object handler) {
    unregisterHandler(o, handler);
  }

  private static void onChange(Object handler, JsArray<JavaScriptObject> mutations) {
    Changes r = GQ.create(Changes.class, Properties.create().set("changes", mutations));
    if (handler instanceof Function) {
      ((Function) handler).f(r.changes());
    } else if (handler instanceof ObserveListener) {
      ((ObserveListener) handler).onChange(r.changes());
    } else if (handler instanceof MutationListener) {
      ((MutationListener) handler).onMutation(r.mutations());
    }
  }

  private static void registerHandler(JavaScriptObject o, Object handler, JavaScriptObject cfg) {
    List<Object> l = observers.get(o);
    if (l == null) {
      l = new ArrayList<Object>();
      observers.put(o,l);
    }
    if (!l.contains(handler)) {
      l.add(handler);
      boolean isElement = JsUtils.isElement(o);
      JavaScriptObject f = getJsHandler(handler, isElement);
      map.put(handler, f);
      if (isElement) {
        mutationObserveImpl((Node)o, cfg, f);
      } else {
        objectObserveImpl(o, cfg, f);
      }
      l.add(handler);
    }
  }

  private static void unregisterHandler(JavaScriptObject o, Object handler) {
    List<Object> l = observers.get(o);
    if (l != null) {
      if (handler == null) {
        for (Object h : l) {
          unregisterHandler(o, h);
        }
        return;
      }
      if (l.remove(handler)) {
        JavaScriptObject f = map.get(handler);
        if (f != null) {
          if (JsUtils.isElement(o)) {
            mutationDisconnectImpl((Node)o, f);
          } else {
            objectUnobserveImpl(o, f);
          }
          for (Iterator<List<Object>> i = observers.values().iterator(); i.hasNext();) {
            if (i.next().contains(handler)) {
              return;
            }
          }
          map.remove(handler);
        }
      }
    }
  }

  private static JavaScriptObject getJsHandler(Object handler, boolean mutation) {
    JavaScriptObject jsh = map.get(handler);
    if (jsh == null) {
      jsh = createJsHandler(handler, mutation);
      map.put(handler, jsh);
    }
    return jsh;
  }

  private static JavaScriptObject observerForObject(JavaScriptObject o) {
    return JsUtils.prop(window, JsUtils.isArray(o) ? "Array" : "Object");
  }

  private native static JavaScriptObject createJsHandler(Object hdl, boolean mutation) /*-{
    var f = function(changes) {
      @com.google.gwt.query.client.plugins.observe.Observe::onChange(*)(hdl, changes);
    }
    return mutation ? new $wnd.MutationObserver(f) : f;
  }-*/;

  private static void objectObserveImpl(JavaScriptObject o, JavaScriptObject cfg, JavaScriptObject hdl)  {
   JsUtils.jsni(observerForObject(o), "observe", o, hdl, cfg);
  }

  private static native void mutationDisconnectImpl(Node e, JavaScriptObject hdl) /*-{
    hdl.disconnect();
  }-*/;

  private static native void mutationObserveImpl(Node e, JavaScriptObject cfg, JavaScriptObject hdl)/*-{
    hdl.observe(e, cfg);
  }-*/;

  private static void objectUnobserveImpl(JavaScriptObject o, JavaScriptObject hdl)  {
    JsUtils.jsni(observerForObject(o), "unobserve", o, hdl);
  }
}
