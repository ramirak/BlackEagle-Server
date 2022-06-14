package com.framework.logic.converters;

import java.util.Map;
import java.util.Set;

public interface JsonConverter {
	
	public String mapToJSON(Map<String, Object> value);

	public Map<String, Object> JSONToMap(String json);

	public String setToJSON(Set<Object> value);

	public Set<Object> JSONToSet(String json);
}
