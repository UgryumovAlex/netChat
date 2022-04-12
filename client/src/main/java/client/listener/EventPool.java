package client.listener;

import java.util.concurrent.ConcurrentLinkedDeque;

public class EventPool {

    private Listener listener;
    private ConcurrentLinkedDeque<Event> events;
    private Thread loop;

    public EventPool() {
        events = new ConcurrentLinkedDeque<>();
    }

    public void start() {
        loop = new Thread(this::process);
        loop.setDaemon(true);
        loop.start();
    }

    public synchronized void registerListener(Listener listener) {
        this.listener = listener;
    }

    private void process() {
        while (true) {
            Event event = events.poll();
            if (event != null) {
                listener.onEventReceived(event);
            }
        }
    }

    public void publishEvent(Event event) {
        events.add(event);
    }
}
