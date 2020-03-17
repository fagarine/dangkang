package cn.laoshini.dk.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import cn.laoshini.dk.listener.PropertyChangedListener;

/**
 * @author fagarine
 */
enum RefresherContainer {
    /**
     * 枚举实现单例
     */
    INSTANCE;

    private static final Set<IPropertyRefreshable> PROPERTY_REFRESHERS = Collections.newSetFromMap(new WeakHashMap<>());

    private static final PropertyRefresher PROPERTY_REFRESHER = new PropertyRefresher();

    static synchronized void addPropertyRefresher(IPropertyRefreshable propertyRefresher) {
        if (propertyRefresher != null) {
            if (!PROPERTY_REFRESHER.registered) {
                PROPERTY_REFRESHER.register();
            }

            PROPERTY_REFRESHERS.add(propertyRefresher);
        }
    }

    public static void refreshByPropertyKeys(Collection<String> propertyKeys) {
        for (IPropertyRefreshable refresher : PROPERTY_REFRESHERS) {
            refresher.refresh(propertyKeys);
        }
    }

    private static class PropertyRefresher implements IPropertyRefreshable {

        private boolean registered;

        void register() {
            if (!registered) {
                PropertyChangedListener.addRefresher(this);
                registered = true;
            }
        }

        @Override
        public void refresh(Collection<String> propertyKeys) {
            refreshByPropertyKeys(propertyKeys);
        }
    }
}
