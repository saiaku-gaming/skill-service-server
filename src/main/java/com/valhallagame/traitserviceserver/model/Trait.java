package com.valhallagame.traitserviceserver.model;

import com.valhallagame.traitserviceclient.message.TraitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

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
	 * Maps to a trait type. See {@link #getTraitType()}
	 */
	@Column(name = "name")
	private String name;

	@Column(name = "character_name")
	private String characterName;

	@Column(name = "claimed")
	private Boolean claimed;

	@Column(name = "selected_attribute")
	private String selectedAttribute;

	@Column(name = "position_x")
	private Integer positionX;
	@Column(name = "position_y")
	private Integer positionY;

	@Column(name = "specialization")
	private Integer specialization;
	@Column(name = "specialization_position_x")
	private Integer specializationPositionX;
	@Column(name = "specialization_position_y")
	private Integer specializationPositionY;

	public Trait(TraitType traitType, String characterName) {
		this.name = traitType.name();
		this.characterName = characterName;
		this.claimed = false;
		this.selectedAttribute = "";
		this.positionX = -1;
		this.positionY = -1;
		this.specialization = -1;
		this.specializationPositionX = -1;
		this.specializationPositionY = -1;
	}

	public TraitType getTraitType() {
		return TraitType.valueOf(this.getName());
	}
}
