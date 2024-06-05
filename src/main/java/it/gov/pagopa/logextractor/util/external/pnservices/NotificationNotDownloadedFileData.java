package it.gov.pagopa.logextractor.util.external.pnservices;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationNotDownloadedFileData extends NotificationDownloadFileData{

	private String reason;
	
	public NotificationNotDownloadedFileData(NotificationDownloadFileData notificationDownloadFileData) {
		super(notificationDownloadFileData.getFileCategory()
				, notificationDownloadFileData.getKey()
				, notificationDownloadFileData.getDownloadUrl());
	}
}
