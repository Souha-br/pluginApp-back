package com.example.test1.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.example.test1.repositories.jira",
        entityManagerFactoryRef = "jiraEntityManagerFactory",
        transactionManagerRef = "jiraTransactionManager"
)
public class JiraDbConfig {

    @Bean(name = "jiraDataSource")
    @ConfigurationProperties(prefix = "jira.datasource")
    public DataSource jiraDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(name = "jiraEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean jiraEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("jiraDataSource") DataSource dataSource) {

        Map<String, Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        properties.put("hibernate.hbm2ddl.auto", "none");

        return builder
                .dataSource(dataSource)
                .packages("com.example.test1.entities.jira")
                .properties(properties)
                .persistenceUnit("jiradb")
                .build();
    }

    @Bean(name = "jiraTransactionManager")
    public PlatformTransactionManager jiraTransactionManager(
            @Qualifier("jiraEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
