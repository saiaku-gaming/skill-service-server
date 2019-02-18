package com.valhallagame.traitserviceserver.rabbitmq;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.service.TraitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class NotificationConsumer {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	TraitService traitService;

	@RabbitListener(queues = { "#{traitCharacterDeleteQueue.name}" })
	public void receiveCharacterDelete(NotificationMessage message) {
		logger.info("Received character delete notification with message {}", message);
		String characterName = (String) message.getData().get("characterName");
		List<Trait> traits = traitService.getTraits(characterName);
		for (Trait trait : traits) {
			traitService.deleteTrait(trait);
		}
	}

	@RabbitListener(queues = { "#{traitFeatAddQueue.name}" })
	public void receiveFeatAdd(NotificationMessage message) throws IOException {
		logger.info("Received fead add notification with message: {}", message);
		String featName = (String) message.getData().get("feat");
		String characterName = (String) message.getData().get("characterName");

		traitService.handleFeatAdding(characterName, featName);
	}
}
