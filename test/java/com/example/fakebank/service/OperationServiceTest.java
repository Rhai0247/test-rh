package com.example.fakebank.service;

import com.example.fakebank.dto.request.RequestOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationDTO;
import com.example.fakebank.dto.response.ResponseOperationsDTO;
import com.example.fakebank.entity.Account;
import com.example.fakebank.entity.Operation;
import com.example.fakebank.exception.InvalidRequestOperationException;
import com.example.fakebank.repository.OperationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class OperationServiceTest {
    @Mock
    private AccountService accountService;
    @Mock
    private OperationRepository operationRepository;

    @Spy
    @InjectMocks
    private OperationService operationService;


    @Test
    void testCreateNewOperationSuccessfully() {
        BigDecimal someValue = BigDecimal.valueOf(0.01);
        Account sender = Account.builder().id(1L).balance(BigDecimal.valueOf(1.01)).build();
        Account receiver = Account.builder().id(2L).balance(BigDecimal.valueOf(0.99)).build();
        RequestOperationDTO requestOperationDTO =
                RequestOperationDTO.builder()
                        .senderAccountId(sender.getId())
                        .receiverAccountId(receiver.getId())
                        .value(someValue).build();
        Operation operationWithoutId =
                Operation.builder()
                        .senderAccount(sender)
                        .receiverAccount(receiver)
                        .value(someValue)
                        .operationDateTime(LocalDateTime.MIN).build();
        Operation operationWithId =
                Operation.builder().id(3L).senderAccount(sender).receiverAccount(receiver).value(someValue)
                        .operationDateTime(LocalDateTime.MIN).build();
        ResponseOperationDTO expectedResponseOperationDTO =
                ResponseOperationDTO.builder().id(operationWithId.getId()).senderAccountId(sender.getId())
                        .receiverAccountId(receiver.getId()).value(someValue)
                        .creationTimestamp(operationWithId.getOperationDateTime()).build();
        when(accountService.getAccountById(sender.getId())).thenReturn(sender);
        when(accountService.getAccountById(receiver.getId())).thenReturn(receiver);
        when(operationRepository.save(operationWithoutId)).thenReturn(operationWithId);
        when(operationService.getCurrentTimestamp()).thenReturn(LocalDateTime.MIN);

        ResponseOperationDTO actualResponseOperationDTO = operationService.createNewOperation(requestOperationDTO);

        Assertions.assertThat(actualResponseOperationDTO).isEqualTo(expectedResponseOperationDTO);
    }

    @Test
    void testCreateNewAccountWithNullRequest() {
        Throwable throwable = Assertions.catchThrowable(() ->operationService.createNewOperation(null));
        Assertions.assertThat(throwable).isInstanceOf(InvalidRequestOperationException.class);
    }

    @Test
    void testRetrieveOperationsEmpty() {
        Account sender = Account.builder().id(1L).build();

        when(operationRepository
                        .findAllBySenderAccount_IdOrReceiverAccount_IdOrderByOperationDateTimeDesc(
                                sender.getId(), sender.getId())).thenReturn(Optional.empty());
        when(accountService.getAccountById(sender.getId())).thenReturn(sender);

        ResponseOperationsDTO operations = operationService.retrieveOperations(sender.getId());
        Assertions.assertThat(operations.getAccountId()).isEqualTo(sender.getId());
        Assertions.assertThat(operations.getOperationDTOList().isEmpty()).isTrue();
    }

    @Test
    void testRetrieveOperationsSuccessful() {
        Account sender = Account.builder().id(1L).build();
        Account receiver = Account.builder().id(2L).build();
        BigDecimal someValue1 = BigDecimal.valueOf(1.50);
        BigDecimal someValue2 = BigDecimal.valueOf(0.50);
        List<Operation> operationList =
                List.of(
                        Operation.builder()
                                .id(1L)
                                .senderAccount(sender)
                                .receiverAccount(receiver)
                                .value(someValue1)
                                .operationDateTime(LocalDateTime.MIN)
                                .build(),
                        Operation.builder()
                                .id(2L)
                                .senderAccount(receiver)
                                .receiverAccount(sender)
                                .value(someValue2)
                                .operationDateTime(LocalDateTime.MIN)
                                .build()
                );

        List<ResponseOperationDTO> expectedOperationDTOList =
                List.of(
                        ResponseOperationDTO.builder()
                                .id(1L)
                                .senderAccountId(sender.getId())
                                .receiverAccountId(receiver.getId())
                                .value(someValue1)
                                .creationTimestamp(LocalDateTime.MIN)
                                .build(),
                        ResponseOperationDTO.builder()
                                .id(2L)
                                .senderAccountId(receiver.getId())
                                .receiverAccountId(sender.getId())
                                .value(someValue2)
                                .creationTimestamp(LocalDateTime.MIN)
                                .build()
                );

        when(operationRepository
                .findAllBySenderAccount_IdOrReceiverAccount_IdOrderByOperationDateTimeDesc(
                        sender.getId(), sender.getId())).thenReturn(Optional.of(operationList));
        when(accountService.getAccountById(sender.getId())).thenReturn(sender);

        ResponseOperationsDTO operations = operationService.retrieveOperations(sender.getId());
        Assertions.assertThat(operations.getAccountId()).isEqualTo(sender.getId());
        Assertions.assertThat(operations.getOperationDTOList()).isEqualTo(expectedOperationDTOList);
    }
}
