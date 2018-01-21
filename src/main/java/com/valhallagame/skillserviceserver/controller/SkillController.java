package com.valhallagame.skillserviceserver.controller;

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
import com.valhallagame.skillserviceclient.message.AddSkillParameter;
import com.valhallagame.skillserviceclient.message.GetSkillsParameter;
import com.valhallagame.skillserviceserver.model.Skill;
import com.valhallagame.skillserviceserver.service.SkillService;

@Controller
@RequestMapping(path = "/v1/skill")
public class SkillController {

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@Autowired
	private SkillService skillService;
	
	@Autowired
	private CharacterServiceClient characterServiceClient;
	

	@RequestMapping(path = "/get-skill-items", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> getSkills(@Valid @RequestBody GetSkillsParameter input) throws IOException {
		RestResponse<CharacterData> characterResp = characterServiceClient.getSelectedCharacter(input.getUsername());
		Optional<CharacterData> characterOpt = characterResp.get();
		if(!characterOpt.isPresent()) {
			return JS.message(characterResp);
		}
		CharacterData character = characterOpt.get();
		List<Skill> skills = skillService.getSkills(character.getCharacterName());
		List<String> items = skills.stream().map(Skill::getName).collect(Collectors.toList());
		return JS.message(HttpStatus.OK, items);
	}


	@RequestMapping(path = "/add-skill-item", method = RequestMethod.POST)
	@ResponseBody
	public ResponseEntity<JsonNode> addSkill(@Valid @RequestBody AddSkillParameter input) {

		// Duplicate protection
		List<Skill> skills = skillService.getSkills(input.getCharacterName().toLowerCase());
		List<String> items = skills.stream().map(Skill::getName).collect(Collectors.toList());
		if (items.contains(input.getName().name())) {
			return JS.message(HttpStatus.ALREADY_REPORTED, "Already in store");
		}

		skillService.saveSkill(new Skill(input.getName().name(), input.getCharacterName().toLowerCase()));
		rabbitTemplate.convertAndSend(RabbitMQRouting.Exchange.SKILL.name(),
				RabbitMQRouting.Skill.ADD.name(),
				new NotificationMessage(input.getCharacterName(), "skill item added"));

		return JS.message(HttpStatus.OK, "Skill item added");
	}
}
