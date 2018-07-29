package com.valhallagame.traitserviceserver.service;

import com.valhallagame.characterserviceclient.CharacterServiceClient;
import com.valhallagame.characterserviceclient.model.CharacterData;
import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.traitserviceclient.message.AttributeType;
import com.valhallagame.traitserviceclient.message.TraitType;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.repository.TraitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TraitService {

	private static final Logger logger = LoggerFactory.getLogger(TraitService.class);

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private TraitRepository traitRepository;

	@Autowired
	private CharacterServiceClient characterServiceClient;

	// Use addTrait to add or other methods to change.
	private Trait saveTrait(Trait trait) {
		return traitRepository.save(trait);
	}

	public void deleteTrait(Trait trait) {
		traitRepository.delete(trait);
	}

	public List<Trait> getTraits(String characterName) {
		return traitRepository.findByCharacterName(characterName);
	}

	public void handleFeatAdding(String characterName, String featName) throws IOException {
		logger.info("Handle Feat Adding characterName: {}, featName: {}", characterName, featName);
		switch (FeatName.valueOf(featName)) {
		case MISSVEDEN_THE_CHIEFTAINS_DEMISE:
			unlockTrait(new Trait(TraitType.TOURETTES, characterName));
			unlockTrait(new Trait(TraitType.PARANOIA, characterName));
		case TRAINING_EFFICIENCY:
		default:
			logger.info("No can do!");
			break;
		}
	}

	public boolean hasTraitUnlocked(TraitType traitType, String characterName) {
		return getTraits(characterName).stream().map(Trait::getName).anyMatch(t -> traitType.name().equals(t));
	}

	public void lockTrait(Trait trait) throws IOException {
		if (hasTraitUnlocked(trait.getTraitType(), trait.getCharacterName())) {

			trait = traitRepository.findByCharacterNameAndName(trait.getCharacterName(), trait.getName()).orElse(null);
			traitRepository.delete(trait);

			RestResponse<CharacterData> characterResp = characterServiceClient.getCharacter(trait.getCharacterName());
			Optional<CharacterData> characterOpt = characterResp.get();
			if (!characterOpt.isPresent()) {
				return;
			}

			NotificationMessage message = new NotificationMessage(characterOpt.get().getOwnerUsername(),
					"trait locked");
			message.addData("traitName", trait.getName());
			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.TRAIT.name(), RabbitMQRouting.Trait.LOCK.name(),
					message);
		}
	}

	public void unlockTrait(Trait trait) throws IOException {
		if (!hasTraitUnlocked(trait.getTraitType(), trait.getCharacterName())) {
			saveTrait(trait);

			RestResponse<CharacterData> characterResp = characterServiceClient.getCharacter(trait.getCharacterName());
			Optional<CharacterData> characterOpt = characterResp.get();
			if (!characterOpt.isPresent()) {
				return;
			}

			NotificationMessage message = new NotificationMessage(characterOpt.get().getOwnerUsername(),
					"trait unlocked");
			message.addData("traitName", trait.getName());
			rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.TRAIT.name(), RabbitMQRouting.Trait.UNLOCK.name(),
					message);
		}
	}

	public Trait skillTrait(Trait trait, AttributeType selectedAttribute) {
		trait.setClaimed(true);
		trait.setSelectedAttribute(selectedAttribute.name());
		return saveTrait(trait);
	}

	public Trait unskillTrait(Trait trait) {
		trait.setClaimed(false);
		trait.setSelectedAttribute(null);
		return saveTrait(trait);
	}

	public Optional<Trait> getUnlockedTrait(String characterName, TraitType traitType) {
		return traitRepository.findByCharacterNameAndName(characterName, traitType.name());
	}
}
