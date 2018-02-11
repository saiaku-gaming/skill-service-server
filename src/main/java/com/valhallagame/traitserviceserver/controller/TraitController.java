package com.valhallagame.traitserviceserver.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.valhallagame.characterserviceclient.CharacterServiceClient;
import com.valhallagame.characterserviceclient.model.CharacterData;
import com.valhallagame.common.JS;
import com.valhallagame.common.RestResponse;
import com.valhallagame.traitserviceclient.message.UnlockTraitParameter;
import com.valhallagame.traitserviceclient.message.GetTraitsParameter;
import com.valhallagame.traitserviceclient.message.LockTraitParameter;
import com.valhallagame.traitserviceclient.message.SaveTraitBarIndexParameter;
import com.valhallagame.traitserviceclient.message.TraitBarItem;
import com.valhallagame.traitserviceclient.message.TraitData;
import com.valhallagame.traitserviceclient.message.TraitType;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.service.TraitService;

@Controller
@RequestMapping(path = "/v1/trait")
public class TraitController {

	@Autowired
	private TraitService traitService;
	
	@Autowired
	private CharacterServiceClient characterServiceClient;
	

	@RequestMapping(path = "/get-traits", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getTraits(@Valid @RequestBody GetTraitsParameter input) throws IOException {
		RestResponse<CharacterData> characterResp = characterServiceClient.getSelectedCharacter(input.getUsername());
		Optional<CharacterData> characterOpt = characterResp.get();
		if(!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();
		
		List<Trait> traits = traitService.getTraits(character.getCharacterName());
		TraitData traitData = convertToData(traits);
		
		return JS.message(HttpStatus.OK, traitData);
	}


	private TraitData convertToData(List<Trait> traits) {
		List<TraitBarItem> barItems = traits.stream().filter(t -> t.getBarIndex() >= 0)
				.map(t -> new TraitBarItem(TraitType.valueOf(t.getName()), t.getBarIndex()))
				.collect(Collectors.toList());
		
		List<TraitType> ownedTraits = traits.stream().map(t -> TraitType.valueOf(t.getName()))
				.collect(Collectors.toList());
		return new TraitData(ownedTraits, barItems);
	}

	@RequestMapping(path = "/save-trait-bar-index", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> saveTraitBarIndex(@Valid @RequestBody SaveTraitBarIndexParameter input) throws IOException {

		RestResponse<CharacterData> characterResp = characterServiceClient.getSelectedCharacter(input.getUsername());
		Optional<CharacterData> characterOpt = characterResp.get();
		if(!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();
		boolean success = traitService.saveTraitBarIndex(character.getCharacterName(), input.getName(), input.getBarIndex());
		if(!success) {
			return JS.message(HttpStatus.FORBIDDEN, "Could not update trait index!");	
		}
		return JS.message(HttpStatus.OK, "Success");
	}
	
	
	@RequestMapping(path = "/unlock-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> unlockTrait(@Valid @RequestBody UnlockTraitParameter input) throws IOException {

		String characterName = input.getCharacterName().toLowerCase();
		TraitType traitType = input.getName();

		// Duplicate protection
		if (traitService.hasTraitUnlocked(traitType, characterName)) {
			return JS.message(HttpStatus.ALREADY_REPORTED, "Already unlocked");
		}

		traitService.unlockTrait(new Trait(traitType, characterName));
		return JS.message(HttpStatus.OK, "Trait unlocked");
	}
	
	@RequestMapping(path = "/lock-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> lockTrait(@Valid @RequestBody LockTraitParameter input) throws IOException {

		String characterName = input.getCharacterName().toLowerCase();
		TraitType traitType = input.getName();

		// Duplicate protection
		if (!traitService.hasTraitUnlocked(traitType, characterName)) {
			return JS.message(HttpStatus.ALREADY_REPORTED, "Already locked");
		}

		traitService.lockTrait(new Trait(traitType, characterName));
		return JS.message(HttpStatus.OK, "Trait Locked");
	}
}
