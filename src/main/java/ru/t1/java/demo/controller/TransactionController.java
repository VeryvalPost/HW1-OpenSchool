package ru.t1.java.demo.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.t1.java.demo.model.Transaction;
import ru.t1.java.demo.service.TransactionService;





@RestController
@Slf4j
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<Long> operation(@RequestBody Transaction transaction) {

        Long confirmOperation = transactionService.operate(transaction);
        if (confirmOperation == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
       return ResponseEntity.ok(confirmOperation);
    }

    @GetMapping("/testTransaction")
    public ResponseEntity<String> sendTransactionToKafka() {
        try {
            log.info("Тестовая отсылка транзакции в Kafka");
            transactionService.sendTransactionToKafka();
            return ResponseEntity.status(HttpStatus.OK).body("Сообщение успешно отправлено в Kafka");
        } catch (Exception e) {
            log.error("Ошибка при отправке транзакции в Kafka", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при отправке транзакции в Kafka");
        }
    }

}