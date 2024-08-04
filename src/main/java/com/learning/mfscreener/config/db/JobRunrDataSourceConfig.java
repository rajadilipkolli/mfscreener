package com.learning.mfscreener.config.db;

import org.jobrunr.spring.autoconfigure.storage.JobRunrSqlStorageAutoConfiguration;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.jdbc.JdbcConnectionDetails;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(JdbcConnectionDetails.class)
@AutoConfigureBefore(JobRunrSqlStorageAutoConfiguration.class)
class JobRunrDataSourceConfig implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

        BeanDefinition beanDefinition = beanFactory.getBeanDefinition("dataSource");
        beanDefinition.getPropertyValues().add("poolName", "jobrunr");

        ((DefaultListableBeanFactory) beanFactory).registerBeanDefinition("jobrunrDataSource", beanDefinition);
    }
}
