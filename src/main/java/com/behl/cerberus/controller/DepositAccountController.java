package com.behl.cerberus.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.behl.cerberus.dto.DepositAccountDetailDto;
import com.behl.cerberus.dto.ExceptionResponseDto;
import com.behl.cerberus.dto.TransactionDetailDto;
import com.behl.cerberus.dto.TransactionRequestDto;
import com.behl.cerberus.service.DepositAccountService;
import com.behl.cerberus.utility.AuthenticatedUserIdProvider;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/deposit-accounts")
@PreAuthorize("hasAuthority('fullaccess')")
@Tag(name = "Deposit Accounts", description = "Endpoints for managing deposit accounts")
public class DepositAccountController {

    private final DepositAccountService depositAccountService;
    private final AuthenticatedUserIdProvider authenticatedUserIdProvider;

    @PostMapping
    @Operation(summary = "Creates a Deposit Account", description = "Creates a new deposit account corresponding to the logged-in user")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "201", description = "Deposit Account created successfully",
            		content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "409", description = "Deposit Account already exists corresponding to the logged-in user",
            		content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
    public ResponseEntity<HttpStatus> createDepositAccount() {
        final var userId = authenticatedUserIdProvider.getUserId();
        depositAccountService.create(userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Deposit Account Details", description = "Retrieves details of the authenticated user's deposit account")
    @ApiResponse(responseCode = "200", description = "Deposit Account details retrieved successfully")
    public ResponseEntity<DepositAccountDetailDto> getDepositAccountDetails() {
        final var userId = authenticatedUserIdProvider.getUserId();
        final var accountDetails = depositAccountService.getByUserId(userId);
        return ResponseEntity.ok(accountDetails);
    }

    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process a transaction", description = "Processes transaction against users deposit account")
    @ApiResponses(value = { 
            @ApiResponse(responseCode = "200", description = "Transaction processed successfully",
            		content = @Content(schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "404", description = "Users deposit account must be created prior to processing transaction(s)",
            		content = @Content(schema = @Schema(implementation = ExceptionResponseDto.class))) })
    public ResponseEntity<HttpStatus> processTransaction(@Valid @RequestBody final TransactionRequestDto transactionRequest) {
        final var userId = authenticatedUserIdProvider.getUserId();
        depositAccountService.processTransaction(userId, transactionRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Fetches all transactions", description = "Retrieves all transactions corresponding to user's deposit account")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    public ResponseEntity<List<TransactionDetailDto>> getTransactions() {
        final var userId = authenticatedUserIdProvider.getUserId();
        final var transactions = depositAccountService.getTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

}
