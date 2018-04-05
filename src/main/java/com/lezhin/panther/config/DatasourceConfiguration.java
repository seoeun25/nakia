package com.lezhin.panther.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

/**
 * @author seoeun
 * @since 2018.04.03
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        entityManagerFactoryRef = "pantherEntityManager",
        transactionManagerRef = "pantherTransactionManager",
        basePackages = "com.lezhin.panther"
)
public class DatasourceConfiguration {

    private final Logger logger = LoggerFactory.getLogger(DatasourceConfiguration.class);

    @Primary
    @Bean("pantherJpaProperties")
    @ConfigurationProperties(prefix = "spring.jpa")
    public JpaProperties getProperties() {
        return new JpaProperties();
    }

    @Bean(name = "pantherDataSource")
    @ConfigurationProperties("spring.datasource")
    public HikariDataSource pantherDataSource(DataSourceProperties properties) {
        logger.info("---- pantherDataSource. {}, {}, {}",
                properties.getUrl(), properties.getUsername(), properties.getDriverClassName());
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }

    @Bean(name = "pantherEntityManager")
    public LocalContainerEntityManagerFactoryBean pantherEntityManager(EntityManagerFactoryBuilder builder,
                                                                       @Qualifier("pantherDataSource") DataSource dataSource) {

        getProperties().getProperties().entrySet().stream()
                .forEach(e -> logger.info("--- jpa.pros. {} = {} ", e.getKey(), e.getValue()));

        return builder
                .dataSource(dataSource)
                .packages("com.lezhin.panther")
                .persistenceUnit("panther")
                .build();
    }

    @Bean(name = "pantherTransactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("pantherEntityManager") EntityManagerFactory
                                                                 entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
