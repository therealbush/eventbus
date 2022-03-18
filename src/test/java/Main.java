import me.bush.eventbus.bus.EventBus;
import me.bush.eventbus.handler.handlers.ReflectHandler;

/**
 * Started: 12/2/2021
 *
 * @author bush
 */
public class Main {

    public static void main(String[] args) {
        EventBus eventBus = new EventBus();
        TestClass testClass = new TestClass();
        TestEvent event = new TestEvent(":)");

        // Post event before subscribing (does nothing)
        eventBus.post(event);

        eventBus.subscribe(testClass);
        System.out.println("Subscribed object.");

        // Post event (only object is subscribed, so only instance methods will be invoked)
        eventBus.post(event);

        eventBus.subscribe(TestClass.class);
        System.out.println("Subscribed class.");

        // Post event (class is subscribed, so static methods will be invoked too)
        eventBus.post(event);

        event.cancel();
        System.out.println("Cancelled event.");

        // Post cancelled event (only "RecieveCancelled" should be invoked)
        eventBus.post(event);

        eventBus.getInfo();

        eventBus.setHandlerType(ReflectHandler.class);
        System.out.println("Changed listener type.");

        eventBus.getInfo();

        /*

        OUTPUT:

        Subscribed object.
        Instance listener recieved event :)
        RecieveCancelled listener recieved event :)
        Subscribed class.
        Instance listener recieved event :)
        Static listener recieved event :)
        RecieveCancelled listener recieved event :)
        Cancelled event.
        RecieveCancelled listener recieved event :)
        [EVENTBUS]: ============ EVENTBUS INFO ============
        [EVENTBUS]: Handler type             LambdaHandler
        [EVENTBUS]: Subscriber count         2
        [EVENTBUS]: Listener count           3
        [EVENTBUS]: TestEvent                3
        Changed listener type.
        [EVENTBUS]: ============ EVENTBUS INFO ============
        [EVENTBUS]: Handler type             ReflectHandler
        [EVENTBUS]: Subscriber count         2
        [EVENTBUS]: Listener count           3
        [EVENTBUS]: TestEvent                3

         */
    }
}
