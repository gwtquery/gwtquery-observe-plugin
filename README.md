

## Introduction
A gwtQuery plugin which simplifies the use of the new DOM-Mutation, Object and Array Observe interfaces introduced in newer browsers.

For mutation changes it always relays in native MutationObserver, for Object and Array changes it use native implementation if available
otherwise the plugin automatically loads the [object-observer](https://github.com/MaxArt2501/object-observe) polyfill.

## Demo

http://manolo.github.io/gwtquery-observe-demo/index.html

## Usage

1. You only have to drop the .jar file in your classpath, or add this dependency to your project:

   ```
        <dependency>
            <groupId>com.googlecode.gwtquery.plugins</groupId>
            <artifactId>observe-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <scope>provided</scope>
        </dependency>
   ```

2. By default, the plugin does not load polyfills, if you want to use the pluggin in non-chrome browsers you have to add this line to your module file:
   ```
    <inherits name='com.google.gwt.query.Observe'/>
   ```

3. To observe Element mutations, use it as any other gQuery plugin through the `as()` method
   ```
   // Observe attribute changes in all elements matching the selector
   $(selector)
     .as(Observe.Observe)
     .mutation(Observe.createMutationInit()
                      .attributes(true)
                      .attributeOldValue(true),
       new MutationListener() {
         public void onMutation(List<MutationRecord> mutations) {
           console.log(mutations.get(0).type());
         }
     });

   // Observe changes in a GWT widget
   $(myWidget)
     .as(Observe.Observe)
     .mutation(
        // You can use either MutationInit or a list of strings
        "childList, subtree",
        // You can use either MutationListener interface or Function
        new Function() {
          public void f() {
              List<MutationRecord> mutations = arguments(0);
          }
     });

   // Disconnect
   $(selector).as(Observe.Observe).disconnect();
   ```

4. For arrays and objects, you have a set of static methods:
   ```
   // Observe properties changes in an object
   Observe.observe(myObject,
       // You can use either ObserveInit or a list of strings
       Observe.createObserveInit().add(true).delete(true).update(true),
       // You can use either ObserveListener interface or Function
       new ObserveListener() {
         public void onChange(List<ChangeRecord> changes) {
           console.log(changes.get(0).type());
         }
     });

   // Unobserve
   Observe.unobserve(myObject);
   ```

5. gQuery Observe plugin supports all the options defined in [MutationObserver](https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver) specification. Use the `MutationObserverInit` interface for setting configuration properties, and the `MutationRecord` to read mutations.

6. To observe javascript Object or Array, use the `ObjectObserverInit` for setting configuration properties, and the `ChangeRecord` to read changes.


## Browser compatibility

   Mutations: Chrome 18, FF 14, IE 11, Opera 15, Safari 6
   Object & Array Observe: Chrome in native mode and FF, IE, Safari through polyfill.

