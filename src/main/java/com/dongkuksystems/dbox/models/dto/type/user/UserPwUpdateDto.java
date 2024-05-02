package com.dongkuksystems.dbox.models.dto.type.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.google.common.base.Objects;
import static com.google.common.base.Preconditions.checkArgument;

@Data
@NoArgsConstructor
@Builder
public class UserPwUpdateDto {
	
  @ApiModelProperty(value = "사용자 ID")
  private String oldPw;
  @ApiModelProperty(value = "잠금구분")
  private String newPw;
  
  public UserPwUpdateDto(String oldPw, String newPw) {
    checkArgument(!Objects.equal(oldPw, null) && !Objects.equal(oldPw, "") , "oldPw는 필수 데이터입니다.");
    checkArgument(!Objects.equal(newPw, null) && !Objects.equal(newPw, "") , "newPw는 필수 데이터입니다.");
    this.oldPw = oldPw;
    this.newPw = newPw;
  }
  
  public void chkValues() {
    checkArgument(!Objects.equal(oldPw, null) && !Objects.equal(oldPw, "") , "oldPw는 필수 데이터입니다.");
    checkArgument(!Objects.equal(newPw, null) && !Objects.equal(newPw, "") , "newPw는 필수 데이터입니다.");
  }
}
