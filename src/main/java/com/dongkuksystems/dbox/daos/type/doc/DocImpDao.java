package com.dongkuksystems.dbox.daos.type.doc;

import java.util.Optional;

import com.dongkuksystems.dbox.models.type.doc.DocImp;

public interface DocImpDao {
  public Optional<DocImp> selectOne(String rObjectId);
}
