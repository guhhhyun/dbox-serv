package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.template.TemplateService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "템플릿 APIs")
public class TemplateController extends AbstractCommonController {

  private final TemplateService templateService;

  public TemplateController(TemplateService templateService) {
    this.templateService = templateService;
  }

  @PostMapping(value = "/templates/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiOperation(value = "자료(문서) 업로드", notes = "파일 단건 업로드", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResult<String> templateUpload(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "파일") @RequestPart(required = true) MultipartFile file) throws Exception {

    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = templateService.createTemplate(userSession, AttachedFile.toAttachedFile(file,
        MessageFormat.format(Commons.TMP_STORAGE_PATH, authentication.loginId), Commons.DEFAULT_EXTENSION));    
    return OK(rst);
  }

  @GetMapping(value = "/templates/{comOrgId}/select/{delStatus}")
  @ApiOperation(value = "템플릿 리스트 조회")
  public ApiResult<Object> getTemplateList(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String comOrgId, @PathVariable String delStatus) throws Exception {
    return OK(templateService.selectTemplates(comOrgId, delStatus));
  }

  @PostMapping(value = "/templates/{rObjectId}/post/{objectName}/{templateCode}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiOperation(value = "템플릿 업로드 ", notes = "템플릿 파일 수정", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResult<String> templateUpdate(@AuthenticationPrincipal JwtAuthentication authentication,HttpServletRequest request, 
      @PathVariable String rObjectId, @PathVariable String objectName,@PathVariable String templateCode,
      @ApiParam(value = "파일") @RequestPart(required = true) MultipartFile file) throws Exception {

    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class); 
    
    String path = request.getSession().getServletContext().getRealPath("");
    String rst = templateService.updateTemplate(userSession,
        AttachedFile.toAttachedFile(file, path, Commons.DEFAULT_EXTENSION),
//        MessageFormat.format(Commons.TMP_STORAGE_PATH, authentication.loginId), Commons.DEFAULT_EXTENSION),
        rObjectId, objectName, templateCode);
    return OK(rst);
  }

  @PatchMapping(value = "/templates/{rObjectId}/delete")
  @ApiOperation(value = "템플릿 삭제(삭제상태 수정)", notes = "템플릿 파일 삭제")
  public ApiResult<String> deleteTemplate(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = templateService.deleteTemplate(userSession, rObjectId);
    return OK(rst);
  }

  @PatchMapping(value = "/templates/{rObjectId}/nameUpdate/{objectName}")
  @ApiOperation(value = "템플릿 파일명 수정", notes = "템플릿 파일명 수정")
  public ApiResult<String> templateNameUpdate(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String objectName) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = templateService.templateNameUpdate(userSession, rObjectId, objectName);
    return OK(rst);
  }
}