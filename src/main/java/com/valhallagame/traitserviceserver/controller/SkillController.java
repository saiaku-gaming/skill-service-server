package com.valhallagame.traitserviceserver.controller;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.common.rabbitmq.RabbitMQRouting;
import com.valhallagame.traitserviceclient.message.AddTraitParameter;
import com.valhallagame.traitserviceclient.message.GetTraitsParameter;
import com.valhallagame.traitserviceserver.model.Trait;
import com.valhallagame.traitserviceserver.service.TraitService;

@Controller
@RequestMapping(path = "/v1/trait")
public class TraitController {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private TraitService traitService;
	
	@Autowired
	private CharacterServiceClient characterServiceClient;
	

	@RequestMapping(path = "/get-trait-items", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getTraits(@Valid @RequestBody GetTraitsParameter input) throws IOException {
		RestResponse<CharacterData> characterResp = characterServiceClient.getSelectedCharacter(input.getUsername());
		Optional<CharacterData> characterOpt = characterResp.get();
		if(!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();
		List<Trait> traits = traitService.getTraits(character.getCharacterName());
		List<String> items = traits.stream().map(Trait::getName).collect(Collectors.toList());
		return JS.message(HttpStatus.OK, items);
	}


	@RequestMapping(path = "/add-trait-item", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> addTrait(@Valid @RequestBody AddTraitParameter input) {

		// Duplicate protection
		List<Trait> traits = traitService.getTraits(input.getCharacterName().toLowerCase());
		List<String> items = traits.stream().map(Trait::getName).collect(Collectors.toList());
		if (items.contains(input.getName().name())) {
			return JS.message(HttpStatus.ALREADY_REPORTED, "Already in store");
		}

		traitService.saveTrait(new Trait(input.getName().name(), input.getCharacterName().toLowerCase()));
		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.TRAIT.name(),
				RabbitMQRouting.Trait.ADD.name(),
				new NotificationMessage(input.getCharacterName(), "trait item added"));

		return JS.message(HttpStatus.OK, "Trait item added");
	}
}
