package com.valhallagame.traitserviceserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.traitserviceserver.model.Trait;

public interface TraitRepository extends JpaRepository<Trait, Integer> {
	public List<Trait> findByCharacterName(String characterOwner);

	public Trait findByCharacterNameAndName(String characterName, String name);
	
	public List<Trait> findByCharacterNameAndBarIndex(String characterName, int barIndex);
}
