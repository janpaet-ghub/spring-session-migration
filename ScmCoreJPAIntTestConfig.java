package de.fms.scm.config;

import de.fms.scm.utils.LogUtil;
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
import java.util.Properties;

@Configuration
@EnableTransactionManagement
@Profile(value = "testCore")
@PropertySource("classpath:/core.configuration.properties")
public class ScmCoreJPAIntTestConfig {

    private static final LogUtil LOG = new LogUtil(ScmCoreJPAIntTestConfig.class);

    private static final String PACKAGES_TO_SCAN = "de.fms.scm.entities";

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

    @Bean(name = "entityManagerFactoryBean")
    public LocalContainerEntityManagerFactoryBean entityManagerFactoryBean(final DataSource dataSource) {
        LOG.info("... create bean entityManagerFactoryBean");
        final LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setJpaVendorAdapter(getJpaVendorAdapter());
        emf.setDataSource(dataSource);
        emf.setJpaProperties(getJpaProperties());
        emf.setPackagesToScan(PACKAGES_TO_SCAN);
        return emf;
    }

    @Bean(name = "getJpaVendorAdapter")
    public JpaVendorAdapter getJpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean(name = "emTxManager")
    public PlatformTransactionManager emTxManager() {
        LOG.info("... create bean emTxManager");
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactoryBean(dataSource()).getObject());
        return transactionManager;
    }

    /**
     * Configuration properties related to Hibernate
     * 
     * @return The Hibernate properties
     */
    final Properties getJpaProperties() {
        final Properties properties = new Properties();
        properties.setProperty("hibernate.dialect", HIBERNATE_DIALECT);
        properties.setProperty("hibernate.hbm2ddl.auto", HIBERNATE_HBM2DLL_AUTO);
        properties.setProperty("hibernate.show_sql", HIBERNATE_SHOW_SQL);
        properties.setProperty("hibernate.format_sql", HIBERNATE_FORMAT_SQL);
        return properties;
    }
}
