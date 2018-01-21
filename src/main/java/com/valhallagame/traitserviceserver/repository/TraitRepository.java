package com.valhallagame.traitserviceserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.valhallagame.traitserviceserver.model.Trait;

public interface TraitRepository extends JpaRepository<Trait, Integer> {
	public List<Trait> findByCharacterOwner(String characterOwner);

	public Trait findByCharacterOwnerAndName(String characterName, String name);
}
