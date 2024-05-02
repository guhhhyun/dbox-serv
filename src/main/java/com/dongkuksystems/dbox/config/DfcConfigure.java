package com.dongkuksystems.dbox.config;

import java.io.IOException;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class DfcConfigure {
  private static final Logger logger = LoggerFactory.getLogger(DfcConfigure.class);
  
  @Value("${dfc.properties.file}")
	private String dfcPropertiesFile;

  @PostConstruct
  public void init() {
    ClassPathResource resource = new ClassPathResource(dfcPropertiesFile);
    try {
      String path = Paths.get(resource.getURI()).toString();
      logger.info("dfc.properties.file: " + path);
      
      // dfc.jar 내부에서 참조하는 dfc.properties 파일의 경로 변수 (설정하지 않을 경우 기본값으로 dfc.properties가 설정됨)
      System.setProperty("dfc.properties.file", path);
    } catch (IOException e) {
      logger.error(ExceptionUtils.getMessage(e));
    }
  }
}
