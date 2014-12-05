package com.google.gwt.query.client.plugin;

import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.query.client.Function;
import com.google.gwt.query.client.GQ;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.builders.JsonBuilder;
import com.google.gwt.query.client.plugin.Observe.MutationRecords.MutationRecord;
import com.google.gwt.query.client.plugins.Events;
import com.google.gwt.query.client.plugins.Plugin;

/**
 * Observe for changes in the elements.
 *
 * @author Manolo Carrasco
 */
public class Observe extends Events {
  public static final Class<Observe> Observe = registerPlugin(Observe.class, new Plugin<Observe>() {
    public Observe init(GQuery gq) {
      return new Observe(gq);
    }
  });

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

  public interface MutationRecords extends JsonBuilder {
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

    List<MutationRecord> mutations();
  }

  public interface MutationListener {
    void onMutation(List<MutationRecord> mutations);
  }

  public Observe(GQuery gq) {
    super(gq);
  }

  public static MutationObserverInit createInit() {
    return GQ.create(MutationObserverInit.class);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * cfg accepts a set of properties separated by comma, valid values are: attributes,
   * characterData, childList, subtree.
   */
  public Observe observe(String cfg, final MutationListener l) {
    return observe(parseCfg(cfg), l);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * config: accepts a set of properties separated by comma, valid values are: attributes,
   * characterData, childList, subtree.
   *
   * func: receives a list of MutationListener as first argument
   */
  public Observe observe(String config, Function func) {
    return observe(parseCfg(config), func);
  }

  public Observe observe(MutationObserverInit init, MutationListener f) {
    return observe(init.<Properties> getDataImpl(), f);
  }

  /**
   * Observe to element changes based on the configuration set.
   *
   * func: receives a list of MutationListener as first argument
   */
  public Observe observe(MutationObserverInit init, Function f) {
    return observe(init.<Properties> getDataImpl(), f);
  }

  private Observe observe(Properties p, Object handler) {
    for (Element e : elements()) {
      observeImpl(e, p, handler);
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
    for (Element e : elements()) {
      disconnectImpl(e);
    }
    return this;
  }

  private static native void disconnectImpl(Node e) /*-{
    for (i in e.__gq_obs) {
      e.__gq_obs[i].disconnect();
    }
    delete e.__gq_obs;
  }-*/;

  private static native void observeImpl(Node e, JavaScriptObject cfg, Object handler)/*-{
    if ($wnd.MutationObserver) {
      var observer = new $wnd.MutationObserver(function(mutations) {
        @com.google.gwt.query.client.plugin.Observe::onMutation(*)(handler, mutations);
      });
      (e.__gq_obs || (e.__gq_obs = [])).push(observer);
      observer.observe(e, cfg);
    } else {
      $wnd.console.log("ERROR: this browser does not support MutationObserver: "
          + $wnd.navigator.userAgent);
    }
  }-*/;

  private static void onMutation(Object handler, JsArray<JavaScriptObject> mutations) {
    MutationRecords r = GQ.create(MutationRecords.class, Properties.create().set("mutations", mutations));
    if (handler instanceof Function) {
      ((Function) handler).f(r.mutations());
    } else if (handler instanceof MutationListener) {
      ((MutationListener) handler).onMutation(r.mutations());
    }
  }
}
