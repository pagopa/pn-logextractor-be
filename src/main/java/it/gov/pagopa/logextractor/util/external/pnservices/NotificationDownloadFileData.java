package it.gov.pagopa.logextractor.util.external.pnservices;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationDownloadFileData {
    String fileCategory;
    String key;
    String downloadUrl;

    public NotificationDownloadFileData(String fileCategory, String key) {
        this.fileCategory = fileCategory;
        this.key = key;
    }

    public NotificationDownloadFileData(String fileCategory, String key, String downloadUrl) {
        this.fileCategory = fileCategory;
        this.key = key;
        this.downloadUrl = downloadUrl;
    }
}
