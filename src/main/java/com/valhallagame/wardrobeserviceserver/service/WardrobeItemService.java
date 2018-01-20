package com.valhallagame.wardrobeserviceserver.service;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.characterserviceclient.CharacterServiceClient;
import com.valhallagame.characterserviceclient.model.CharacterData;
import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.wardrobeserviceserver.model.WardrobeItem;
import com.valhallagame.wardrobeserviceserver.rabbitmq.NotificationConsumer;
import com.valhallagame.wardrobeserviceserver.repository.WardrobeItemRepository;

@Service
public class WardrobeItemService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	private WardrobeItemRepository wardrobeItemRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private CharacterServiceClient characterServiceClient;

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
		logger.info("Handle Feat Adding characterName: {}, featName: {}", characterName, featName);
		switch (FeatName.valueOf(featName)) {
		case EINHARJER_SLAYER:
			logger.info("Found Einharjer_Slayer in the switch");
			WardrobeItem wardrobeItem = new WardrobeItem();
			wardrobeItem.setCharacterOwner(characterName);
			wardrobeItem.setName(com.valhallagame.wardrobeserviceclient.message.WardrobeItem.MAIL_ARMOR.name());
			saveWardrobeItem(wardrobeItem);

			try {
				RestResponse<CharacterData> characterWithoutOwnerValidation = characterServiceClient
						.getCharacterWithoutOwnerValidation(characterName);
				if (characterWithoutOwnerValidation.get().isPresent()) {
					rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.WARDROBE.name(),
							RabbitMQRouting.Wardrobe.ADD_WARDROBE_ITEM.name(),
							new NotificationMessage(characterWithoutOwnerValidation.get().get().getOwnerUsername(),
									"wardrobe item added"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		default:
			logger.info("Went to default :(");
			break;
		}
	}
}
