# EVENTBUS
<img src="https://tokei.rs/b1/github/therealbush/eventbus" alt="GitHub lines of code"/> <img src="https://img.shields.io/github/languages/code-size/therealbush/eventbus" alt="GitHub code size"/> [![](https://jitpack.io/v/therealbush/eventbus.svg)](https://jitpack.io/#therealbush/eventbus)<br>
*An extremely fast, flexible, lightweight, and simple event system aimed towards Minecraft utility mods.*

## Usage

### Adding to your project:
If you have not already, add Jitpack as a repository:
```groovy
repositories {
    maven { url 'https://jitpack.io' }
}
```
Add the release of your choice in the dependencies block:
```groovy
dependencies {
    implementation 'com.github.bush-did-711:eventbus:1.0.2'
}
```

### Creating an EventBus:
When creating a new EventBus, there are 3 different arguments it can accept:
- A handler type.
- A consumer for logging errors.
- A consumer for logging debug info.
- Alternatively, one consumer for both info and errors.

The default handler type is `LambdaHandler`

The default consumer logs to console, with the prefix `[EVENTBUS]:`

The following are all valid:
```java
new EventBus();
new EventBus(ASMHandler.class);
new EventBus(MyUtilityMod.logger::error);
new EventBus(System.out::println, info -> mc.player.sendChatMessage("I love bush's event bus! " + info));
new EventBus(ReflectHandler.class, MyUtilityMod.logger::error);
new EventBus(ASMHandler.class, MyUtilityMod.logger::error, MyUtilityMod.logger::info);
```

### Creating an Event:
Extend `Event`, and implement the method `isCancellable()`.

Example:
```java
import me.bush.eventbus.event.Event;

public class MyEvent extends Event {

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
```

### Creating a Listener:
Create a public void method with one parameter, which is a subclass of `Event`. 
Annotate the method with `@EventListener`.

There are two modifiers you can add to the annotation:
- `priority`: Listeners with high priority will recieve events before listeners with low priority.
- `recieveCancelled`: Listeners with recieveCancelled enabled will recieve events even after they are cancelled.

Example:
```java
@EventListener
public void onEvent(MyEvent event) {}
```

### Subscribing an Object/Class:
Calling `EventBus#subscribe` and `EventBus#unsubscribe` will add and remove listeners from the EventBus.
Only listeners in subscribed objects and classes will recieve events. 
- For non static listeners to recieve events, you **must** subscribe an object.
- For static listeners to recieve events, you **must** subscribe a class.

Static listeners will **not** recieve events if only an object is subscribed, and vice versa.

### Posting an Event:
Calling `EventBus#post` will post an event to every listener with an **exactly** matching event type.
For example, if event B extends event A, and event A is posted, B listeners will not recieve it.

This method will return true if the posted event was cancelled, and false otherwise.

## Features

### Thread Safe
I tried to make it fail, and it wouldn't.

### Simple to Use
Simple implementation, and simple listener syntax.

### Fast
Nearly twice as fast as the default Forge EventBus.

### Flexible
*3 Different Handler Types*<br>
*You can also make your own by extending* `Handler`<br>
~~Kinda pointless, but it was fun to make (just use lambdahandler lol)~~

#### ASMHandler:
The same invocation style Forge uses. This is pretty fast, but a little hacky.
#### LambdaHandler:
Uses LambdaMetaFactory to create a "function object", which is nearly as fast as direct access. 
#### ReflectHandler:
The most basic style, but also the most reliable.

###
