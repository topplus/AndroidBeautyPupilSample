package example.com.androidbeautypupil.manager;

import org.greenrobot.eventbus.EventBus;

/**
 * @author fandong
 * @date 2016/10/28.
 * @description
 */
public class EventBusManager {

    public static void register(Object subscriber) {
        EventBus.getDefault().register(subscriber);
    }

    public static void unregister(Object subscriber) {
        EventBus.getDefault().unregister(subscriber);
    }

    public static boolean isRegister(Object subscriber) {
        return EventBus.getDefault().isRegistered(subscriber);
    }

    public static void post(Object event) {
        EventBus.getDefault().post(event);
    }
}
