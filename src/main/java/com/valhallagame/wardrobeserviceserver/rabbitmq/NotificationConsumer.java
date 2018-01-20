package com.valhallagame.wardrobeserviceserver.rabbitmq;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.wardrobeserviceserver.model.WardrobeItem;
import com.valhallagame.wardrobeserviceserver.service.WardrobeItemService;

@Component
public class NotificationConsumer {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	WardrobeItemService wardrobeItemService;

	@RabbitListener(queues = { "#{wardrobeCharacterDeleteQueue.name}" })
	public void receiveCharacterDelete(NotificationMessage message) {
		String characterName = (String) message.getData().get("characterName");
		List<WardrobeItem> wardrobeItems = wardrobeItemService.getWardrobeItems(characterName);
		for (WardrobeItem wardrobeItem : wardrobeItems) {
			wardrobeItemService.deleteWardrobeItem(wardrobeItem);
		}
	}

	@RabbitListener(queues = { "#{wardrobeFeatAddQueue.name}" })
	public void receiveFeatAdd(NotificationMessage message) {
		logger.info("Received fead add notification with message: {}", message.toString());
		String featName = (String) message.getData().get("feat");
		String characterName = (String) message.getData().get("characterName");

		wardrobeItemService.handleFeatAdding(characterName, featName);
	}
}
