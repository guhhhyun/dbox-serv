package com.dongkuksystems.dbox.models.dto.type.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserFilterDto {
  private String rObjectId;
  private String userName;
}
