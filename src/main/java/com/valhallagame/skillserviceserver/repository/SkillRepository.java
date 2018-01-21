package com.valhallagame.skillserviceserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.skillserviceserver.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Integer> {
	public List<Skill> findByCharacterOwner(String characterOwner);
}
