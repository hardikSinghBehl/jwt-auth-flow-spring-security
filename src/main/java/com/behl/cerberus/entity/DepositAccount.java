package com.behl.cerberus.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
@Table(name = "deposit_accounts")
public class DepositAccount {

	@Id
	@Setter(AccessLevel.NONE)
	@Column(name = "id", nullable = false, unique = true)
	private UUID id;

	@Column(name = "balance", nullable = false)
	private BigDecimal balance;

	@Column(name = "user_id", nullable = false)
	private UUID userId;

	@Setter(AccessLevel.NONE)
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
	private User user;

	@Setter(AccessLevel.NONE)
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@PrePersist
	void onCreate() {
		this.id = UUID.randomUUID();
		this.balance = BigDecimal.ZERO;
		this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
	}

}