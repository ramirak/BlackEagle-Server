package com.framework.logic.converters;

public interface EntityConverter<E, B> {
	public B toBoundary(E entity);

	public E fromBoundary(B boundary);
}