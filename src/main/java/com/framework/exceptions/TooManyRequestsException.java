package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TooManyRequestsException() {
	}

	public TooManyRequestsException(String message) {
		super(message);
	}

	public TooManyRequestsException(Throwable cause) {
		super(cause);
	}

	public TooManyRequestsException(String message, Throwable cause) {
		super(message, cause);
	}
}
