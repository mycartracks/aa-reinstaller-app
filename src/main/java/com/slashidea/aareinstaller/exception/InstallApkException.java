package com.slashidea.aareinstaller.exception;

public class InstallApkException extends Exception {
	private static final long serialVersionUID = 1L;

	public InstallApkException() {
		super();
	}

	public InstallApkException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public InstallApkException(String detailMessage) {
		super(detailMessage);
	}

	public InstallApkException(Throwable throwable) {
		super(throwable);
	}
}
