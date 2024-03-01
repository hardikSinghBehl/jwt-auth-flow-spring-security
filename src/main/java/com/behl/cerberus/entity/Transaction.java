package com.behl.cerberus.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transactions")
public class Transaction {

	@Id
	@Setter(AccessLevel.NONE)
	@Column(name = "id", nullable = false, unique = true)
	private UUID id;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false)
	private TransactionType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "currency", nullable = false)
	private Currency currency;

	@Column(name = "amount", nullable = false)
	private BigDecimal amount;

	@Column(name = "deposit_account_id", nullable = false)
	private UUID accountId;

	@Setter(AccessLevel.NONE)
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "deposit_account_id", nullable = false, insertable = false, updatable = false)
	private DepositAccount depositAccount;

	@Setter(AccessLevel.NONE)
	@Column(name = "timestamp", nullable = false)
	private LocalDateTime timestamp;

	@PrePersist
	void onCreate() {
		this.id = UUID.randomUUID();
		this.timestamp = LocalDateTime.now(ZoneOffset.UTC);
	}

}