package com.valhallagame.wardrobeserviceserver.controller;

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
import com.valhallagame.wardrobeserviceclient.message.AddWardrobeItemParameter;
import com.valhallagame.wardrobeserviceclient.message.GetWardrobeItemsParameter;
import com.valhallagame.wardrobeserviceserver.model.WardrobeItem;
import com.valhallagame.wardrobeserviceserver.service.WardrobeItemService;

@Controller
@RequestMapping(path = "/v1/wardrobe")
public class WardrobeController {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private WardrobeItemService wardrobeItemService;
	
	@Autowired
	private CharacterServiceClient characterServiceClient;
	

	@RequestMapping(path = "/get-wardrobe-items", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getWardrobeItems(@Valid @RequestBody GetWardrobeItemsParameter input) throws IOException {
		RestResponse<CharacterData> characterResp = characterServiceClient.getSelectedCharacter(input.getUsername());
		Optional<CharacterData> characterOpt = characterResp.get();
		if(!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();
		List<WardrobeItem> wardrobeItems = wardrobeItemService.getWardrobeItems(character.getCharacterName());
		List<String> items = wardrobeItems.stream().map(WardrobeItem::getName).collect(Collectors.toList());
		return JS.message(HttpStatus.OK, items);
	}


	@RequestMapping(path = "/add-wardrobe-item", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> addWardrobeItem(@Valid @RequestBody AddWardrobeItemParameter input) {

		// Duplicate protection
		List<WardrobeItem> wardrobeItems = wardrobeItemService.getWardrobeItems(input.getCharacterName().toLowerCase());
		List<String> items = wardrobeItems.stream().map(WardrobeItem::getName).collect(Collectors.toList());
		if (items.contains(input.getName().name())) {
			return JS.message(HttpStatus.ALREADY_REPORTED, "Already in store");
		}

		wardrobeItemService.saveWardrobeItem(new WardrobeItem(input.getName().name(), input.getCharacterName().toLowerCase()));
		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.WARDROBE.name(),
				RabbitMQRouting.Wardrobe.ADD_WARDROBE_ITEM.name(),
				new NotificationMessage(input.getCharacterName(), "wardrobe item added"));

		return JS.message(HttpStatus.OK, "Wardrobe item added");
	}
}
