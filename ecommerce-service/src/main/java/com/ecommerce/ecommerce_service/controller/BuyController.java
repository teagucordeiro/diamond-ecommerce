package com.ecommerce.ecommerce_service.controller;

import com.ecommerce.ecommerce_service.model.Product;
import com.ecommerce.ecommerce_service.service.BuyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/buy")
public class BuyController {
    private final BuyService buyService;

    @Autowired
    public BuyController(BuyService buyService) {
        this.buyService = buyService;
    }

    @PostMapping
    public ResponseEntity<String> buyProduct(
            @RequestParam("product") Long productId,
            @RequestParam("user") Long userId,
            @RequestParam("ft") Boolean faultToleranceEnabled) {


        Product response = buyService.fetchProductResponse("23423");


        return ResponseEntity.ok("Purchase processed successfully for product " + productId
                + " by user " + userId + ". Fault tolerance: " + faultToleranceEnabled
                + ". Service response: " + response.getName());
    }
}
