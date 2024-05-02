package com.dongkuksystems.dbox.daos.custom.dmformat;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat; 

public interface DmFormatMapper {
  public List<DmFormat> selectAll(); 
}
