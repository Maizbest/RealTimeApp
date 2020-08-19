package org.example;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import org.example.dao.Client;
import org.example.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Slf4j
@Component
public class SubscribeEventListener {

  @Autowired private SimpMessagingTemplate template;
  @Autowired private ClientRepository repository;
  private final ScheduledExecutorService scheduledExecutorService =
      Executors.newScheduledThreadPool(10);
  private final AtomicInteger count = new AtomicInteger(0);

  @EventListener(SessionConnectEvent.class)
  @Async
  public void connectedEvent(SessionConnectEvent event) {
    final SimpMessageHeaderAccessor accessor = getAccessor(event.getMessage());
    repository.save(new Client(accessor.getSessionId(), "user", "127.0.0.1"));
    template.convertAndSend("/topic/logins", repository.count());
  }

  @EventListener(SessionConnectedEvent.class)
  @Async
  public void connectedEvent(SessionConnectedEvent event) {
    final SimpMessageHeaderAccessor accessor = getAccessor(event.getMessage());
    repository.save(new Client(accessor.getSessionId(), "user", "127.0.0.1"));
    template.convertAndSend("/topic/logins", repository.count());
  }

  @EventListener(SessionSubscribeEvent.class)
  @Async
  public void subscribeEvent(SessionSubscribeEvent event) {
    final SimpMessageHeaderAccessor accessor = getAccessor(event.getMessage());
    if ("/topic/logins".equals(accessor.getDestination())) {
      template.convertAndSend("/topic/logins", repository.count());
    }
  }

  private SimpMessageHeaderAccessor getAccessor(Message<byte[]> message) {
    return SimpMessageHeaderAccessor.getAccessor(
        message.getHeaders(), SimpMessageHeaderAccessor.class);
  }

  @EventListener(SessionDisconnectEvent.class)
  @Async
  public void connectedEvent(SessionDisconnectEvent event) {
    repository.deleteById(getAccessor(event.getMessage()).getSessionId());
    template.convertAndSend("/topic/logins", repository.count());
  }
}
