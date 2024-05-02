package com.dongkuksystems.dbox.daos.type.doc;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.doc.DocImp; 

public interface DocImpMapper {
  public Optional<DocImp> selectOne(@Param("rObjectId") String rObjectId);
}
