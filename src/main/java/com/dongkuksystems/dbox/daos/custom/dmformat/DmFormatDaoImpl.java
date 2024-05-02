package com.dongkuksystems.dbox.daos.custom.dmformat;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat; 

@Primary
@Repository
public class DmFormatDaoImpl implements DmFormatDao {
  private DmFormatMapper dmFormatMapper;

  public DmFormatDaoImpl(DmFormatMapper dmFormatMapper) {
    this.dmFormatMapper = dmFormatMapper;
  }

  @Override
  public List<DmFormat> selectAll() {
    return dmFormatMapper.selectAll();
  }
}
