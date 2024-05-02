package com.dongkuksystems.dbox.services.viewer;

import java.util.HashMap;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.dongkuksystems.dbox.utils.RestTemplateUtils;


public class ViewerServiceImpl implements ViewerService {

	@Override
	public String getViewerUrl(String objectId, String token) throws Exception {
//	  	MultiValueMap<String, Object> multipartParams = new LinkedMultiValueMap<>();
//			
//			// 사이냅뷰어 API 
//			// 1. fileType(필수) - URL
//			multipartParams.add("fileType", "URL");
//			// 2. convertType - 0 : HTML, 1 : Image, 2 : PDF, -1 : DEFAULT
//			multipartParams.add("convertType", "-1");
//			// 3. filePath(필수)		
//			
//			String ndoc = getEnv().getProperty("ndoc.url");
//			String path = ndoc + "/api/nodes/" + uuid + "/content?token=" + token + "&mode=viewer";
//			multipartParams.add("filePath", path);
//			
//			// 4. fid(필수) - uuid 지정
//			multipartParams.add("fid", uuid);
//			
//			// 5. sync - true : 변환 후 뷰어로 자동 전환 / false : json반환
//			multipartParams.add("sync", "true");
//			
//			// 6. force - true : 기존 변환 결과를 사용하지 않고 재변환
//			multipartParams.add("force", "true");
//			
//			// 기타정보
//			multipartParams.add("accessCookieData", "");
//			multipartParams.add("convertLocale", "");
//			multipartParams.add("urlEncoding", "UTF-8");
//			multipartParams.add("refererUrl", "");
//			multipartParams.add("downloadUrl", "");
//			multipartParams.add("title", "");
//			multipartParams.add("single", "false");
//			multipartParams.add("fitSheet", "false");
//			multipartParams.add("openPassword", "");
//			multipartParams.add("permissionPassword", "");
//			
//			String url = "http://viewer.dongkuk.com:8080/SynapDocViewServer/job";
//			
//			RestTemplateUtils restTemplate = new RestTemplateUtils();
//
//		    HttpHeaders headers = new HttpHeaders();
//		    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//			
//			ResponseEntity<HashMap<String, Object>> returnValue = restTemplate.post(url, headers, multipartParams, new ParameterizedTypeReference<HashMap<String,Object>>() {});
//			
//			// 화면에서 window.open으로 처리해줘야 함. result 결과 호출해야 함.
//			
//			String result = returnValue.getHeaders().getLocation().toString();
//			
//			return result;
		return null;
	}

}
