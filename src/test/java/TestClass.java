import me.bush.eventbus.annotation.EventListener;
import me.bush.eventbus.annotation.ListenerPriority;

/**
 * Started: 12/2/2021
 *
 * @author bush
 */
public class TestClass {

    @EventListener(priority = ListenerPriority.HIGHEST)
    public void instanceListener(TestEvent event) {
        System.out.println("Instance listener recieved event " + event.getString());
    }

    @EventListener(priority = ListenerPriority.HIGH)
    public static void staticListener(TestEvent event) {
        System.out.println("Static listener recieved event " + event.getString());
    }

    @EventListener(recieveCancelled = true) // Default is normal priority
    public void recieveCancelled(TestEvent event) {
        System.out.println("RecieveCancelled listener recieved event " + event.getString());
    }
}
