package com.ecommerce.ecommerce_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Bonus {
  private Long user;
  private Integer bonus;
}
