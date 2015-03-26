package com.google.gwt.query.client.plugins.observe;

import static com.google.gwt.query.client.GQuery.$;
import static com.google.gwt.query.client.GQuery.document;

import java.util.List;

import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.query.client.GQuery;
import com.google.gwt.query.client.plugins.observe.Observe.MutationListener;
import com.google.gwt.query.client.plugins.observe.Observe.MutationRecords.MutationRecord;
import com.google.gwt.user.client.Timer;

/**
 * Test class for Observe plugin
 *
 * @author Manolo Carrasco
 */
public class ObserveTest extends GWTTestCase {

  public String getModuleName() {
    return "com.google.gwt.query.Query";
  }

  /**
   * To run this test you should use a real browser with MutationObserver support.
   * Normally you can add this system property to your test launcher:
   * -Dgwt.args="-prod -runStyle Manual:1"
   */
  int status = 0;
  @DoNotRunWith({Platform.Devel, Platform.HtmlUnitUnknown})
  public void testObserveApply() {

    final GQuery g =  $("<div>foo</div>").appendTo(document);

    g.as(Observe.Observe).mutation(Observe.createMutationInit().attributes(true), new MutationListener() {
      public void onMutation(List<MutationRecord> mutations) {
        assertEquals("attributes", mutations.get(0).type());
        assertEquals("foo", mutations.get(0).attributeName());
        assertNull(mutations.get(0).oldValue());
        status++;
        g.as(Observe.Observe).disconnect();
        g.attr("Foo", "bar");
      }
    });

    delayTestFinish(100);
    g.attr("Foo", "bar");

    new Timer() {
      public void run() {
        assertEquals(1, status);
        finishTest();
      }
    }.schedule(50);
  }
}
