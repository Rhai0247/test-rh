package com.example.fakebank.service.mapper;

import com.example.fakebank.dto.UserDTO;
import com.example.fakebank.dto.request.RequestAccountDTO;
import com.example.fakebank.dto.request.RequestOperationDTO;
import com.example.fakebank.dto.response.ResponseAccountDTO;
import com.example.fakebank.dto.response.ResponseOperationDTO;
import com.example.fakebank.entity.Account;
import com.example.fakebank.entity.Operation;
import com.example.fakebank.entity.User;

public class BankMapper {
    public static Account toAccountEntity(RequestAccountDTO requestAccountDTO) {
        return Account
                .builder()
                .initialDepositAmount(requestAccountDTO.getInitialDepositAmount())
                .balance(requestAccountDTO.getInitialDepositAmount())
                .build();
    }

    public static ResponseAccountDTO toResponseAccountDTO(Account account) {
        return ResponseAccountDTO
                .builder()
                .userId(account.getUser().getId())
                .id(account.getId())
                .balance(account.getBalance())
                .creationTimestamp(account.getCreationTimestamp())
                .build();
    }

    public static Operation toOperationEntity(RequestOperationDTO requestOperationDTO) {
        return Operation
                .builder()
                .value(requestOperationDTO.getValue())
                .build();
    }

    public static ResponseOperationDTO toResponseOperationDTO(Operation operation) {
        return ResponseOperationDTO
                .builder()
                .id(operation.getId())
                .senderAccountId(operation.getSenderAccount().getId())
                .receiverAccountId(operation.getReceiverAccount().getId())
                .value(operation.getValue())
                .creationTimestamp(operation.getOperationDateTime())
                .build();
    }
}
