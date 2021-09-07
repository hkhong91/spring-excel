package com.example.excel.controller;

import com.example.excel.dto.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

  private final JdbcTemplate jdbcTemplate;

  /**
   * save at once
   * ---------------------------------------------
   * sec         %     Task name
   * ---------------------------------------------
   * 20.410314497  048%  CreateWorkbook
   * 5.127343926  012%  SetProducts
   * 17.357501694  040%  InsertProducts
   *
   * @param file multipart file
   * @throws IOException input stream
   */
  @PostMapping("/v1")
  public void createV1(@RequestPart("file") MultipartFile file) throws IOException {
    StopWatch watch = new StopWatch("ExcelToDatabaseV1");

    watch.start("CreateWorkbook");
    Workbook workbook = WorkbookFactory.create(file.getInputStream());
    watch.stop();

    watch.start("SetProducts");
    Sheet sheet = workbook.getSheetAt(0);
    List<Product> products = new ArrayList<>();
    Iterator<Row> rows = sheet.iterator();
    rows.forEachRemaining(row -> {
      Cell cell = row.getCell(0);
      String productName = cell.getStringCellValue();
      products.add(Product.builder().name(productName).build());
    });
    watch.stop();

    watch.start("InsertProducts");
    LocalDateTime now = LocalDateTime.now();
    jdbcTemplate.batchUpdate("insert into product (name, created_at) values (?, ?)",
        products,
        10000,
        (ps, argument) -> {
          ps.setObject(1, argument.getName(), Types.VARCHAR);
          ps.setObject(2, now, Types.TIMESTAMP);
        }
    );
    watch.stop();

    log.info(watch.prettyPrint());
  }
}
