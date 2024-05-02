package com.dongkuksystems.dbox.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;

import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.utils.MessageUtils;

@Configuration
public class ServiceConfigure {
  @Value("${jwt.token.issuer}")
  String issuer;
  @Value("${jwt.token.clientSecret}")
  String clientSecret;
  @Value("${jwt.token.expirySeconds}")
  int expirySeconds;

  @Bean
  public JWT jwt() {
    return new JWT(issuer, clientSecret, expirySeconds);
  }

//  @Bean(name = "h2Db")
//  @ConfigurationProperties(prefix="spring.datasource.h2")
//  public DataSource dataSource() throws SQLException {
//    Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "8089").start();
//    return new HikariDataSource();
//  }

  @Bean
  public HttpMessageConverters customConverters() {
    ByteArrayHttpMessageConverter arrayHttpMessageConverter = new ByteArrayHttpMessageConverter();
    return new HttpMessageConverters(arrayHttpMessageConverter);
  }

  @Bean
  public MessageSourceAccessor messageSourceAccessor(MessageSource messageSource) {
    return new MessageSourceAccessor(messageSource);
  }

  @Bean
  public MessageUtils messageUtils() {
    return MessageUtils.getInstance();
  }
}