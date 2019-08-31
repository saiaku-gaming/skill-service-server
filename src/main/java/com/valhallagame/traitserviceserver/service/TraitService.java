package com.valhallagame.traitserviceserver.service;

import com.valhallagame.characterserviceclient.CharacterServiceClient;
import com.valhallagame.characterserviceclient.model.CharacterData;
import com.valhallagame.common.RestResponse;
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.common.rabbitmq.RabbitSender;
import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.traitserviceclient.message.AttributeType;
import com.valhallagame.traitserviceclient.message.TraitType;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.repository.TraitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TraitService {

	private static final Logger logger = LoggerFactory.getLogger(TraitService.class);

	@Autowired
	private RabbitSender rabbitSender;

	@Autowired
	private TraitRepository traitRepository;

	@Autowired
	private CharacterServiceClient characterServiceClient;

	// Use addTrait to add or other methods to change.
	private Trait saveTrait(Trait trait) {
		logger.info("Saving trait {}", trait);
		return traitRepository.save(trait);
	}

	public void deleteTrait(Trait trait) {
		logger.info("Deleting trait {}", trait);
		traitRepository.delete(trait);
	}

	public List<Trait> getTraits(String characterName) {
		logger.info("Getting traits for {}", characterName);
		return traitRepository.findByCharacterName(characterName);
	}

	public void handleFeatAdding(String characterName, String featName) throws IOException {
		logger.info("Handle Feat Adding characterName: {}, featName: {}", characterName, featName);
		if (FeatName.valueOf(featName) == FeatName.MISSVEDEN_THE_CHIEFTAINS_DEMISE) {
			unlockTrait(new Trait(TraitType.SOUL_TRANSFUSION, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.MISSVEDEN_DENIED) {
			unlockTrait(new Trait(TraitType.PETRIFY, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.MISSVEDEN_TREADING_WITH_GREAT_CARE) {
			unlockTrait(new Trait(TraitType.RECOVER, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.MISSVEDEN_A_CRYSTAL_CLEAR_MYSTERY) {
			unlockTrait(new Trait(TraitType.FROST_ARROWS, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.FREDSTORP_THIEF_OF_THIEVES) {
			unlockTrait(new Trait(TraitType.FIERY_PURGE, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.FREDSTORP_ANORECTIC) {
			unlockTrait(new Trait(TraitType.SEIDHRING, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.FREDSTORP_NEVER_BEEN_BETTER) {
			unlockTrait(new Trait(TraitType.FRIGGS_INTERVENTION, characterName));
		} else if (FeatName.valueOf(featName) == FeatName.FREDSTORP_EXTRACTOR) {
			unlockTrait(new Trait(TraitType.SHIELD_BASH, characterName));
		} else {
			logger.info("Nothing to do for feat adding {} with {}", characterName, featName);
		}
	}

	public boolean hasTraitUnlocked(TraitType traitType, String characterName) {
		logger.info("Has {} unlocked trait [}", characterName, traitType);
		return getTraits(characterName).stream().map(Trait::getName).anyMatch(t -> traitType.name().equals(t));
	}

	public void lockTrait(Trait trait) throws IOException {
		logger.info("Locking trait {}", trait);
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
			rabbitSender.sendMessage(RabbitMQRouting.Exchange.TRAIT, RabbitMQRouting.Trait.LOCK.name(),
					message);
		}
	}

	public void unlockTrait(Trait trait) throws IOException {
		logger.info("Unlocking trait {}", trait);
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
			rabbitSender.sendMessage(RabbitMQRouting.Exchange.TRAIT, RabbitMQRouting.Trait.UNLOCK.name(),
					message);
		}
	}

	public Trait skillTrait(Trait trait, AttributeType selectedAttribute, Integer position) {
		logger.info("Skilling trait {} attribute {} position {}", trait, selectedAttribute, position);
		trait.setClaimed(true);
		trait.setSelectedAttribute(selectedAttribute.name());
		trait.setPosition(position == null ? -1 : position);
		trait.setSpecialization(-1);
		trait.setSpecializationPosition(-1);
		return saveTrait(trait);
	}

	public Trait unskillTrait(Trait trait) {
		logger.info("Unskilling trait {}", trait);
		trait.setClaimed(false);
		trait.setSelectedAttribute(null);
		return saveTrait(trait);
	}

	public Trait specializeTrait(Trait trait, Integer specialization, Integer position) {
		logger.info("Specializing trait {} specialization {} position {}", trait, specialization, position);
		trait.setSpecialization(specialization);
		trait.setSpecializationPosition(position);
		return saveTrait(trait);
	}

	public Trait unspecializeTrait(Trait trait) {
		logger.info("Unspecializing trait {}", trait);
		trait.setSpecialization(-1);
		trait.setSpecializationPosition(-1);
		return saveTrait(trait);
	}

	public Optional<Trait> getUnlockedTrait(String characterName, TraitType traitType) {
		logger.info("Getting unlocked traits for {} trait type {}", characterName, traitType);
		return traitRepository.findByCharacterNameAndName(characterName, traitType.name());
	}
}
