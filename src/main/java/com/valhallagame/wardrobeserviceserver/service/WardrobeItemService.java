package com.valhallagame.wardrobeserviceserver.service;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.wardrobeserviceserver.model.WardrobeItem;
import com.valhallagame.wardrobeserviceserver.repository.WardrobeItemRepository;

@Service
public class WardrobeItemService {

	@Autowired
	private WardrobeItemRepository wardrobeItemRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	public WardrobeItem saveWardrobeItem(WardrobeItem wardrobeItem) {
		return wardrobeItemRepository.save(wardrobeItem);
	}

	public void deleteWardrobeItem(WardrobeItem wardrobeItem) {
		wardrobeItemRepository.delete(wardrobeItem);
	}

	public List<WardrobeItem> getWardrobeItems(String characterName) {
		return wardrobeItemRepository.findByCharacterOwner(characterName);
	}

	public void handleFeatAdding(String characterName, String featName) {
		switch (FeatName.valueOf(featName)) {
		case EINHARJER_SLAYER:
			WardrobeItem wardrobeItem = new WardrobeItem();
			wardrobeItem.setCharacterOwner(characterName);
			wardrobeItem.setName(com.valhallagame.wardrobeserviceclient.message.WardrobeItem.MAIL_ARMOR.name());
			saveWardrobeItem(wardrobeItem);
			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.WARDROBE.name(),
					RabbitMQRouting.Wardrobe.ADD_WARDROBE_ITEM.name(),
					new NotificationMessage(characterName, "wardrobe item added"));
			return;
		default:
			break;
		}
	}
}
