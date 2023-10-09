package com.behl.cerberus.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.behl.cerberus.entity.DepositAccount;

@Repository
public interface DepositAccountRepository extends JpaRepository<DepositAccount, UUID> {

	Optional<DepositAccount> findByUserId(final UUID userId);

}