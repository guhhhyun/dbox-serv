package com.dongkuksystems.dbox.models.dto;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AbstractCommonDto {
	@JsonIgnore
	private Set<String> assignParameters = new HashSet<String>();

	public void removeAssignParameters(String parameter){
		assignParameters.remove(parameter);
	}

	public void addAssignParameters(String parameter){
		assignParameters.add(parameter);
	}
	
	public Set<String> getAssignParameters() {
		return assignParameters;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.JSON_STYLE);
	}
}
