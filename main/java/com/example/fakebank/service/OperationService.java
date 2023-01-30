package com.example.fakebank.service;

import static com.example.fakebank.configuration.BankConstants.ERROR_MESSAGE_NULL_REQUEST_OPERATION_DTO;
import static com.example.fakebank.service.ServiceUtil.throwsOnCondition;
import static com.example.fakebank.service.mapper.BankMapper.toResponseOperationDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.fakebank.dto.request.RequestOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationsDTO;
import com.example.fakebank.entity.Account;
import com.example.fakebank.entity.Operation;
import com.example.fakebank.exception.InvalidRequestOperationException;
import com.example.fakebank.repository.OperationRepository;
import com.example.fakebank.service.mapper.BankMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class OperationService {

    private final AccountService accountService;
    private final OperationRepository operationRepository;

    @Transactional
    public ResponseOperationDTO createNewOperation(@Valid RequestOperationDTO requestOperationDTO) {
        log.debug("Creating a new operation - {}", requestOperationDTO);
        throwsOnCondition(Objects.isNull(requestOperationDTO), InvalidRequestOperationException::new,
                ERROR_MESSAGE_NULL_REQUEST_OPERATION_DTO);
        Operation operation = BankMapper.toOperationEntity(requestOperationDTO);
        Account senderAccount = accountService.getAccountById(requestOperationDTO.getSenderAccountId());
        Account receiverAccount = accountService.getAccountById(requestOperationDTO.getReceiverAccountId());
        accountService.transfer(senderAccount, receiverAccount, requestOperationDTO.getValue());
        fillMissingFields(operation, senderAccount, receiverAccount);
        operation = operationRepository.save(operation);
        logOperation(operation);
        return toResponseOperationDTO(operation);
    }

    //TODO: method to simulate transaction error, how to fix?
    public void logOperation(Operation operation) {
        log.debug("Created operation - {}", operation);
    }

    public ResponseOperationsDTO retrieveOperations(long accountId) {
        log.debug("Retrieving operations accountId - {}", accountId);
        Account account = accountService.getAccountById(accountId);
        List<ResponseOperationDTO> operationDTOList = operationRepository
                .findAllBySenderAccount_IdOrReceiverAccount_IdOrderByOperationDateTimeDesc(account.getId(), account.getId())
                .orElse(new ArrayList<>()).stream().map(BankMapper::toResponseOperationDTO)
                .collect(Collectors.toList());
        return ResponseOperationsDTO.builder()
                .accountId(accountId)
                .operationDTOList(operationDTOList)
                .creationTimestamp(getCurrentTimestamp())
                .build();
    }

    private void fillMissingFields(Operation operation, Account senderAccount, Account receiverAccount) {
        operation.setSenderAccount(senderAccount);
        operation.setReceiverAccount(receiverAccount);
        operation.setOperationDateTime(getCurrentTimestamp());
    }

    LocalDateTime getCurrentTimestamp() {
        return LocalDateTime.now();
    }
}
