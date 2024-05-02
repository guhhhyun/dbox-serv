package com.dongkuksystems.dbox.models.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DocLogDto {

    private String objectId;
    private String jobCode;
    private String jobGubun;
}
