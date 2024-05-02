package com.dongkuksystems.dbox.config;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static springfox.documentation.builders.RequestHandlerSelectors.withMethodAnnotation;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.dongkuksystems.dbox.config.support.Pageable;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.Contact;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//추후 dev적용
//@Profile(value="dev")
@Configuration
@EnableSwagger2
public class Swagger2Configure implements WebMvcConfigurer {

  @Value("${spring.application.name}")
  private String applicationName;

  @Value("${jwt.token.header}")
  private String tokenHeader;

  @Bean
  public Docket restApi() {
    return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
        .ignoredParameterTypes(AuthenticationPrincipal.class, Pageable.class).securitySchemes(singletonList(apiKey()))
        .securityContexts(singletonList(securityContext())).produces(singleton("application/json"))
        .consumes(singleton("application/json")).useDefaultResponseMessages(false).select()
        .apis(withMethodAnnotation(ApiOperation.class)).paths(PathSelectors.any()).build();
  }

  @Bean
  public SecurityConfiguration security() {
    return SecurityConfigurationBuilder.builder().scopeSeparator(",").additionalQueryStringParams(null)
        .useBasicAuthenticationWithAccessCodeGrant(false).build();
  }

  @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui.html**")
                .addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
          registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

//        registry.addResourceHandler("/swagger-ui.html**").addResourceLocations("classpath:/META-INF/resources/swagger-ui.html");
//        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title(applicationName).contact(new Contact("username", null, "your-email@address.com"))
        .version("1.0.0").build();
  }

  private ApiKey apiKey() {
    return new ApiKey("apiKey", tokenHeader, "header");
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder().securityReferences(securityReference()).forPaths(PathSelectors.any()).build();
  }

  private List<SecurityReference> securityReference() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return singletonList(new SecurityReference("apiKey", authorizationScopes));
  }

}