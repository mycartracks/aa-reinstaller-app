package com.slashidea.aareinstaller.exception;

public class DeleteApkException extends Exception {
	private static final long serialVersionUID = 1L;

	public DeleteApkException() {
		super();
	}

	public DeleteApkException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DeleteApkException(String detailMessage) {
		super(detailMessage);
	}

	public DeleteApkException(Throwable throwable) {
		super(throwable);
	}
}
