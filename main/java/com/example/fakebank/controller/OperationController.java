package com.example.fakebank.controller;

import java.net.URI;

import javax.validation.Valid;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.fakebank.dto.request.RequestOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationsDTO;
import com.example.fakebank.service.OperationService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OperationController {

    public static final String OPERATION_END_POINT_V1 = "/v1/operations";
    public static final String OPERATION_GET_END_POINT_V1 = OPERATION_END_POINT_V1 + "/fromAccount/{accountId}";

    private final OperationService operationService;

    @GetMapping(
            path     = OPERATION_GET_END_POINT_V1,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Retrieves transfer history for a given account.",
            notes = "Given an account id, retrieves the operations/transfers where this account has participated.")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "List of operations"),
            @ApiResponse(code = 404, message = "Account not found.")
    })
    public ResponseEntity<ResponseOperationsDTO> getOperations(@PathVariable("accountId") final long accountId) {
        return ResponseEntity.ok(operationService.retrieveOperations(accountId));
    }

    @PostMapping(
            path = OPERATION_END_POINT_V1,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiOperation(
            value = "Creates new transfer",
            notes = "Transfer amounts between any two accounts, including those owned by different customers.")
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Transfer created."),
            @ApiResponse(code = 404, message = "Sender/receiver account not found."),
            @ApiResponse(code = 400, message = "Sender/receiver account id negative, Insufficient balance to transfer, Same account used in the transfer operation")
    })
    public ResponseEntity postOperation(@Valid @RequestBody RequestOperationDTO requestOperationDTO) {
        ResponseOperationDTO responseOperationDTO = operationService.createNewOperation(requestOperationDTO);

        URI uri =
                ServletUriComponentsBuilder
                        .fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(responseOperationDTO.getId())
                        .toUri();

        return ResponseEntity.created(uri).build();
    }
}
