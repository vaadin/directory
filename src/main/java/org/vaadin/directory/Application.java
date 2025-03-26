package org.vaadin.directory;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.NoTheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.spi.CachingProvider;
import javax.cache.expiry.Duration;

/**
 * The entry point of the Spring Boot application.
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@NoTheme
@Import(com.vaadin.directory.backend.BackendConfig.class)
public class Application extends SpringBootServletInitializer
        implements AppShellConfigurator, ApplicationListener<ContextRefreshedEvent> {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Init data here
    }

    @Bean
    public CacheManager cacheManager() {

        CachingProvider provider = Caching.getCachingProvider();
        javax.cache.CacheManager jcacheManager = provider.getCacheManager();

        // 15-minute cache
        jcacheManager.createCache("cache15m", new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.TEN_MINUTES))
                .setStoreByValue(false));

        // 1-hour cache
        jcacheManager.createCache("cache1h", new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_HOUR))
                .setStoreByValue(false));

        // 24-hour cachecache24h
        jcacheManager.createCache("cache24h", new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ONE_DAY))
                .setStoreByValue(false));

        // eternal cache
        jcacheManager.createCache("cacheEternal", new MutableConfiguration<>()
                .setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(Duration.ETERNAL))
                .setStoreByValue(false));

        return new JCacheCacheManager(jcacheManager);
    }

}
