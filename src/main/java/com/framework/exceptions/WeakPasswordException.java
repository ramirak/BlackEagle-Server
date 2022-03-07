package com.framework.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE)
public class WeakPasswordException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public WeakPasswordException() {
	}

	public WeakPasswordException(String message) {
		super(message);
	}

	public WeakPasswordException(Throwable cause) {
		super(cause);
	}

	public WeakPasswordException(String message, Throwable cause) {
		super(message, cause);
	}
}
