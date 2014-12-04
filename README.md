

## Introduction
A gwtQuery plugin which simplifies the use of the new DOM Mutation Observer interface introduced in newer browsers.

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
2. Then use it as any other gQuery plugin through the `as()` method
   ```
   // Observe to attribute changes
   $(selector)
     .as(Observe.Observe)
     .observe(Observe.createInit()
                     .attributes(true)
                     .attributeOldValue(true),
       new MutationListener() {
         public void onMutation(List<MutationRecord> mutations) {
           console.log(mutations.get(0).type());
         }
     });

   // Disconnect
   $(selector).as(Observe.Observe).disconnect();
   ```
3. gQuery Observe plugin supports all the options defined in [MutationObserver](https://developer.mozilla.org/en-US/docs/Web/API/MutationObserver) specification.
   Use the `MutationObserverInit` interface for setting configuration properties, and the `MutationRecord` to read mutation changes.


## Browser compatibility

   Chrome 18, FF 14, IE 11, Opera 15, Safari 6
