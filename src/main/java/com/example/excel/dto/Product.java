package com.example.excel.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  private Long id;

  private String name;

  private LocalDateTime createdAt;
}
