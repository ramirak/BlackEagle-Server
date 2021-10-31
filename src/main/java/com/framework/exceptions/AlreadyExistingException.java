package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.CONFLICT)
public class AlreadyExistingException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AlreadyExistingException() {
	}

	public AlreadyExistingException(String message) {
		super(message);
	}

	public AlreadyExistingException(Throwable cause) {
		super(cause);
	}

	public AlreadyExistingException(String message, Throwable cause) {
		super(message, cause);
	}
}
