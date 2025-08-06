package de.fms.scm.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@Profile(value = "testCore")
@PropertySource("classpath:/core.configuration.properties")
public class ScmCoreJPAIntTestConfig {

    @Value("${db.url}")
    private String DB_URL;

    @Value("${db.username}")
    private String DB_USERNAME;

    @Value("${db.password}")
    private String DB_PASSWORD;

    @Value("${hibernate.dialect}")
    private String HIBERNATE_DIALECT;

    @Value("${hibernate.hbm2ddl.auto}")
    private String HIBERNATE_HBM2DLL_AUTO;

    @Value("${hibernate.show_sql}")
    private String HIBERNATE_SHOW_SQL;

    @Value("${hibernate.format_sql}")
    private String HIBERNATE_FORMAT_SQL;

    @Value("${hibernate.cache.use_second_level_cache}")
    private String HIBERNATE_SECOND_LEVEL_CACHE;

    @Value("${hibernate.cache.use_query_cache}")
    private String HIBERNATE_QUERY_CACHE;

    @Bean(name = "scmDataSource")
    public DataSource dataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(DB_URL);
        dataSource.setUser(DB_USERNAME);
        dataSource.setPassword(DB_PASSWORD);
        return dataSource;
    }
    
    @Bean
    LocalContainerEntityManagerFactoryBean entityManagerFactory(
            final DataSource dataSource,
            final EntityManagerFactoryBuilder builder
    ) {
        return builder
                .dataSource(dataSource)
                .packages("de.fms.scm.entities")
                .properties(getJpaProperties())
                .build();
    }    

    @Bean(name = "getJpaVendorAdapter")
    public JpaVendorAdapter getJpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }   

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }    

    private Map<String, String> getJpaProperties() {
    	final Map<String, String> properties = new HashMap<>();
    	properties.put("hibernate.dialect", HIBERNATE_DIALECT);
    	properties.put("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DLL_AUTO);
    	properties.put("hibernate.show_sql", HIBERNATE_SHOW_SQL);
    	properties.put("hibernate.format_sql", HIBERNATE_FORMAT_SQL);
        return properties;
    }
}
