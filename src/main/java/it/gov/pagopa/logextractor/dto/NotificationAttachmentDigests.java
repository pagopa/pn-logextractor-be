package it.gov.pagopa.logextractor.dto;

import lombok.*;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationAttachmentDigests {
    private String sha256;
}
