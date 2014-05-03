package com.slashidea.aareinstaller.exception;

public class DownloadApkException extends Exception {

	private static final long serialVersionUID = 1L;

	public DownloadApkException() {
		super();
	}

	public DownloadApkException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public DownloadApkException(String detailMessage) {
		super(detailMessage);
	}

	public DownloadApkException(Throwable throwable) {
		super(throwable);
	}

}
