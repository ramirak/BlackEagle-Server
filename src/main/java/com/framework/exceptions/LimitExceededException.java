package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INSUFFICIENT_STORAGE)
public class LimitExceededException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LimitExceededException() {
	}

	public LimitExceededException(String message) {
		super(message);
	}

	public LimitExceededException(Throwable cause) {
		super(cause);
	}

	public LimitExceededException(String message, Throwable cause) {
		super(message, cause);
	}
}
