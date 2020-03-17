package cn.laoshini.dk.config.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.CommandLinePropertySource;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.context.support.StandardServletEnvironment;

/**
 * 由于spring自身实现的{@link ContextRefresher}类中只支持{@link StandardEnvironment}配置项刷新（详见{@link ContextRefresher#copyEnvironment(ConfigurableEnvironment)}方法），
 * 项目中为了使用spring cloud config，重写了{@link StandardEnvironment}类（即{@link DkConfigClientEnvironment}），所以必须重写对应的配置刷新类，才能使用spring cloud bus实现配置源的自动刷新
 * <p>
 * 思考：Spring实现的{@link ContextRefresher}类中大量方法和属性是私有的，没法针对性重写，导致我把整个类的代码全部复制了一遍。
 * 我猜测Spring这么做的原因，可能是觉得该类不需要重写；其实在大多数情况下确实是不需要的，之所以这里需要，是因为：
 * 该项目不是spring boot项目，为了使用cloud config和cloud bus功能，做了一些订制处理
 * </p>
 *
 * @author fagarine
 */
public class CustomerContextRefresher extends ContextRefresher {

    private static final String REFRESH_ARGS_PROPERTY_SOURCE = "refreshArgs";

    private static final String[] DEFAULT_PROPERTY_SOURCES = new String[] {
            // order matters, if cli args aren't first, things get messy
            CommandLinePropertySource.COMMAND_LINE_PROPERTY_SOURCE_NAME, "defaultProperties" };

    private Set<String> standardSources = new HashSet<>(
            Arrays.asList(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                    StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME,
                    StandardServletEnvironment.SERVLET_CONFIG_PROPERTY_SOURCE_NAME,
                    StandardServletEnvironment.SERVLET_CONTEXT_PROPERTY_SOURCE_NAME, "configurationProperties"));

    private ConfigurableApplicationContext context;

    private RefreshScope scope;

    public CustomerContextRefresher(ConfigurableApplicationContext context, RefreshScope scope) {
        super(context, scope);
        this.context = context;
        this.scope = scope;
    }

    @Override
    public synchronized Set<String> refreshEnvironment() {
        Map<String, Object> before = extract(this.context.getEnvironment().getPropertySources());
        addConfigFilesToEnvironment();
        Set<String> keys = changes(before, extract(this.context.getEnvironment().getPropertySources())).keySet();
        this.context.publishEvent(new EnvironmentChangeEvent(this.context, keys));
        return keys;
    }

    /* For testing. */ ConfigurableApplicationContext addConfigFilesToEnvironment() {
        ConfigurableApplicationContext capture = null;
        try {
            StandardEnvironment environment = copyEnvironment(this.context.getEnvironment());
            SpringApplicationBuilder builder = new SpringApplicationBuilder(ContextRefresher.Empty.class)
                    .bannerMode(Banner.Mode.OFF).web(WebApplicationType.NONE).environment(environment);
            // Just the listeners that affect the environment (e.g. excluding logging
            // listener because it has side effects)
            builder.application().setListeners(
                    Arrays.asList(new BootstrapApplicationListener(), new ConfigFileApplicationListener()));
            capture = builder.run();
            if (environment.getPropertySources().contains(REFRESH_ARGS_PROPERTY_SOURCE)) {
                environment.getPropertySources().remove(REFRESH_ARGS_PROPERTY_SOURCE);
            }
            MutablePropertySources target = this.context.getEnvironment().getPropertySources();
            String targetName = null;
            for (PropertySource<?> source : environment.getPropertySources()) {
                String name = source.getName();
                if (target.contains(name)) {
                    targetName = name;
                }
                if (!this.standardSources.contains(name)) {
                    if (target.contains(name)) {
                        target.replace(name, source);
                    } else {
                        if (targetName != null) {
                            target.addAfter(targetName, source);
                        } else {
                            // targetName was null so we are at the start of the list
                            target.addFirst(source);
                            targetName = name;
                        }
                    }
                }
            }
        } finally {
            ConfigurableApplicationContext closeable = capture;
            while (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // Ignore;
                }
                if (closeable.getParent() instanceof ConfigurableApplicationContext) {
                    closeable = (ConfigurableApplicationContext) closeable.getParent();
                } else {
                    break;
                }
            }
        }
        return capture;
    }

    // Don't use ConfigurableEnvironment.merge() in case there are clashes with property
    // source names
    private StandardEnvironment copyEnvironment(ConfigurableEnvironment input) {
        // 重写ContextRefresher类，只为了改变这一行代码，替换StandardEnvironment为DkConfigClientEnvironment
        StandardEnvironment environment = new DkConfigClientEnvironment();
        MutablePropertySources capturedPropertySources = environment.getPropertySources();
        // Only copy the default property source(s) and the profiles over from the main
        // environment (everything else should be pristine, just like it was on startup).
        for (String name : DEFAULT_PROPERTY_SOURCES) {
            if (input.getPropertySources().contains(name)) {
                if (capturedPropertySources.contains(name)) {
                    capturedPropertySources.replace(name, input.getPropertySources().get(name));
                } else {
                    capturedPropertySources.addLast(input.getPropertySources().get(name));
                }
            }
        }
        environment.setActiveProfiles(input.getActiveProfiles());
        environment.setDefaultProfiles(input.getDefaultProfiles());
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("spring.jmx.enabled", false);
        map.put("spring.main.sources", "");
        capturedPropertySources.addFirst(new MapPropertySource(REFRESH_ARGS_PROPERTY_SOURCE, map));
        return environment;
    }

    private Map<String, Object> changes(Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> result = new HashMap<String, Object>();
        for (String key : before.keySet()) {
            if (!after.containsKey(key)) {
                result.put(key, null);
            } else if (!equal(before.get(key), after.get(key))) {
                result.put(key, after.get(key));
            }
        }
        for (String key : after.keySet()) {
            if (!before.containsKey(key)) {
                result.put(key, after.get(key));
            }
        }
        return result;
    }

    private boolean equal(Object one, Object two) {
        if (one == null && two == null) {
            return true;
        }
        if (one == null || two == null) {
            return false;
        }
        return one.equals(two);
    }

    private Map<String, Object> extract(MutablePropertySources propertySources) {
        Map<String, Object> result = new HashMap<String, Object>();
        List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
        for (PropertySource<?> source : propertySources) {
            sources.add(0, source);
        }
        for (PropertySource<?> source : sources) {
            if (!this.standardSources.contains(source.getName())) {
                extract(source, result);
            }
        }
        return result;
    }

    private void extract(PropertySource<?> parent, Map<String, Object> result) {
        if (parent instanceof CompositePropertySource) {
            try {
                List<PropertySource<?>> sources = new ArrayList<PropertySource<?>>();
                for (PropertySource<?> source : ((CompositePropertySource) parent).getPropertySources()) {
                    sources.add(0, source);
                }
                for (PropertySource<?> source : sources) {
                    extract(source, result);
                }
            } catch (Exception e) {
                return;
            }
        } else if (parent instanceof EnumerablePropertySource) {
            for (String key : ((EnumerablePropertySource<?>) parent).getPropertyNames()) {
                result.put(key, parent.getProperty(key));
            }
        }
    }
}
