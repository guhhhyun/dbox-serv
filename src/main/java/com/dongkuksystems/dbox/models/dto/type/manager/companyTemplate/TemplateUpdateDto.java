package com.dongkuksystems.dbox.models.dto.type.manager.companyTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TemplateUpdateDto {
  private String rObjectId; 
  private String uTemplateName; 


}
