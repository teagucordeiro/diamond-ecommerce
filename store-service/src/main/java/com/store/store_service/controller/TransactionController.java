package com.store.store_service.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.store.store_service.dto.TransactionDTO;
import com.store.store_service.model.Transaction;
import com.store.store_service.service.TransactionService;

@RestController
@RequestMapping("/store")
public class TransactionController {
  private final TransactionService transactionService;

  public TransactionController(TransactionService transactionService) {
    this.transactionService = transactionService;
  }

  @PostMapping("/sell")
  public ResponseEntity<?> createTransaction(@RequestBody TransactionDTO transactionDTO) {
    try {
      Transaction transaction = transactionService.createTransaction(transactionDTO.getProductId());
      return ResponseEntity.ok(transaction);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }
}
