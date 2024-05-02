package com.dongkuksystems.dbox.securities;


import static com.dongkuksystems.dbox.models.api.response.ApiResult.ERROR;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

  static ApiResult E403 = ERROR("Authentication error (cause: forbidden)", HttpStatus.FORBIDDEN);

  private ObjectMapper om;

  public JwtAccessDeniedHandler(ObjectMapper om) {
      this.om = om;
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
          throws IOException, ServletException {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setHeader("content-type", "application/json");
      response.getWriter().write(om.writeValueAsString(E403));
      response.getWriter().flush();
      response.getWriter().close();
  }

}