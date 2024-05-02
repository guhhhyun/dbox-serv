package com.dongkuksystems.dbox.services.doc;

import java.util.Optional;

import com.dongkuksystems.dbox.models.type.doc.DocImp;

public interface DocImpService {
  Optional<DocImp> selectOne(String rObjectId) throws Exception;
}
