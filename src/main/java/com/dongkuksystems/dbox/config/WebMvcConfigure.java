package com.dongkuksystems.dbox.config;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.dongkuksystems.dbox.config.support.InputStreamWrapperFilter;
import com.dongkuksystems.dbox.config.support.SimpleOffsetPageRequest;
import com.dongkuksystems.dbox.config.support.SimpleOffsetPageableHandlerMethodArgumentResolver;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy.PropertyNamingStrategyBase;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.CaseFormat;

@Configuration
public class WebMvcConfigure implements WebMvcConfigurer {
  @Autowired
  @Qualifier(value = "httpInterceptor")
  private HandlerInterceptor interceptor;

//	@Value("${cors.allowed-origins}")
//	private String[] frontUrls;
	
//  @Override
//  public void addInterceptors(InterceptorRegistry registry) {
//    registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/api/login/**", "/api/session/**");
//  }
  
  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
      registry.addViewController("/").setViewName("forward:/index.html");
  }
  
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
      registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
  }
  
  @Bean
  public InternalResourceViewResolver defaultViewResolver() {
     return new InternalResourceViewResolver();
  }

  @Bean
  public SimpleOffsetPageableHandlerMethodArgumentResolver simpleOffsetPageableHandlerMethodArgumentResolver() {
    SimpleOffsetPageableHandlerMethodArgumentResolver resolver = new SimpleOffsetPageableHandlerMethodArgumentResolver();
    resolver.setFallbackPageable(new SimpleOffsetPageRequest(0, 5));
    return resolver;
  }

  @Bean
  @Primary
  public Jackson2ObjectMapperBuilder jacksonBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder() {
      @Override
      public void configure(ObjectMapper objectMapper) {
        objectMapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      }
    };
    
    builder.propertyNamingStrategy(new PropertyNamingStrategyBase() {
      private static final long serialVersionUID = -6456981617015946114L;

      @Override
      public String translate(String propertyName) {
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, propertyName);
      }
    });
    builder.serializationInclusion(JsonInclude.Include.NON_NULL);
    builder.featuresToEnable(MapperFeature.USE_STD_BEAN_NAMING);
    builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    
    // Java time module
    JavaTimeModule jtm = new JavaTimeModule();
    jtm.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ISO_DATE_TIME));
    builder.modulesToInstall(jtm);
    
    return builder;
  }
  
  @Bean
  @Order(1)
  public HiddenHttpMethodFilter hiddenHttpMethodFilter () {
      return new InputStreamWrapperFilter();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
  	HttpMessageConverter<?> jacksonConverter = converters.stream()
  			.filter((item) -> MappingJackson2HttpMessageConverter.class.equals(item.getClass()))
  			.findFirst().get();
  	
  	int idx = converters.indexOf(jacksonConverter);
  	converters.set(idx, new MappingJackson2HttpMessageConverter(jacksonBuilder().build()));
  }

//  @Override
//  public void addCorsMappings(CorsRegistry registry) {
//    registry.addMapping("/**").allowedOrigins(frontUrls).allowedMethods("*").maxAge(3600).allowCredentials(true);
//  }
}