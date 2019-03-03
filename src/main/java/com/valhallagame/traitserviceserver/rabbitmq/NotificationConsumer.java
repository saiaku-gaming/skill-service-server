package com.valhallagame.traitserviceserver.rabbitmq;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.service.TraitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class NotificationConsumer {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	private TraitService traitService;

	@Value("${spring.application.name}")
	private String appName;

	@RabbitListener(queues = { "#{traitCharacterDeleteQueue.name}" })
	public void receiveCharacterDelete(NotificationMessage message) {
		MDC.put("service_name", appName);
		MDC.put("request_id", message.getData().get("requestId") != null ? (String)message.getData().get("requestId") : UUID.randomUUID().toString());

		logger.info("Received character delete notification with message {}", message);

		try {
			String characterName = (String) message.getData().get("characterName");
			List<Trait> traits = traitService.getTraits(characterName);
			for (Trait trait : traits) {
				traitService.deleteTrait(trait);
			}
		} finally {
			MDC.clear();
		}
	}

	@RabbitListener(queues = { "#{traitFeatAddQueue.name}" })
	public void receiveFeatAdd(NotificationMessage message) throws IOException {
		MDC.put("service_name", appName);
		MDC.put("request_id", message.getData().get("requestId") != null ? (String)message.getData().get("requestId") : UUID.randomUUID().toString());

		logger.info("Received fead add notification with message: {}", message);

		try {
			String featName = (String) message.getData().get("feat");
			String characterName = (String) message.getData().get("characterName");

			traitService.handleFeatAdding(characterName, featName);
		} finally {
			MDC.clear();
		}
	}
}
