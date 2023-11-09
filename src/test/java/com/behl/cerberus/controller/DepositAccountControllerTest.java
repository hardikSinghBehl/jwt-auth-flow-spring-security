package com.behl.cerberus.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import com.behl.cerberus.configuration.CustomAuthenticationEntryPoint;
import com.behl.cerberus.configuration.SecurityConfiguration;
import com.behl.cerberus.dto.DepositAccountDetailDto;
import com.behl.cerberus.dto.TransactionRequestDto;
import com.behl.cerberus.entity.Currency;
import com.behl.cerberus.entity.TransactionType;
import com.behl.cerberus.exception.AccountAlreadyExistsException;
import com.behl.cerberus.exception.DepositAccountNotFoundException;
import com.behl.cerberus.exception.ExceptionResponseHandler;
import com.behl.cerberus.exception.InsufficientBalanceException;
import com.behl.cerberus.service.DepositAccountService;
import com.behl.cerberus.service.TokenRevocationService;
import com.behl.cerberus.utility.ApiEndpointSecurityInspector;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;
import com.behl.cerberus.utility.JwtUtility;

import io.swagger.v3.core.util.Json;
import lombok.SneakyThrows;

@WebMvcTest(controllers = DepositAccountController.class)
@Import({ ExceptionResponseHandler.class, SecurityConfiguration.class, CustomAuthenticationEntryPoint.class, ApiEndpointSecurityInspector.class })
class DepositAccountControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private DepositAccountService depositAccountService;

	@SpyBean
	private AuthenticatedUserIdProvider authenticatedUserIdProvider;

	@MockBean
	private JwtUtility jwtUtility;

	@MockBean
	private TokenRevocationService tokenRevocationService;
	
	@Test
	@SneakyThrows
	void shouldThrowConflictIfDepositAccountAlreadyExistsForAuthenticatedUser() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);

		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// simulate conflict w.r.t deposit accounts for authenticated user
		final var errorMessage = "Deposit Account already exists.";
		doThrow(new AccountAlreadyExistsException(errorMessage)).when(depositAccountService).create(userId);;
		
		// execute API request
		final var apiPath = "/deposit-accounts";
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isConflict())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.CONFLICT.toString()))
				.andExpect(jsonPath("$.Description").value(errorMessage));
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).create(userId);
	}
	
	@Test
	@SneakyThrows
	void shouldCreateDepositAccountForAuthenticatedUser() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);

		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// execute API request
		final var apiPath = "/deposit-accounts";
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isCreated())
				.andDo(print());
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).create(userId);
	}
	
	@Test
	@SneakyThrows
	void shouldReturnNotFoundIfDepositAccountIsNotCreated() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// mock deposit account not created scenario
		when(depositAccountService.getByUserId(userId)).thenThrow(new DepositAccountNotFoundException());
		
		// execute API request
		final var apiPath = "/deposit-accounts";
		mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isNotFound())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.NOT_FOUND.toString()))
				.andExpect(jsonPath("$.Description").value("Deposit account must be created prior to performing this operation"));
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).getByUserId(userId);
	}
	
	@Test
	@SneakyThrows
	void shouldProcessTransactionSuccessfullyForAuthenticatedUserIfAccountCreated() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// mock transaction processing
		final var withdrawlAmount = new BigDecimal(new Random().nextInt(1, 100));
		final var transactionRequest = new TransactionRequestDto();
		transactionRequest.setAmount(withdrawlAmount);
		transactionRequest.setCurrency(Currency.USD);
		transactionRequest.setType(TransactionType.WITHDRAW);
		doNothing().when(depositAccountService).processTransaction(eq(userId), refEq(transactionRequest));
		
		// execute API request
		final var apiPath = "/deposit-accounts/transactions";
		final var requestBody = Json.mapper().writeValueAsString(transactionRequest);
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isOk())
				.andDo(print());
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).processTransaction(eq(userId), refEq(transactionRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldNotAllowWithrawlMoreThanAccountBalance() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// mock withrawl amount more than account balance scenario
		final var withdrawlAmount = new BigDecimal(new Random().nextInt(1, 100));
		final var transactionRequest = new TransactionRequestDto();
		transactionRequest.setAmount(withdrawlAmount);
		transactionRequest.setCurrency(Currency.USD);
		transactionRequest.setType(TransactionType.WITHDRAW);
		doThrow(new InsufficientBalanceException()).when(depositAccountService).processTransaction(eq(userId), refEq(transactionRequest));
		
		// execute API request
		final var apiPath = "/deposit-accounts/transactions";
		final var requestBody = Json.mapper().writeValueAsString(transactionRequest);
		mockMvc.perform(post(apiPath)
				.header("Authorization", "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.content(requestBody))
				.andExpect(status().isNotAcceptable())
				.andDo(print())
				.andExpect(jsonPath("$.Status").value(HttpStatus.NOT_ACCEPTABLE.toString()))
				.andExpect(jsonPath("$.Description").value("Insufficient balance to perform transaction"));
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).processTransaction(eq(userId), refEq(transactionRequest));
	}
	
	@Test
	@SneakyThrows
	void shouldFetchDepositAccountDetailsForAuthenticatedUser() {
		// simulate authority extraction from access token
		final var scope = "fullaccess";
		final var accessToken = "test-access-token";
		final var accessTokenAuthority = List.<GrantedAuthority>of(new SimpleGrantedAuthority(scope));
		when(jwtUtility.getAuthority(accessToken)).thenReturn(accessTokenAuthority);
		
		// simulate user ID extraction from access token
		// @see com.behl.cerberus.filter.JwtAuthenticationFilter
		final var userId = UUID.randomUUID();
		when(jwtUtility.extractUserId(accessToken)).thenReturn(userId);
		
		// mock deposit account details fetch call
		final var balance = new BigDecimal(new Random().nextInt(1, 100));
		final var createdAt = LocalDateTime.now();
		final var depositAccountDetails = mock(DepositAccountDetailDto.class);
		when(depositAccountDetails.getBalance()).thenReturn(balance);
		when(depositAccountDetails.getCreatedAt()).thenReturn(createdAt);
		when(depositAccountService.getByUserId(userId)).thenReturn(depositAccountDetails);
		
		// execute API request
		final var apiPath = "/deposit-accounts";
		mockMvc.perform(get(apiPath)
				.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk())
				.andDo(print())
				.andExpect(jsonPath("$.Balance").value(balance))
				.andExpect(jsonPath("$.CreatedAt").value(createdAt.toString()));
		
		// verify mock interaction
		verify(authenticatedUserIdProvider).getUserId();
		verify(depositAccountService).getByUserId(userId);
	}

}
