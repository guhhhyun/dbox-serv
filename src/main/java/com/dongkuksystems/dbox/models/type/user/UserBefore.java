package com.dongkuksystems.dbox.models.type.user;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.time.LocalDateTime.now;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserBefore { 
  
  @ApiModelProperty(value = "PK", required = true, example = "1234")
  private String userId;
  @ApiModelProperty(value = "노드아이디", required = true)
  private String nodeId;
  @ApiModelProperty(value = "niris아이디", required = true)
  private String loginId;
  @ApiModelProperty(value = "이름")
  private String name;
  @ApiModelProperty(value = "권한값")
  private String authKey;
  @ApiModelProperty(value = "커스텀권한값")
  private String customToken;
  @ApiModelProperty(value = "권한기한")
  private Date authKeyExpire;
  @ApiModelProperty(value = "사번")
  private String sabun;
  @JsonIgnore
  @ApiModelProperty(value = "비밀번호", hidden = true)
  private String password;
  @ApiModelProperty(value = "부서아이디") 
  private String groupId;
  @ApiModelProperty(value = "pstnCode")
  private String pstnCode;
  @ApiModelProperty(value = "pstn이름")
  private String pstnName;
  @ApiModelProperty(value = "levelCode")
  private String levelCode;
  @ApiModelProperty(value = "level이름")
  private String levelName;
  @ApiModelProperty(value = "titleCode")
  private String titleCode;
  @ApiModelProperty(value = "title이름")
  private String titleName;
  @ApiModelProperty(value = "이메일")
  private String email;
  @ApiModelProperty(value = "핸드폰번호")
  private String mobileTel;
  @ApiModelProperty(value = "생성유저아이디")
  private String creatorId;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성일")
  private LocalDateTime regDate;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정유저아이디")
  private String modifierId;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정일")
  private LocalDateTime modDate;
   
  @ApiModelProperty(value = "상태")
  private String status;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "승진일")
  private LocalDateTime promotionDt;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "입사일")
  private LocalDateTime enterDt;
  @ApiModelProperty(value = "정렬키")
  private String sortKey; 
  @ApiModelProperty(value = "로그인 시도 횟수")
  private int loginCount;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "마지막 로그인 일")
  private LocalDateTime lastLoginAt;

  public UserBefore(String userId, String nodeId, String loginId, String password, int loginCount, LocalDateTime lastLoginAt) {
    checkArgument(isNotEmpty(name), "name must be provided.");
    checkArgument(
            name.length() >= 1 && name.length() <= 10,
            "name length must be between 1 and 10 characters."
    );
    checkNotNull(email, "email must be provided.");
    checkNotNull(password, "password must be provided.");
    checkArgument(
        password == null || password.length() >= 8,
            "password length must be longer than 8 characters."
    );

    this.userId =userId;
    this.nodeId = nodeId;
    this.loginId = loginId;
    this.password = password; 
    this.loginCount = loginCount;
    this.lastLoginAt = defaultIfNull(lastLoginAt, now());
  } 
  
  public void login(PasswordEncoder passwordEncoder, String credentials) {
    
    String tmp = passwordEncoder.encode(credentials);
    if (!passwordEncoder.matches(credentials, password))
        throw new IllegalArgumentException("Bad credential");
  }
  
  public void afterLoginSuccess() {
    loginCount++;
    lastLoginAt = now();
  }
}
