package com.dongkuksystems.dbox.securities;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.dongkuksystems.dbox.models.api.response.ApiResult.ERROR;

@Component
public class EntryPointUnauthorizedHandler implements AuthenticationEntryPoint {

  static ApiResult E401 = ERROR("Authentication error (cause: unauthorized)", HttpStatus.UNAUTHORIZED);

  private ObjectMapper om;

  public EntryPointUnauthorizedHandler(ObjectMapper om) {
      this.om = om;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
          throws IOException, ServletException {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      response.setHeader("content-type", "application/json");
      response.getWriter().write(om.writeValueAsString(E401));
      response.getWriter().flush();
      response.getWriter().close();
  }

}