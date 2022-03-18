import me.bush.eventbus.event.Event;

/**
 * Started: 12/2/2021
 *
 * @author bush
 */
public class TestEvent extends Event {
    private final String string;

    public TestEvent(String string) {
        this.string = string;
    }

    public String getString() {
        return this.string;
    }

    @Override
    protected boolean isCancellable() {
        return true;
    }
}
