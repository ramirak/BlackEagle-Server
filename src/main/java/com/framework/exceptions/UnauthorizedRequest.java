package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.UNAUTHORIZED)
public class UnauthorizedRequest extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnauthorizedRequest() {
	}

	public UnauthorizedRequest(String message) {
		super(message);
	}

	public UnauthorizedRequest(Throwable cause) {
		super(cause);
	}

	public UnauthorizedRequest(String message, Throwable cause) {
		super(message, cause);
	}
}
