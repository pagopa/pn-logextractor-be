package it.gov.pagopa.logextractor.rest;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import it.gov.pagopa.logextractor.dto.Message;

@Controller
public class WebsocketController {

	@MessageMapping("/chat")
	@SendTo("/topic/messages")
	public Message send(Message message) throws Exception {
	    String time = new SimpleDateFormat("HH:mm").format(new Date());
	    return new Message(message.getFrom(), message.getText(), time);
	}
}
