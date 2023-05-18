package it.gov.pagopa.logextractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Message {

	private String from;
    private String text;
    private String time;
}
