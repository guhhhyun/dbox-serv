package com.dongkuksystems.dbox.models.type.agent;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentFolder {

	@ApiModelProperty(value = "순서")
	@Builder.Default
	private String order_code = "0";
	@ApiModelProperty(value = "Key")
	@Builder.Default
	private String rid = "";
	@ApiModelProperty(value = "Key Name")
	@Builder.Default
	private String text = "";
	@ApiModelProperty(value = "보안등급")
	@Builder.Default
	private String r_folder_security = "";
	@ApiModelProperty(value = "Link Type")
	@Builder.Default
	private String r_link_type = "";
	@ApiModelProperty(value = "Folder Type")
	@Builder.Default
	private String folder_type = "";
	@ApiModelProperty(value = "Object Type")
	@Builder.Default
	private String r_object_type = "";
	
	/**
	 * Default 폴더 생성
	 * @param folder
	 * @return
	 */
	public static Map<String, Object> toMap(AgentFolder folder) {
		
		// Map<String, Object> map = new HashMap<>();
		Map<String, Object> map = new LinkedHashMap<>();	// HashMap 아닌 순서에 맞는 LinkedHashMap 사용
		
		map.put("order_code"		, folder.getOrder_code());
		map.put("rid"				, folder.getRid());
		map.put("text"				, folder.getText());
		map.put("r_folder_security"	, folder.getR_folder_security());
		map.put("r_link_type"		, folder.getR_link_type());
		map.put("folder_type"		, folder.getFolder_type());
		map.put("r_object_type"		, folder.getR_object_type());
		
		return map;
	}
	
	public static Map<String, Object> toMap(FolderDetailDto folder, String order, String rid, String sLinkType) {
		
		Map<String, Object> map = new HashMap<>();
		// Map<String, Object> map = new LinkedHashMap<>();	// HashMap 아닌 순서에 맞는 LinkedHashMap 사용
		
		map.put("order_code"		, order);
		map.put("rid"				, folder.getRObjectId()+"_"+rid);
		map.put("text"				, folder.getUFolName());
		map.put("r_folder_security"	, folder.getUSecLevel());
		map.put("r_link_type"		, sLinkType);
		map.put("folder_type"		, folder.getUFolType());
		map.put("r_object_type"		, folder.getUFolType());
		
		return map;
	}
	
}

