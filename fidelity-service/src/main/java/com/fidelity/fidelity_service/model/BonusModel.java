package com.fidelity.fidelity_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BonusModel {
  private Long user;
    private Integer bonus;
}