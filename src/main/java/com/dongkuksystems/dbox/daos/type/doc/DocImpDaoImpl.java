package com.dongkuksystems.dbox.daos.type.doc;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.doc.DocImp;

@Primary
@Repository
public class DocImpDaoImpl implements DocImpDao {
	private DocImpMapper docImpMapper;

	public DocImpDaoImpl(DocImpMapper docImpMapper) {
		this.docImpMapper = docImpMapper;
	}

	@Override
	public Optional<DocImp> selectOne(String rObjectId) {
		return docImpMapper.selectOne(rObjectId);
	}
}
