package com.example.excel.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class Product {
  private Long id;

  private String name;

  private LocalDateTime createdAt;
}
