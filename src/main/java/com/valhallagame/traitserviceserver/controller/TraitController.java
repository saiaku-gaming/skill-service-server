package com.valhallagame.traitserviceserver.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.valhallagame.characterserviceclient.CharacterServiceClient;
import com.valhallagame.characterserviceclient.model.CharacterData;
import com.valhallagame.common.JS;
import com.valhallagame.common.RestResponse;
import com.valhallagame.traitserviceclient.message.*;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.service.TraitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
		if (!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();

		List<Trait> traits = traitService.getTraits(character.getCharacterName());
		TraitData traitData = convertToData(traits);

		return JS.message(HttpStatus.OK, traitData);
	}

	private TraitData convertToData(List<Trait> traits) {
		List<TraitType> ownedTraits = traits.stream().map(t -> TraitType.valueOf(t.getName()))
				.collect(Collectors.toList());
		// @formatter:off
		List<SkilledTraitData> skilledTraits = traits.stream()
				.filter(Trait::getClaimed)
				.map(t -> new SkilledTraitData(
						TraitType.valueOf(t.getName()),
						AttributeType.valueOf(t.getSelectedAttribute()),
						t.getPosition(),
						t.getSpecialization(),
						t.getSpecializationPosition()))
				.collect(Collectors.toList());
		// @formatter:on
		return new TraitData(ownedTraits, skilledTraits);
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

	@RequestMapping(path = "/skill-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> skillTrait(@Valid @RequestBody SkillTraitParameter input) {
		Optional<Trait> unlockedTrait = traitService.getUnlockedTrait(input.getCharacterName(), input.getName());

		if (!unlockedTrait.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Unable to find trait: " + input.getName().name());
		}

		Trait trait = unlockedTrait.get();
		if (trait.getClaimed()) {
			return JS.message(HttpStatus.CONFLICT, "Trait " + input.getName().name() + " is already skilled.");
		}

		traitService.skillTrait(trait, input.getSelectedAttribute(), input.getPosition());

		return JS.message(HttpStatus.OK, "Trait skilled");
	}

	@RequestMapping(path = "/unskill-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> unskillTrait(@Valid @RequestBody UnskillTraitParameter input) {
		Optional<Trait> unlockedTrait = traitService.getUnlockedTrait(input.getCharacterName(), input.getName());

		if (!unlockedTrait.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Unable to find trait: " + input.getName().name());
		}

		Trait trait = unlockedTrait.get();
		if (!trait.getClaimed()) {
			return JS.message(HttpStatus.CONFLICT, "Trait " + input.getName().name() + " is already unskilled.");
		}

		traitService.unskillTrait(trait);

		return JS.message(HttpStatus.OK, "Trait unskilled");
	}

	@RequestMapping(path = "/specialize-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> specializeTrait(@Valid @RequestBody SpecializeTraitParameter input) {
		Optional<Trait> unlockedTrait = traitService.getUnlockedTrait(input.getCharacterName(), input.getName());

		if (!unlockedTrait.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Unable to find trait: " + input.getName().name());
		}

		Trait trait = unlockedTrait.get();
		if (!trait.getClaimed()) {
			return JS.message(HttpStatus.CONFLICT, "Trait " + input.getName().name() + " is not skilled.");
		}

		traitService.specializeTrait(trait, input.getSpecialization(), input.getPosition());

		return JS.message(HttpStatus.OK, "Trait specialized");
	}

	@RequestMapping(path = "/unspecialize-trait", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> unspecializeTrait(@Valid @RequestBody UnspecializeTraitParameter input) {
		Optional<Trait> unlockedTrait = traitService.getUnlockedTrait(input.getCharacterName(), input.getName());

		if (!unlockedTrait.isPresent()) {
			return JS.message(HttpStatus.NOT_FOUND, "Unable to find trait: " + input.getName().name());
		}

		Trait trait = unlockedTrait.get();
		if (!trait.getClaimed()) {
			return JS.message(HttpStatus.CONFLICT, "Trait " + input.getName().name() + " is not skilled.");
		}

		traitService.unspecializeTrait(trait);

		return JS.message(HttpStatus.OK, "Trait unspecialized");
	}
}
