package com.dongkuksystems.dbox.models.dto.type.upload;


import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.apache.commons.lang3.StringUtils.defaultIfEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttachedFile {

    private String originalFileName;
    private String tmpFileName;

    private String fileExtention;
    private String contentType;
    private String dContentType;

//    private MultipartFile mFile;
    private byte[] bytes;

    public AttachedFile(String originalFileName, String contentType, MultipartFile mFile, String basePath, String defaultExtension) throws IOException {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
//        this.mFile = mFile;
        setExtension(defaultExtension);
        
        System.out.println("전: this.tmpFileName :" + this.tmpFileName);

        this.tmpFileName = this.randomName(basePath+"upload", defaultExtension);
        
        System.out.println("후: this.tmpFileName :" + this.tmpFileName);

        this.saveFile(mFile, basePath);
    }
    
    public AttachedFile(String originalFileName, String contentType, byte[] bytes) {
        this.originalFileName = originalFileName;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    private static boolean verify(MultipartFile multipartFile) {
        if (multipartFile != null && multipartFile.getSize() > 0 && multipartFile.getOriginalFilename() != null) {
            String contentType = multipartFile.getContentType();
            // 이미지인 경우만 처리
            return isNotEmpty(contentType);
//            return isNotEmpty(contentType) && contentType.toLowerCase().startsWith("image");
        }
        return false;
    }

    public static AttachedFile toAttachedFile(MultipartFile multipartFile, String basePath, String defaultExtension) throws IOException {
      if (verify(multipartFile)) {
        return new AttachedFile(multipartFile.getOriginalFilename(), multipartFile.getContentType(), multipartFile, basePath, defaultExtension);
      } else {
        return null;
      }
      
//        return verify(multipartFile)
//                ? new AttachedFile(multipartFile.getOriginalFilename(), multipartFile.getContentType())
////                ? new AttachedFile(multipartFile.getOriginalFilename(), multipartFile.getContentType(), multipartFile.getBytes())
//                : null;
    }

    public void setExtension(String defaultExtension) {
      this.fileExtention = defaultIfEmpty(getExtension(originalFileName), defaultExtension);
//        return defaultIfEmpty(getExtension(originalFileName), defaultExtension);
    }

    public String randomName(String defaultExtension) {
        return randomName(null, defaultExtension);
    }

    public String randomName(String basePath, String defaultExtension) {
    	System.out.println("random :" + basePath);
        String name = isEmpty(basePath) ? UUID.randomUUID().toString() : basePath + File.separator + UUID.randomUUID().toString();
        
        return name + "." + this.fileExtention;
    }

    public InputStream inputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public long length() {
        return bytes.length;
    }

    public File multipartToFile(MultipartFile file) throws IllegalStateException, IOException {
      File rst = new File(file.getOriginalFilename());
      file.transferTo(rst);
      return rst;
    }
    
    public void saveFile(MultipartFile file, String directoryPath) throws IOException {
    	
/*
      Path directory = Paths.get(directoryPath).toAbsolutePath().normalize();

      // 파일을 저장할 경로를 Path 객체로 받는다.
      Path targetPath = directory.resolve(this.tmpFileName).normalize();

      // 파일이 이미 존재하는지 확인하여 존재한다면 오류를 발생하고 없다면 저장한다.
      checkArgument(!Files.exists(targetPath), this.tmpFileName + " File alerdy exists.");
//    file.transferTo(targetPath);
      file.transferTo(targetPath.toFile());    	
*/    	
      // 저장할 디렉토리 경로
      //System.out.println("경로 (directoryPath)=" +directoryPath);    	
      File dir = new File("upload");
      
      // 경로가 없을 경우 경로 강제 생성
      FileUtils.forceMkdir(dir);
      System.out.println("경로(dir)=" +dir);
      // 저장할 파일
      
      //file.getInputStream()
      //System.out.println("메인(1): this.tmpFileName :" + this.tmpFileName);
      
      File newFile = new File(this.tmpFileName);
      
      //System.out.println("메인(2): this.tmpFileName :" + this.tmpFileName);
      //System.out.println("메인(3): file :" + newFile.getAbsolutePath());
      // 파일이 이미 존재하는지 확인하여 존재한다면 오류를 발생하고 없다면 저장한다.
      checkArgument(!newFile.exists(), this.tmpFileName + " File alerdy exists.");
      
      //System.out.println("=========="+ directoryPath);
      Path path = Paths.get(directoryPath).toAbsolutePath();
      //System.out.println("=========="+ path);
      //file.transferTo(path.toFile());

      FileOutputStream fos = new FileOutputStream(newFile);
      fos.write(file.getBytes());
      fos.close();
      
      // MultipartFile을 해당 파일로 복사
      //file.transferTo(newFile);
      
    }
    public boolean deleteFile() {
      File file = new File(this.tmpFileName);
      if (file.exists()) {
        return file.delete();
      }
      return true;
    }
    public void setDcmtContentType(List<DmFormat> source) {
      DmFormat rst = null;
      rst = source.stream().filter(s -> this.contentType.equals(s.getMimeType())).findFirst().orElse(null);
      if (rst == null) source.stream().filter(s -> this.fileExtention.equals(s.getDosExtension())).findFirst().orElse(null);
      if (rst == null) source.stream().filter(s -> this.fileExtention.equals(s.getMacType())).findFirst().orElse(null);
      if (rst == null) this.dContentType = "unknown";
      else this.dContentType = rst.getName();
    }
}
