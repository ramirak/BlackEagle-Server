package com.framework.logic.converters;

import java.util.Map;

public interface JsonConverter {

	public String mapToJSON(Map<String, Object> value);

	public Map<String, Object> JSONToMap (String json);

}
