package com.behl.cerberus.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.behl.cerberus.dto.TransactionRequestDto;
import com.behl.cerberus.entity.DepositAccount;
import com.behl.cerberus.entity.Transaction;
import com.behl.cerberus.entity.TransactionType;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.exception.InsufficientBalanceException;
import com.behl.cerberus.repository.DepositAccountRepository;
import com.behl.cerberus.repository.TransactionRepository;

class DepositAccountServiceTest {
	
	private final TransactionRepository transactionRepository = mock(TransactionRepository.class);
	private final DepositAccountRepository depositAccountRepository = mock(DepositAccountRepository.class);
	private final DepositAccountService depositAccountService = new DepositAccountService(transactionRepository, depositAccountRepository);
	
	@Test
	void shouldCreateDepositAccountForUser() {
		// set up conflict detection query result
		final var userId = UUID.randomUUID();
		when(depositAccountRepository.existsByUserId(userId)).thenReturn(Boolean.FALSE);
		
		// invoke method under test
		depositAccountService.create(userId);
		
		// verify mock interactions
		verify(depositAccountRepository).existsByUserId(userId);
		verify(depositAccountRepository).save(any(DepositAccount.class));
	}
	
	@Test
	void shouldNotAllowMultipleDepositAccountsForUser() {
		// set up conflict detection query result
		final var userId = UUID.randomUUID();
		when(depositAccountRepository.existsByUserId(userId)).thenReturn(Boolean.TRUE);
		
		// assert AccountAlreadyExistsException is thrown if deposit account exists for user
		assertThrows(AccountAlreadyExistsException.class, () -> depositAccountService.create(userId));
		
		// verify mock interactions
		verify(depositAccountRepository).existsByUserId(userId);
	}
	
	@Test
	void shouldProcessWithdrawlTransactionForValidRequest() {
		// prepare withdrawl transaction request
		final var withdrawlAmount = new BigDecimal(new Random().nextInt(1, 10));
		final var transactionRequest = mock(TransactionRequestDto.class);
		when(transactionRequest.getAmount()).thenReturn(withdrawlAmount);
		when(transactionRequest.getType()).thenReturn(TransactionType.WITHDRAW);
		
		// prepare users deposit account with balance greater than withdrawl amount
		final var userId = UUID.randomUUID();
		final var accountBalance = new BigDecimal(new Random().nextInt(11, 100));
		final var depositAccount = mock(DepositAccount.class);
		when(depositAccount.getBalance()).thenReturn(accountBalance);
		when(depositAccountRepository.findByUserId(userId)).thenReturn(Optional.of(depositAccount));
		
		// invoke method under test
		depositAccountService.processTransaction(userId, transactionRequest);
		
		// verify mock interactions
		verify(depositAccountRepository).findByUserId(userId);
		verify(depositAccount).setBalance(accountBalance.subtract(withdrawlAmount));
		verify(depositAccountRepository).save(depositAccount);
		verify(transactionRepository).save(any(Transaction.class));
	}
	
	@Test
	void shouldNotProcessTransactionWhenWithdrawlAmountGreaterThanAccountBalance() {
		// prepare withdrawl transaction request
		final var withdrawlAmount = new BigDecimal(new Random().nextInt(100, 1000));
		final var transactionRequest = mock(TransactionRequestDto.class);
		when(transactionRequest.getAmount()).thenReturn(withdrawlAmount);
		when(transactionRequest.getType()).thenReturn(TransactionType.WITHDRAW);
		
		// prepare users deposit account with balance less than withdrawl amount
		final var userId = UUID.randomUUID();
		final var accountBalance = new BigDecimal(new Random().nextInt(1, 10));
		final var depositAccount = mock(DepositAccount.class);
		when(depositAccount.getBalance()).thenReturn(accountBalance);
		when(depositAccountRepository.findByUserId(userId)).thenReturn(Optional.of(depositAccount));
		
		// assert InsufficientBalanceException is thrown when withdrawl amount greater than account balance
		assertThrows(InsufficientBalanceException.class, () -> depositAccountService.processTransaction(userId, transactionRequest));
		
		// verify mock interactions
		verify(depositAccountRepository).findByUserId(userId);
		verify(depositAccountRepository, times(0)).save(depositAccount);
		verify(transactionRepository, times(0)).save(any(Transaction.class));
	}
	
	@Test
	void shouldProcessDepositTransactionForValidRequest() {
		// prepare deposit transaction request
		final var depositAmount = new BigDecimal(new Random().nextInt(1, 100));
		final var transactionRequest = mock(TransactionRequestDto.class);
		when(transactionRequest.getAmount()).thenReturn(depositAmount);
		when(transactionRequest.getType()).thenReturn(TransactionType.DEPOSIT);
		
		// prepare users deposit account
		final var userId = UUID.randomUUID();
		final var accountBalance = new BigDecimal(0);
		final var depositAccount = mock(DepositAccount.class);
		when(depositAccount.getBalance()).thenReturn(accountBalance);
		when(depositAccountRepository.findByUserId(userId)).thenReturn(Optional.of(depositAccount));
		
		// invoke method under test
		depositAccountService.processTransaction(userId, transactionRequest);
		
		// verify mock interactions
		verify(depositAccountRepository).findByUserId(userId);
		verify(depositAccount).setBalance(accountBalance.add(depositAmount));
		verify(depositAccountRepository).save(depositAccount);
		verify(transactionRepository).save(any(Transaction.class));
	}
	
}
