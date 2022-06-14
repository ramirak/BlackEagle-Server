package com.framework.logic.converters;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonConverterImplementation implements JsonConverter {
	private ObjectMapper jackson;

	@Autowired
	public void setJackson(ObjectMapper jackson) {
		this.jackson = jackson;
	}

	@Override
	public String mapToJSON(Map<String, Object> value) {
		try {
			return this.jackson.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> JSONToMap(String json) {
		try {
			return this.jackson.readValue(json, Map.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String setToJSON(Set<Object> value) {
		try {
			return this.jackson.writeValueAsString(value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<Object> JSONToSet(String json) {
		try {
			return this.jackson.readValue(json, Set.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
