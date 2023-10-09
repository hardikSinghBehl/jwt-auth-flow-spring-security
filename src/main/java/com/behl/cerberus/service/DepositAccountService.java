package com.behl.cerberus.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.behl.cerberus.dto.DepositAccountDetailDto;
import com.behl.cerberus.dto.TransactionDetailDto;
import com.behl.cerberus.dto.TransactionRequestDto;
import com.behl.cerberus.entity.DepositAccount;
import com.behl.cerberus.entity.Transaction;
import com.behl.cerberus.entity.TransactionType;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.exception.InsufficientBalanceException;
import com.behl.cerberus.exception.DepositAccountNotFoundException;
import com.behl.cerberus.repository.DepositAccountRepository;
import com.behl.cerberus.repository.TransactionRepository;

import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositAccountService {

	private final TransactionRepository transactionRepository;
	private final DepositAccountRepository depositAccountRepository;

	public void create(@NonNull final UUID userId) {
		final var accountExists = depositAccountRepository.findByUserId(userId).isPresent();
		if (Boolean.TRUE.equals(accountExists)) {
			throw new AccountAlreadyExistsException("Deposit Account already exists.");
		}

		final var depositAccount = new DepositAccount();
		depositAccount.setUserId(userId);
		depositAccountRepository.save(depositAccount);
	}

	public void processTransaction(@NotNull final UUID userId, @NonNull final TransactionRequestDto transactionRequest) {
		final var depositAccount = depositAccountRepository.findByUserId(userId)
				.orElseThrow(() -> new DepositAccountNotFoundException());

		final var transaction = new Transaction();
		final var transactionType = transactionRequest.getType();
		final var transactionAmount = transactionRequest.getAmount();
		transaction.setAccountId(depositAccount.getId());
		transaction.setAmount(transactionAmount);
		transaction.setType(transactionType);
		transaction.setCurrency(transactionRequest.getCurrency());

		final BigDecimal newBalance;
		if (TransactionType.WITHDRAW.equals(transactionType)) {
			if (depositAccount.getBalance().compareTo(transactionAmount) < 0) {
				throw new InsufficientBalanceException();
			}
			newBalance = depositAccount.getBalance().subtract(transactionAmount);
		} else {
			newBalance = depositAccount.getBalance().add(transactionAmount);
		}

		depositAccount.setBalance(newBalance);
		transactionRepository.save(transaction);
		depositAccountRepository.save(depositAccount);
	}

	public List<TransactionDetailDto> getTransactions(@NonNull final UUID userId) {
		return transactionRepository.findByDepositAccountUserId(userId)
				.stream()
				.map(transaction -> TransactionDetailDto.builder()
						.amount(transaction.getAmount())
						.currency(transaction.getCurrency())
						.type(transaction.getType())
						.timestamp(transaction.getTimestamp())
						.build())
				.toList();
	}

	public DepositAccountDetailDto getByUserId(@NonNull final UUID userId) {
		final var account = depositAccountRepository.findByUserId(userId)
				.orElseThrow(() -> new DepositAccountNotFoundException());

		return DepositAccountDetailDto.builder()
				.balance(account.getBalance())
				.createdAt(account.getCreatedAt())
				.build();
	}

}