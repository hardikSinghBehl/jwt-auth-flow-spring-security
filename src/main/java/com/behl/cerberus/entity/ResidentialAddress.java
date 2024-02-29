package com.behl.cerberus.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "residential_addresses")
public class ResidentialAddress {

	@Id
	@Setter(AccessLevel.NONE)
	@Column(name = "id", nullable = false, unique = true)
	private UUID id;
	
	@Column(name = "user_id", nullable = true)
	private UUID userId;

	@Setter(AccessLevel.NONE)
	@OneToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "user_id", nullable = true, insertable = false, updatable = false)
	private User user;

	@Column(name = "street_address", nullable = false)
	private String streetAddress;
	
	@Column(name = "city", nullable = false)
	private String city;
	
	@Column(name = "state", nullable = false)
	private String state;
	
	@Column(name = "postal_code", nullable = false)
	private String postalCode;
    
	@PrePersist
	void onCreate() {
		this.id = UUID.randomUUID();
	}

}
