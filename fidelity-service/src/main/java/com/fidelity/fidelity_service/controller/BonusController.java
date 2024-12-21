package com.fidelity.fidelity_service.controller;

import java.util.List;

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
            Long userId = bonusRequest.getUserID();
            Integer bonus = bonusRequest.getBonus();

            bonusService.processBonus(userId, bonus);
            return ResponseEntity.ok("Bonus successfully added to user with ID: " + userId);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("Request is taking longer than expected. Please try again later.");
        }
    }

    @PostMapping("/list")
    public ResponseEntity<String> handleBonusList(@RequestBody List<BonusModel> bonusRequest)
            throws InterruptedException {
        try {
            bonusService.processBonuses(bonusRequest);
            return ResponseEntity.ok("All bonuses in the list have been successfully added.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    "Request is taking longer than expectedes. Please try again.");
        }
    }

    @GetMapping
    public ResponseEntity<UserModel> getUser(@PathParam("userID") Long userID) {
        try {
            UserModel user = bonusService.findUser(userID);

            if (user == null) {
                return ResponseEntity.status(404).body(null);
            }

            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/status")
    public ResponseEntity<String> handleStatus() {
        return ResponseEntity.ok(bonusService.BonusEndpointStatus());
    }
}
