/**
 * 
 */
package com.cloudpaas.service.pas.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.cloudpaas.common.mybatis.MultiRoutingDataSource;
import com.zaxxer.hikari.HikariDataSource;

import tk.mybatis.spring.mapper.MapperScannerConfigurer;

/**
 * @author 大鱼
 *
 * @date 2019年8月2日 下午5:14:44
 */
@Configuration
@MapperScan(basePackages = {"com.cloudpaas.service.pas.mapper"}) 
public class MybatisConfig {
	
	@Primary
    @Bean(name = "dataSource_dn1")
	@ConfigurationProperties(prefix = "spring.datasource.druid.dn1" )
    public DataSource dataSourceDn1() {
		
		DataSource datasource = dn1DataSourceProperties()
				.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
                .build();
		return datasource;
    }
	
	@Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid.dn1")
    public DataSourceProperties dn1DataSourceProperties() {
        return new DataSourceProperties();
    }
	

	

	
	@Bean(name = "dataSource_dn2")
	@ConfigurationProperties(prefix = "spring.datasource.druid.dn2" )
    public DataSource dataSourceDn2() {
		DataSource datasource = dn2DataSourceProperties()
				.initializeDataSourceBuilder()
				.type(HikariDataSource.class)
                .build();
		return datasource;
    }
	
	@Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid.dn2")
    public DataSourceProperties dn2DataSourceProperties() {
        return new DataSourceProperties();
    }

	
	@Bean("dynamicDataSource")
    public DataSource dynamicDataSource() {
		MultiRoutingDataSource dynamicDataSource = new MultiRoutingDataSource();
        Map<Object, Object> dataSourceMap = new HashMap<>(2);
        dataSourceMap.put("dn1", dataSourceDn1());
        dataSourceMap.put("dn2", dataSourceDn2());
        // 将 master 数据源作为默认指定的数据源
        dynamicDataSource.setDefaultTargetDataSource(dataSourceDn1());
        // 将 master 和 slave 数据源作为指定的数据源
        dynamicDataSource.setTargetDataSources(dataSourceMap);
        dynamicDataSource.afterPropertiesSet();
        return dynamicDataSource;
    }
	
	@Bean(name="sqlSessionFactory")
    public SqlSessionFactoryBean sqlSessionFactoryBean() throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        // 配置数据源，此处配置为关键配置，如果没有将 dynamicDataSource作为数据源则不能实现切换
        sessionFactory.setDataSource(dynamicDataSource());
        sessionFactory.setTypeAliasesPackage("com.cloudpaas.**.model");    // 扫描Model
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources("classpath*:mybatis/mapper/*.xml"));    // 扫描映射文件
        return sessionFactory;
    }
	
	@Bean
	public MapperScannerConfigurer mapperScannerConfigurer(){
		MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
		mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
		mapperScannerConfigurer.setBasePackage("com.cloudpaas.service.pas.mapper.**");
		return mapperScannerConfigurer;
	}
	
	@Bean
    public PlatformTransactionManager transactionManager(@Qualifier("dynamicDataSource")DataSource dynamicDataSource) {
        // 配置事务管理, 使用事务时在方法头部添加@Transactional注解即可
        return new DataSourceTransactionManager(dynamicDataSource);
    }
	

}
