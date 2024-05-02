package com.dongkuksystems.dbox.models.type.recycle;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
public class Recycle {
	  private String rObjectId;
	  @ApiModelProperty(value = "문서함유형 D:부서, P:프로젝트, R:연구과제")
	  private String uCabinetType;
	  @ApiModelProperty(value = "문서함코드")
	  private String uCabinetCode;
	  @ApiModelProperty(value = "자료유형 D:문서, F:폴더")
	  private String uObjType;
	  @ApiModelProperty(value = "자료ID")
	  private String uObjId;
	  @ApiModelProperty(value = "삭제자")
	  private String uDeleteUser;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "삭제일")
	  private LocalDateTime uDeleteDate;
	  
	  private Doc recycleDetail;
	  private VUser userDetail;
	  private VUser folderUserDetail;
	  private VUser deleteUserDetail;
	  private Folder folderDetail; 
	  private Project projectDetail;
	  private Research researchDetail;
	  private VUser recycleUserDetail;
	  private VUser projectUserDetail;
	  private VUser researchUserDetail;
	 
	  public static Map<String, String> toMap(Doc doc) {
		    Map<String, String> map = new HashMap<>();
		    map.put("u_obj_id", doc.getRObjectId());
		    return map;
		  }
}
