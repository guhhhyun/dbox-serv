package com.dongkuksystems.dbox.config;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.expression.WebExpressionVoter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.dongkuksystems.dbox.models.common.Documentum;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.EntryPointUnauthorizedHandler;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.securities.JwtAccessDeniedHandler;
import com.dongkuksystems.dbox.securities.JwtAuthenticationProvider;
import com.dongkuksystems.dbox.securities.JwtAuthenticationTokenFilter;
import com.dongkuksystems.dbox.services.login.LoginService;

@Configuration
@EnableWebSecurity(debug = false)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfigure extends WebSecurityConfigurerAdapter {
  
//  @Override
//  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
//      auth.inMemoryAuthentication()
//              .withUser(User.builder()
//                      .username("user1")
//                      .password(passwordEncoder().encode("1111"))
//                      .roles("USER")
//              .build())
//              ;
//  }
//
//  @Override
//  protected void configure(HttpSecurity http) throws Exception {
//      http
//              .httpBasic()
//              ;
//  }
//  
//  @Bean
//  PasswordEncoder passwordEncoder() {
//   return new BCryptPasswordEncoder(); 
//  }

  private final JwtAccessDeniedHandler accessDeniedHandler;

  private final EntryPointUnauthorizedHandler unauthorizedHandler;
  private final Documentum documentum;

  public WebSecurityConfigure(JwtAccessDeniedHandler accessDeniedHandler, EntryPointUnauthorizedHandler unauthorizedHandler, Documentum documentum) {
      this.accessDeniedHandler = accessDeniedHandler;
      this.unauthorizedHandler = unauthorizedHandler;
      this.documentum = documentum;
  }

  @Autowired
  public void configureAuthentication(AuthenticationManagerBuilder builder, JwtAuthenticationProvider authenticationProvider) {
      builder.authenticationProvider(authenticationProvider);
  }

  @Bean
  public JwtAuthenticationProvider jwtAuthenticationProvider(JWT jwt, LoginService loginService, RedisRepository redisRepository) {
      return new JwtAuthenticationProvider(jwt, loginService, redisRepository);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
      return super.authenticationManagerBean();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
      return new PasswordEncoder() {
        
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
          if (rawPassword == null) {
            throw new IllegalArgumentException("rawPassword cannot be null");
          }
          if (encodedPassword == null || encodedPassword.length() == 0) { 
            return false;
          }
          if (encrypt(rawPassword.toString()).equals(encodedPassword)) { 
            return true;
          }
          return false;
        }
        
        @Override
        public String encode(CharSequence rawPassword) {
          return encrypt(rawPassword.toString());
        }
        
        private String encrypt(String s) {
          try {
              MessageDigest md = MessageDigest.getInstance("SHA-256");
              byte[] passBytes = s.getBytes();
              md.reset();
              byte[] digested = md.digest(passBytes);
              StringBuilder sb = new StringBuilder();
              for (int i = 0; i < digested.length; i++) sb.append(Integer.toString((digested[i]&0xff) + 0x100, 16).substring(1));
              return sb.toString().toUpperCase();
          } catch (Exception e) {
              return s;
          }
        }
      };
//      return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter() {
      return new JwtAuthenticationTokenFilter();
  }

//  @Bean
//  public ConnectionBasedVoter connectionBasedVoter() {
//      
//  }

  @Bean
  public AccessDecisionManager accessDecisionManager() {
      List<AccessDecisionVoter<?>> decisionVoters = new ArrayList<>();
      decisionVoters.add(new WebExpressionVoter());
//      decisionVoters.add(connectionBasedVoter());
      // 모든 voter가 승인해야 해야한다.
      return new UnanimousBased(decisionVoters);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
      http
      		.cors()
      				.and()
          .csrf()
              .disable()
          .headers()
              .disable()
          .exceptionHandling()
              .accessDeniedHandler(accessDeniedHandler)
              .authenticationEntryPoint(unauthorizedHandler)
              .and()
          .sessionManagement()
//              .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
              .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
              .and()
          .authorizeRequests()
//              .antMatchers("/api/_hcheck").permitAll()
              .antMatchers("/swagger-ui.html**").permitAll()
              .antMatchers("/api/login").permitAll()  
              .antMatchers("/api/external/users/*/password").permitAll()  
              .antMatchers("/api/logout").permitAll() 
              .antMatchers("/api/mobile-version").permitAll() 
              .antMatchers("/api/external/infs-installer").permitAll()
              .antMatchers("/api/external/cache/init").permitAll()
              .antMatchers("/api/data/*/url-download").permitAll()
              .antMatchers("/api/takeout-requests/*/doc").permitAll()
              .antMatchers("/api/external/users/*/dsearch-auth").permitAll()
              .antMatchers("/api/log/debug").permitAll()
              .antMatchers("/api/**").hasRole("USER")
              .accessDecisionManager(accessDecisionManager())
              .anyRequest().permitAll()
              .and()
          .formLogin()
              .disable();
      http
          .addFilterBefore(jwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
  }

  @Override
  public void configure(WebSecurity web) {
      web.ignoring().antMatchers("/swagger-resources/**", "/swagger-ui.html**","/webjars/**", "/templates/**", "/h2-console/**", "/h2/**", "/kupload/**"); 
  }
 
}