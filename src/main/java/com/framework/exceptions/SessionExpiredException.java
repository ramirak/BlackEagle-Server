package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.GATEWAY_TIMEOUT)
public class SessionExpiredException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public SessionExpiredException() {
	}

	public SessionExpiredException(String message) {
		super(message);
	}

	public SessionExpiredException(Throwable cause) {
		super(cause);
	}

	public SessionExpiredException(String message, Throwable cause) {
		super(message, cause);
	}
}
