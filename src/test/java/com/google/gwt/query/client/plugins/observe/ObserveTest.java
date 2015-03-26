package com.google.gwt.query.client.plugins.observe;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.$$;
import static com.google.gwt.query.client.GQuery.console;
import static com.google.gwt.query.client.GQuery.document;

import java.util.List;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.Properties;
import com.google.gwt.query.client.js.JsUtils;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.ChangeRecord;
import com.google.gwt.query.client.plugins.observe.Observe.Changes.MutationRecord;
import com.google.gwt.query.client.plugins.observe.Observe.MutationListener;
import com.google.gwt.query.client.plugins.observe.Observe.ObserveListener;
import com.google.gwt.user.client.Timer;

/**
 * Test class for Observe plugin
 *
 * @author Manolo Carrasco
 */
public class ObserveTest extends GWTTestCase {

  public String getModuleName() {
    return "com.google.gwt.query.Observe";
  }

  /**
   * To run this test you should use a real browser with MutationObserver support.
   * Normally you can add this system property to your test launcher:
   * -Dgwt.args="-prod -runStyle Manual:1"
   */

  int mutationStatus = 0;
  @DoNotRunWith({Platform.Devel, Platform.HtmlUnitUnknown})
  public void testObserveMutation() {
    delayTestFinish(500);

    final GQuery g =  $("<div>foo</div>").appendTo(document);
    g.as(Observe.Observe).mutation(Observe.createMutationInit().attributes(true), new MutationListener() {
      public void onMutation(List<MutationRecord> mutations) {
        assertEquals("attributes", mutations.get(0).type());
        assertEquals("foo", mutations.get(0).attributeName());
        assertNull(mutations.get(0).oldValue());
        mutationStatus += mutations.size();
        g.as(Observe.Observe).disconnect();
        g.attr("Foo", "bar");
      }
    });

    g.attr("foo", "bar");
    new Timer() {
      public void run() {
        finishTest();
      }
    }.schedule(200);
  }

  int lastObjectStatus, objectStatus;
  @DoNotRunWith({Platform.Devel, Platform.HtmlUnitUnknown})
  public void testObserveObject() {
    delayTestFinish(500);

    final Properties j = $$();
    Observe.observe(j, Observe.createObserveInit().add(true).delete(true).update(true), new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        objectStatus += changes.size();
        for(ChangeRecord r : changes) {
          console.log("O -> " + r.toJson());
        }
      }
    });

    j.set("foo", "bar1");
    j.set("bar", "foo1");
    new Timer(){
      public void run() {
        j.set("foo", "bar2");
        j.remove("foo");
      }
    }.schedule(100);

    new Timer(){
      public void run() {
        assertTrue(objectStatus >= 3);
        lastObjectStatus = objectStatus;
        Observe.unobserve(j);
        j.set("bar", "foo2");
      }
    }.schedule(200);

    new Timer() {
      public void run() {
        assertEquals(objectStatus, lastObjectStatus);
        finishTest();
      }
    }.schedule(400);
  }

  int lastArrayStatus, arrayStatus;
  @DoNotRunWith({Platform.Devel, Platform.HtmlUnitUnknown})
  public void testObserveArray() {
    delayTestFinish(500);

    final JsArrayString a = JsArray.createArray().cast();

    Observe.observe(a, new ObserveListener() {
      public void onChange(List<ChangeRecord> changes) {
        arrayStatus += changes.size();
        for(ChangeRecord r : changes) {
          console.log("A -> " + r.toJson());
        }
      }
    });

    a.push("Hi");
    new Timer(){
      public void run() {
        assertTrue(arrayStatus == 1 || arrayStatus == 2);
        JsUtils.jsni(a,"splice", 0, 1);
      }
    }.schedule(100);

    new Timer(){
      public void run() {
        assertTrue(arrayStatus == 2 || arrayStatus == 4);
        lastArrayStatus = arrayStatus;
        Observe.unobserve(a);
        a.push("Bye");
      }
    }.schedule(200);

    new Timer() {
      public void run() {
        assertEquals(lastArrayStatus, arrayStatus);
        finishTest();
      }
    }.schedule(400);
  }

}
