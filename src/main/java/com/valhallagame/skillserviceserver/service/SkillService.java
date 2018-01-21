package com.valhallagame.skillserviceserver.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.valhallagame.featserviceclient.message.FeatName;
import com.valhallagame.skillserviceserver.model.Skill;
import com.valhallagame.skillserviceserver.rabbitmq.NotificationConsumer;
import com.valhallagame.skillserviceserver.repository.SkillRepository;

@Service
public class SkillService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

	@Autowired
	private SkillRepository skillRepository;

	public Skill saveSkill(Skill skill) {
		return skillRepository.save(skill);
	}

	public void deleteSkill(Skill skill) {
		skillRepository.delete(skill);
	}

	public List<Skill> getSkills(String characterName) {
		return skillRepository.findByCharacterOwner(characterName);
	}

	public void handleFeatAdding(String characterName, String featName) {
		logger.info("Handle Feat Adding characterName: {}, featName: {}", characterName, featName);
		switch (FeatName.valueOf(featName)) {
		default:
			logger.info("No can do!");
			break;
		}
	}
}
