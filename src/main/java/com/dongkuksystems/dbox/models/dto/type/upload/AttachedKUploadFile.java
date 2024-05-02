package com.dongkuksystems.dbox.models.dto.type.upload;

import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.errors.upload.UploadNameLengthException;
import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
@Data
@AllArgsConstructor
@Builder
public class AttachedKUploadFile {

  private String originalFileName;
  private String newFileLocation;
  private String fileNameOnly;

  private String fileExtention;
  private String contentType;
  private String dContentType;

  private String fileIndex;
  private String guid;
  private String guidExtension;

  @Builder.Default 
  private List<String> pathList = new ArrayList<>();
  
//    HttpServletRequest request = eventVo.getRequest(); //Request Value
//    HttpServletResponse response = eventVo.getResponse(); //Response Value
//    String strNewFileLocation = eventVo.getNewFileLocation(); //NewFileLocation Value
//    String strResponseFileServerPath = eventVo.getResponseFileServerPath(); //ResponseFileServerPath Value
//    String strCustomValue = eventVo.getCustomValue(); //CustomValue
//    String strFileIndex = eventVo.getFileIndex(); //FileIndex Value - 마지막 파일은 index 뒤에 z가 붙습니다.
//    String strOriginalFileName = eventVo.getOriginalFileName(); //Original File Name
//    String strGuid = eventVo.getGuid(); //Guid

  public AttachedKUploadFile(String strNewFileLocation, String strResponseFileServerPath,
      String strOriginalFileName, String strFileIndex, String strGuid) throws IOException, UploadNameLengthException {
    this.newFileLocation = strNewFileLocation;
    this.originalFileName = strOriginalFileName;
    this.fileIndex = strFileIndex;
    this.fileExtention = defaultIfEmpty(getExtension(this.originalFileName), Commons.DEFAULT_EXTENSION).toLowerCase();
    this.fileNameOnly = this.originalFileName.replace(".".concat(this.fileExtention), "");
    this.contentType = fileExtention;
    this.guid = strGuid;
    this.guidExtension = this.guid + "." + this.fileExtention;

    if (this.fileNameOnly.getBytes().length >= 240) {
      throw new UploadNameLengthException("폴더 및 파일명은 영문 240, 한글 80자 이내여야 합니다.");
    }
//    checkArgument(this.originalFileName.length() - this.fileExtention.length() <= 240, "파일명은 영문 240, 한글 80자 이내여야 합니다.");
  }

  public void setExtension(String defaultExtension) {
    this.fileExtention = defaultIfEmpty(getExtension(originalFileName), defaultExtension).toLowerCase();
  }

  public void setFileNames(String newName) {
    this.originalFileName = newName;
    this.fileExtention = defaultIfEmpty(getExtension(newName), Commons.DEFAULT_EXTENSION).toLowerCase();
    this.fileNameOnly = this.originalFileName.replace(".".concat(this.fileExtention), "");
  }
  
  public boolean deleteFile() {
    File file = new File(this.newFileLocation);
    if (file.exists()) {
      return file.delete();
    }
    return true;
  }

  public void setDcmtContentType(List<DmFormat> source) {
    DmFormat rst = null;
    rst = source.stream().filter(s -> this.fileExtention.equals(s.getMimeType())).findFirst().orElse(null);
    if (rst == null)
      rst = source.stream().filter(s -> this.fileExtention.equals(s.getDosExtension())).findFirst().orElse(null);
    if (rst == null)
      rst = source.stream().filter(s -> this.fileExtention.equals(s.getMacType())).findFirst().orElse(null);
    if (rst == null)
      this.dContentType = "unknown";
    else
      this.dContentType = rst.getName();
  }
  
  public void setFolderPath(String basePath) {
    if (!Objects.isNull(this.newFileLocation)) {
      String filePath = this.newFileLocation.substring(this.newFileLocation.indexOf(basePath) + basePath.length() + 9); // 8-> /2021/08
      this.pathList = Arrays.asList(filePath.split("/"));
    } 
  }
}
