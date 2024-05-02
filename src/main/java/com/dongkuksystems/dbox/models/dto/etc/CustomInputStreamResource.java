package com.dongkuksystems.dbox.models.dto.etc;

import java.io.InputStream;

import org.springframework.core.io.InputStreamResource;

public class CustomInputStreamResource extends InputStreamResource {
  private final String filename;
  private final long contentLength;
  private final String fileSecLevel;
  private final String docStatus;
  ;
  public CustomInputStreamResource(InputStream inputStream, long contentLength) {
      super(inputStream);
      this.filename = null;
      this.contentLength = contentLength;
      this.fileSecLevel = null;
      this.docStatus = null;
  }

  public CustomInputStreamResource(InputStream inputStream, long contentLength, String filename) {
      super(inputStream);
      this.filename = filename;
      this.contentLength = contentLength;
      this.fileSecLevel = null;
      this.docStatus = null;
  }

  public CustomInputStreamResource(InputStream inputStream, long contentLength, String filename, String fileSecLevel, String docStatus) {
      super(inputStream);
      this.filename = filename;
      this.contentLength = contentLength;
      this.fileSecLevel = fileSecLevel;
      this.docStatus = docStatus;
  }

  public String getFileSecLevel() {
    return this.fileSecLevel;
  }
  
  public String getDocStatus() {
    return this.docStatus;
  }
  
  @Override
  public String getFilename() {
      return filename;
  }

  @Override
  public long contentLength() {
      return contentLength;
  }
}
