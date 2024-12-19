package com.fidelity.fidelity_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fidelity.fidelity_service.model.BonusModel;
import com.fidelity.fidelity_service.model.UserModel;
import com.fidelity.fidelity_service.service.BonusService;

import jakarta.websocket.server.PathParam;

@RestController
@RequestMapping("/bonus")
public class BonusController {

    @Autowired
    private BonusService bonusService;

    @PostMapping
    public ResponseEntity<String> handleBonus(@RequestBody BonusModel bonusRequest) throws InterruptedException {
        try {
            Long userId = bonusRequest.getUser();
            Integer bonus = bonusRequest.getBonus();

            bonusService.processBonus(userId, bonus);
            return ResponseEntity.ok("bonus added");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Can't proccess bonus");
        }
    }

    @GetMapping
    public ResponseEntity<UserModel> getUser(@PathParam("userID") Long userID) {
        try {
            UserModel user = bonusService.findUser(userID);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}
