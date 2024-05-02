package com.dongkuksystems.dbox.models.dto.type.feedback;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class FeedbackFilterDto {
  private String uDocKey;
  private int uLevel;
}
