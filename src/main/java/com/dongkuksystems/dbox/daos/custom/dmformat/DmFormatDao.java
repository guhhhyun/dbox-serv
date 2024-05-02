package com.dongkuksystems.dbox.daos.custom.dmformat;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat;

public interface DmFormatDao {
  public List<DmFormat> selectAll(); 
}
