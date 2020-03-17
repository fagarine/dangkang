package cn.laoshini.dk.listener;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import cn.laoshini.dk.support.IPropertyRefreshable;

/**
 * 配置项更新事件监听器
 *
 * @author fagarine
 */
@Component
public class PropertyChangedListener implements IDkContextListener<PropertyChangedEvent> {

    private static Set<IPropertyRefreshable> refreshers = new LinkedHashSet<>();

    public static void addRefresher(IPropertyRefreshable refresher) {
        refreshers.add(refresher);
    }

    @Override
    public void onApplicationEvent(PropertyChangedEvent event) {
        for (IPropertyRefreshable refresher : refreshers) {
            refresher.refresh(event.getChangedPropertyKeys());
        }
    }
}
