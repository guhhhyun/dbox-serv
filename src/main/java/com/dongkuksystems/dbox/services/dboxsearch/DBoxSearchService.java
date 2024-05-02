package com.dongkuksystems.dbox.services.dboxsearch;

import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.type.folder.FolderDaoImpl;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.DBoxSearch;
import com.dongkuksystems.dbox.models.dto.etc.SimpleUserDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.doc.DocLink;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.services.folder.FolderServiceImpl;
import com.dongkuksystems.dbox.utils.RestTemplateUtils;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDaoImpl;

import io.swagger.annotations.ApiModelProperty;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DBoxSearchService {


    public final String SEARCH_URL = "http://searchdev.dongkuk.com:8082/dbox/getDboxElasticQueryListHttp.do";

    @Autowired
    private FolderDaoImpl folderDao;
    @Autowired
    private DocDaoImpl docDao;
    @Autowired
    private FolderServiceImpl folderService;

    @Autowired
    private RestTemplateUtils restTemplateUtils;

    public List<DataDetailDto> getDataFromDBoxSearch(DBoxSearch dBoxSearch) throws Exception {

        String url = getFullUrl(dBoxSearch);

        Map<String, List<Map<String, String>>> body = getBody(url);

        boolean noResults = (body == null || CollectionUtils.isEmpty(body.get("resultList")));
        if (noResults) {
            // TODO or something.
            return new ArrayList<>();
        }
        return body.get("resultList").stream().map(item -> getDataDetailDto(item, dBoxSearch.getUserId())).collect(Collectors.toList());
    }

    private Map<String, List<Map<String, String>>> getBody(String url) throws Exception {
        ResponseEntity<Map<String, List<Map<String, String>>>> response = getResponse(url);
        HttpStatus statusCode = response.getStatusCode();

        if (HttpStatus.OK != statusCode) {
            throw new RuntimeException(statusCode.getReasonPhrase());
        }
        return response.getBody();
    }

    private DataDetailDto getDataDetailDto(Map<String, String> item, String userId) {
        DocDetailDto docDetailDto = getDocDetailDto(item, userId);
        return DataDetailDto.builder()
                .dataType(DboxObjectType.DOCUMENT.getValue())
                .doc(docDetailDto).build();
    }

    private ResponseEntity<Map<String, List<Map<String, String>>>> getResponse(String fullUrl) throws Exception {
        return restTemplateUtils.get(fullUrl, getHeader(), new ParameterizedTypeReference<Map<String, List<Map<String, String>>>>() {});
    }

    private String getFullUrl(DBoxSearch dBoxSearch) throws Exception {
        MultiValueMap<String, String> params = getParams(dBoxSearch);
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(SEARCH_URL).queryParams(params).build();
        return uriComponents.toString();
    }

    private String getDSearchFullPath(DBoxSearch dBoxSearch) throws Exception {

        String folId = dBoxSearch.getFolderCode();
        String folPath = folderDao.selectDsearchFullPath(folId, dBoxSearch.getFolderType(), dBoxSearch.getDeptCode());
        return folderDao.selectDsearchFullPath(folId, dBoxSearch.getFolderType(), dBoxSearch.getDeptCode());
    }

    private MultiValueMap<String, String> getParams(DBoxSearch dBoxSearch) throws Exception {
        return new LinkedMultiValueMap<String, String>(){
            {
                add("encUserId", dBoxSearch.getUserId());
                add("searchKeyword", getEncoded(dBoxSearch.getSearchName()));
                add("area_path", getEncoded(getDSearchFullPath(dBoxSearch)));
                add("searchIndices", "dkg_dbox");
            }
        };
    }

    private String getFolderId(String folderCode) throws Exception {
        if (StringUtils.isNotEmpty(folderCode)) {
            Folder folder = folderService.selectOne(folderCode).orElse(null);
            if (!Objects.isNull(folder)) {
                return folder.getUPrCode();
            }
        }
        return null;
    }

    private String getEncoded(String searchName) throws UnsupportedEncodingException {
        return URLEncoder.encode(searchName, "UTF-8");
    }

    private HttpHeaders getHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Type", "application/json;utf-8");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        return headers;
    }

    private String getDocContentFrom(Map<String, String> result) {
        String contents = result.get("contents");
        if (StringUtils.isEmpty(contents)) {
            return result.get("subject");
        }
        byte[] bytes = contents.getBytes();
        String ellipsis = "...";
        int max = 300;
        if (bytes.length > max) {
            return new String(bytes, 0, max - ellipsis.length()) + ellipsis;
        }
        return contents;
    }

    private String getSecLevelNameFrom(Map<String, String> result) {
        try {
            String secLevel = result.get("sec_level");
            SecLevelCode byValue = SecLevelCode.findByValue(secLevel);
            return byValue.getDesc();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getDocStatusNameFrom(Map<String, String> result) {
        String docStatus = result.get("doc_status");
        boolean isLive = DocStatus.LIVE.getValue().equals(docStatus);
        boolean isClosed = DocStatus.CLOSED.getValue().equals(docStatus);
        return isLive ? "Live" : isClosed ? "Closed" : "";
    }

    private String getDatetime(Map<String, String> result, String name) {
        String timestamp = result.get(name);
        if (StringUtils.isEmpty(timestamp) || timestamp.contains("1753")) {
            return "-";
        }
        return timestamp;
    }
    

    private DocDetailDto getDocDetailDto(Map<String, String> result, String userId) {
      
 
        String authorityUsers = result.get("authority_users");
        boolean hasAuthority = StringUtils.isNotEmpty(authorityUsers) && authorityUsers.contains(userId);
        int maxLevel = hasAuthority ? 7 : 2;

        SimpleUserDto regUserDetail = SimpleUserDto.builder().displayName(result.get("create_user_nm")).build();
        VUser closerDetail = VUser.builder().displayName(result.get("close_user_id")).build();
        
        DocLinkDto linkFileDataDto = null;
        
        if(result.get("contents_id").substring(0,6).equals("DBOX_L")) {
            linkFileDataDto = DocLinkDto.builder()
                .rObjectId(result.get("contents_id").substring(7))
                .build();
            linkFileDataDto =  docDao.selectDSearchLinkList(linkFileDataDto);   
        }

        String updateDate = getDatetime(result, "update_timestamp");
        String closeDate = getDatetime(result, "close_timestamp");

        return DocDetailDto.builder()
                .rObjectId(result.get("indexKey").substring(7))     // 파일 ID
                .uDocKey(result.get("contents_id").substring(7))
                .objectName(result.get("subject"))                  // 파일 제목
                .title(result.get("subject"))                       // 파일 제목 + 확장자
                .regUserDetail(regUserDetail)                       // 작성자
                .uFileExt(result.get("extension"))                  // 확장자
                .uSecLevel(result.get("sec_level"))                 // 권한
                .uDocStatus(result.get("doc_status"))               // 문서 상태
                .docStatusName(getDocStatusNameFrom(result))
                .secLevelName(getSecLevelNameFrom(result))
                .createTime(result.get("create_timestamp"))         // 생성일
                .docContent(getDocContentFrom(result))
                .uFolderPath(result.get("navi_path"))
                .updateTime(updateDate)    // 수정일
                .maxLevel(maxLevel)
                .readable(hasAuthority)
                .rContentSize(result.get("doc_size"))
                .uDocTag(result.get("tag"))
                .closerDetail(closerDetail)
                .uclosedDate(closeDate)                             // 만료일
                .lastVersion(result.get("version"))
                .approval_user_id(result.get("approval_user_id"))
                .approval_link(result.get("approval_link").trim())
                .uDocClass(result.get("category"))
                .docLink(linkFileDataDto)
                .build();
    }
    

  }
