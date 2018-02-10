package com.valhallagame.traitserviceserver.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.traitserviceclient.message.TraitType;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.repository.TraitRepository;

@Service
public class TraitService {

	private static final Logger logger = LoggerFactory.getLogger(TraitService.class);

	@Autowired
	private TraitRepository traitRepository;

	public Trait saveTrait(Trait trait) {
		return traitRepository.save(trait);
	}

	public void deleteTrait(Trait trait) {
		traitRepository.delete(trait);
	}

	public List<Trait> getTraits(String characterName) {
		return traitRepository.findByCharacterOwner(characterName);
	}

	public void handleFeatAdding(String characterName, String featName) {
		logger.info("Handle Feat Adding characterName: {}, featName: {}", characterName, featName);
		switch (FeatName.valueOf(featName)) {
		default:
			logger.info("No can do!");
			break;
		}
	}

	public boolean saveTraitBarIndex(String characterName, TraitType traitType, int barIndex) {
		
		//Clean out old traits in that index (Should be done in db i guess.)
		List<Trait> traits = traitRepository.findByCharacterOwnerAndBarIndex(characterName, barIndex);
		traits.forEach(trait -> {
			if(trait.getBarIndex() != -1) {
				trait.setBarIndex(-1);
				saveTrait(trait);
			}
		});
		
		Trait trait = traitRepository.findByCharacterOwnerAndName(characterName, traitType.name());
		if(trait != null) {
			trait.setBarIndex(barIndex);
			saveTrait(trait);
			return true;
		}
		return false;
	}
}
