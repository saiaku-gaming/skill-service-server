package com.valhallagame.skillserviceserver.rabbitmq;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.valhallagame.common.rabbitmq.NotificationMessage;
import com.valhallagame.skillserviceserver.model.Skill;
import com.valhallagame.skillserviceserver.service.SkillService;

@Component
public class NotificationConsumer {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	SkillService skillService;

	@RabbitListener(queues = { "#{skillCharacterDeleteQueue.name}" })
	public void receiveCharacterDelete(NotificationMessage message) {
		String characterName = (String) message.getData().get("characterName");
		List<Skill> skills = skillService.getSkills(characterName);
		for (Skill skill : skills) {
			skillService.deleteSkill(skill);
		}
	}

	@RabbitListener(queues = { "#{skillFeatAddQueue.name}" })
	public void receiveFeatAdd(NotificationMessage message) {
		logger.info("Received fead add notification with message: {}", message.toString());
		String featName = (String) message.getData().get("feat");
		String characterName = (String) message.getData().get("characterName");

		skillService.handleFeatAdding(characterName, featName);
	}
}
