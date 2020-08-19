package org.example.controller;

import com.google.gson.Gson;
import java.security.Principal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import org.example.dao.Message;
import org.example.dao.OutputMessage;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
public class MessageController {

  private Gson gson = new Gson();

  @MessageMapping("/chat")
  @SendTo("/topic/messages")
  public OutputMessage send(Message message) throws Exception {
    return new OutputMessage(
        message.getFrom(),
        message.getText(),
        DateTimeFormatter.ISO_TIME.format(OffsetDateTime.now()));
  }

  @MessageMapping("/message")
  @SendToUser("/queue/reply")
  public String processMessageFromClient(@Payload String message, Principal principal)
      throws Exception {
    return gson.fromJson(message, Map.class).get("name").toString();
  }

  @MessageExceptionHandler
  @SendToUser("/queue/errors")
  public String handleException(Throwable exception) {
    return exception.getMessage();
  }
}
