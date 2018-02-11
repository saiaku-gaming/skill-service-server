package com.valhallagame.traitserviceserver.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.valhallagame.traitserviceclient.message.TraitType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "trait")
public class Trait {
	@Id
	@SequenceGenerator(name = "trait_trait_id_seq", sequenceName = "trait_trait_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "trait_trait_id_seq")
	@Column(name = "trait_id", updatable = false)
	private Integer id;

	/**
	 * Maps to a trait type. See  {@link #getTraitType()}
	 */
	@Column(name = "name")
	private String name;

	@Column(name = "character_name")
	private String characterName;

	@Column(name = "bar_index")
	private int barIndex;
	
	public Trait(TraitType traitType, String characterName) {
		this.name = traitType.name();
		this.characterName = characterName;
		this.barIndex = -1; //-1 means not in bar. 
	}
	
	public TraitType getTraitType(){
		return TraitType.valueOf(this.getName());
	}
}
