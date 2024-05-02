package com.dongkuksystems.dbox.models.dto.type.data;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.google.common.base.Objects;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class RegistDataDto {
  @ApiModelProperty(value = "자료 타입", notes = "F:folder, T:template, P:project folder, R -> DataObjecTType enum확인", required = true)
  private String dateType;
  @ApiModelProperty(value = "폴더 타입", notes = "DFO:부서, DWY:부서 전자결재 년도, PCL:프로젝트분류, PFO:프로젝트, RCL:연구과제분류, RFO:연구과제")
  private String folderType;
  @ApiModelProperty(value = "자료 이름", required = true)
  private String dataName;
  @ApiModelProperty(value = "상위 함 타입", example = "P:프로젝트, R:연구과제, D:부서, F:folder", required = true)
  private String hamType;
  
  @ApiModelProperty(value = "상위 id", notes = "hamType이 F가 아닐경우 project code, research code, orgId", hidden = true)
  private String upObjectId;
  @ApiModelProperty(value = "상위 함 id", example = "프로젝트, 연구과제 아이디, 부서아이디", hidden = true)
  private String hamId;
  @Builder.Default
  @ApiModelProperty(value = "보안등급, 전달용", hidden = true)
  private String secLevel = "T";
  @ApiModelProperty(value = "템플릿 file code")
  private String templateType;
  @ApiModelProperty(value = "템플릿 object Id", hidden = true)
  private String templateObjId;
  @ApiModelProperty(value = "함 info", example = "", hidden = true)
  private HamInfoResult hamInfo;
//  @Builder.Default 
//  @ApiModelProperty(value = "트랜잭션 유지여부", example = "", hidden = true)
//  private boolean transactionMa = false;
  @Builder.Default 
  @ApiModelProperty(value = "프로젝트/리서치 생성후 첫 폴더 생성일경우", example = "", hidden = true)
  private boolean isPrFirst = false;
  
  public RegistDataDto(String dateType, String folderType, String dataName, String hamType, String upObjectId, String secLevel,
      String hamId, String templateType, String templateObjId, HamInfoResult hamInfo, boolean isPrFirst) {
    checkNotNull(dateType, "dateType must be provided."); 
    checkNotNull(hamType, "hamType must be provided."); 
    checkNotNull(dataName, "dataName must be provided.");
    checkArgument(dataName.getBytes().length <= 240, "폴더 및 파일명은 영문 240, 한글 80자 이내여야 합니다.");
//    checkArgument(dataName.length() <= 255, "문서/폴더 이름은 전체 80자를 넘지 않도록 함 (한글 80자, 영문 255자)");
//    checkArgument(folderType != null && templateType != null, "folderType, templateFileCode 모두 null이 될 수 없습니다.");
    checkArgument(!Objects.equal(folderType, null) || !Objects.equal(templateType, null), "folderType, templateFileCode 모두 null이 될 수 없습니다.");
    
    this.dateType = dateType;
    this.folderType = folderType;
    this.dataName = dataName;
    this.hamType = hamType;
    this.upObjectId = upObjectId;
    this.secLevel = secLevel;
    this.hamId = hamId;
    this.templateType = templateType;
    this.templateObjId = templateObjId;
    this.hamInfo = hamInfo; 
    this.isPrFirst = isPrFirst;
  }
  
  public void addAdditionalInfo(String dataId) {
    if (checkRoot()) {
      this.hamId = dataId;
    } else {
      this.upObjectId = dataId;
    }
  }
  
  public boolean hamValidation() {
    if ("P".equals(this.hamInfo.getHamType()) || "R".equals(this.hamInfo.getHamType())) {
      return true;
    }
    return false;
//    if ("P".equals(this.hamType) || "R".equals(this.hamType)) {
//      return true;
//    }
//    return false;
  }
  
  public boolean checkRoot() {
    if (!"F".equals(this.hamType)) {
      return true;
    } else {
      return false;
    }
  }
  
  public static IDfDocument makeTemplate(IDfSession idfSession, RegistDataDto registDataDto, String secLevel,
      String extension) throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(registDataDto.getTemplateObjId()));
//  ByteArrayOutputStream out = idfDoc.getContent().;
//  customInputStream = new CustomInputStreamResource(idfDoc.getContent(), idfDoc.getContentSize(), registDataDto.getDataName());

    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);

//    IDfDocument idfNewDoc = (IDfDocument) idfSession.getObject(idfDoc.saveAsNew(true));
    IDfDocument idfNewDoc = (IDfDocument) idfSession.newObject(SysObjectType.DOC.getValue());
    // 파일 set
//    idfNewDoc.setFile(idfDoc.get);

    idfNewDoc.setContentType(idfDoc.getContentType());
    idfNewDoc.setContent(CommonUtils.convertByteInputStreamToOut(idfDoc.getContent()));
    // DCTM 폴더(화면에 보이는 부서 폴더 아님)
    idfNewDoc.link(s_DCTMFolderId);

//    String test = idfDoc.getString("u_file_ext");
//    throw new Exception("");

    // 문서명
    String dataName = registDataDto.getDataName();
    if (dataName.contains(extension)) {
      dataName = dataName.replaceAll(".".concat(extension), "");
    }
    idfNewDoc.setTitle(dataName.concat(".").concat(extension));
    idfNewDoc.setObjectName(dataName);
    // 포맷
    idfNewDoc.setContentType(idfDoc.getContentType());
    // 소유자 지정 : Docbase Owner로 지정
    idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
    // 템플릿 ACL 적용
//    idfNewDoc.setACLDomain(idfSession.getDocbaseOwnerName());
//    idfNewDoc.setACLName(registDataDto.getCabinetCode());
    
    // 업무 속성 지정
    if (registDataDto.hamValidation()) {
      idfNewDoc.setString("u_pr_code", registDataDto.getHamInfo().getMyCode()); 
      idfNewDoc.setString("u_pr_type", registDataDto.getHamInfo().getHamType()); 
    }
    if (!registDataDto.checkRoot()) {
      idfNewDoc.setString("u_fol_id", registDataDto.getUpObjectId()); // 부서폴더
    }

    idfNewDoc.setString("u_cabinet_code", registDataDto.getHamInfo().getUCabinetCode()); // 문서함코드
    idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
    idfNewDoc.setString("u_doc_flag", "S"); // 일반문서, M:마이그레이션문서
    idfNewDoc.setString("u_file_ext", extension); 
    idfNewDoc.setString("u_sec_level", secLevel); // 보안등급
    idfNewDoc.setString("u_doc_status", DocStatus.LIVE.getValue()); // live
    idfNewDoc.setString("u_reg_user", idfSession.getLoginUserName());
    idfNewDoc.setString("u_reg_date", (new DfTime()).toString());
    idfNewDoc.setString("u_update_date", (new DfTime()).toString());
    
    idfNewDoc.setString("u_last_editor", idfSession.getLoginUserName());
    //editors repeating
    idfNewDoc.appendString("u_editor", idfSession.getLoginUserName());
    return idfNewDoc;
  }
}
