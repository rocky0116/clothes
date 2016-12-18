package com.zujihu.opencv;

public class OpenCvException extends Exception {

	private static final long	serialVersionUID	= -7099648699742859441L;

	public OpenCvException() {
	}

	public OpenCvException(String detailMessage) {
		super(detailMessage);
	}

	public OpenCvException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public OpenCvException(Throwable throwable) {
		super(throwable);
	}
}
