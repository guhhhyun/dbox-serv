package com.dongkuksystems.dbox.securities;

import com.dongkuksystems.dbox.models.dto.mobile.MobileDeviceLoginDto;
import com.google.common.base.Preconditions;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class AuthenticationRequest {
  @ApiModelProperty(value = "로그인 아이디", required = true)
  private String userId;
  @ApiModelProperty(value = "로그인 비밀번호", required = true)
  private String password;
  @Default
  @ApiModelProperty(value = "모바일 여부", required = true)
  private Boolean isMobile = false;
  @Default
  @ApiModelProperty(value = "인증 무시 여부", hidden = true)
  private boolean isAuthIgnore = false;
  
  @ApiModelProperty(value = "모바일 정보", hidden = true)
  private MobileDeviceLoginDto mobileDevice;

  public AuthenticationRequest(String userId, String password, Boolean isMobile, boolean isAuthIgnore, MobileDeviceLoginDto mobileDevice) {
    super();
    Preconditions.checkNotNull(userId, "userId is null");
    this.userId = userId.trim();
    this.password = password;
    this.mobileDevice = mobileDevice;
    this.isMobile = isMobile;
    this.isAuthIgnore = isAuthIgnore;
  }
  
  public AuthenticationRequest(String userId, String password, MobileDeviceLoginDto mobileDevice) {
    super();
    Preconditions.checkNotNull(userId, "userId is null");
    this.userId = userId.trim();
    this.password = password;
    this.mobileDevice = mobileDevice;
  }

  public AuthenticationRequest(String userId, String password, Boolean isMobile) {
    super();
    Preconditions.checkNotNull(userId, "userId is null");
    this.userId = userId.trim();
    this.password = password;
    this.isMobile = isMobile;
  }

  public AuthenticationRequest(String userId, String password, Boolean isMobile, boolean isAuthIgnore) {
    super();
    Preconditions.checkNotNull(userId, "userId is null");
    this.userId = userId.trim();
    this.password = password;
    this.isMobile = isMobile;
    this.isAuthIgnore = isAuthIgnore;
  }
  
  public AuthenticationRequest(String userId, String password) {
    super();
    Preconditions.checkNotNull(userId, "userId is null");
    this.userId = userId.trim();
    this.password = password;
  }
}
