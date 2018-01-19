package com.valhallagame.wardrobeserviceserver.rabbitmq;

import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.wardrobeserviceserver.model.WardrobeItem;
import com.valhallagame.wardrobeserviceserver.service.WardrobeItemService;

@Component
public class NotificationConsumer {

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
		String featName = (String) message.getData().get("feat");
		String characterName = (String) message.getData().get("characterName");

		wardrobeItemService.handleFeatAdding(characterName, featName);
	}
}
