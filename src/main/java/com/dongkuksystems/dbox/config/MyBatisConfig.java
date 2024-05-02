package com.dongkuksystems.dbox.config;

import javax.sql.DataSource;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.JdbcType;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = { "com.dongkuksystems.dbox.daos" })
public class MyBatisConfig {
  private static final Logger logger = LoggerFactory.getLogger(MyBatisConfig.class);
  
  @Value("${spring.datasource.jndi-name}")
  private String jndiName;
  
  @Bean(name = "dboxDb")
  public DataSource dataSource() {
    try {
      logger.info("JNDI 이름: " + jndiName);

      JndiObjectFactoryBean bean = new JndiObjectFactoryBean();    
      bean.setJndiName(jndiName);
      bean.setProxyInterface(DataSource.class);
      bean.afterPropertiesSet();
      
      return (DataSource) bean.getObject();
    } catch (Exception e) {
      logger.error("★★★★★★★★★★ JNDI 연결에 실패하였습니다. 임베디드 DataSource를 사용하여 연결합니다. (로컬일 경우 아래 StackTrace는 무시해주세요) ★★★★★★★★★★");
      logger.error(ExceptionUtils.getStackTrace(e));
      return embeddedDataSource();
    }
  }

  @Bean(name = "embeddedDataSource")
  @ConfigurationProperties(prefix = "spring.datasource.hikari")
  public DataSource embeddedDataSource() {
    return DataSourceBuilder.create().type(HikariDataSource.class).build();
  }

  @Bean
  @ConfigurationProperties(prefix = "mybatis")
  public SqlSessionFactoryBean sqlSessionFactory(@Qualifier("dboxDb") DataSource dataSource) throws Exception {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();

    org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
    configuration.setCacheEnabled(true);
    configuration.setLazyLoadingEnabled(true);
    configuration.setMapUnderscoreToCamelCase(true);
    configuration.setJdbcTypeForNull(JdbcType.NULL);
    configuration.setCallSettersOnNulls(true);

    sqlSessionFactoryBean.setConfiguration(configuration);
    sqlSessionFactoryBean.setDataSource(dataSource);
    sqlSessionFactoryBean.setTypeAliasesPackage("com.dongkuksystems.dbox.models");
    sqlSessionFactoryBean
        .setMapperLocations(new PathMatchingResourcePatternResolver().getResources("mappers/**/*.xml"));

    return sqlSessionFactoryBean;
  }

  @Bean
  public SqlSessionTemplate sqlSession(SqlSessionFactory sqlSessionFactory) {
    return new SqlSessionTemplate(sqlSessionFactory);
  }

  @Bean
  public DataSourceTransactionManager transactionManager(@Qualifier("dboxDb") DataSource dataSource) {
    return new DataSourceTransactionManager(dataSource);
  }
}