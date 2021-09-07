package com.example.excel.controller;

import com.example.excel.dto.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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
   * upload
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

  /**
   * download
   *
   * @return file
   * @throws IOException output stream
   */
  @GetMapping("/v1")
  public ResponseEntity<byte[]> fetchV1() throws IOException {
    StopWatch watch = new StopWatch("DatabaseToExcelV1");

    watch.start("FetchProducts");
    List<Product> products = jdbcTemplate.query(
        "select id, name, created_at as createdAt from product limit ?",
        new BeanPropertyRowMapper<>(Product.class),
        1000000
    );
    log.info("count: {}", products.size());
    watch.stop();

    watch.start("SetProducts");
    SXSSFWorkbook workbook = new SXSSFWorkbook();
    Sheet sheet = workbook.createSheet();
    Row row;
    Cell cell;
    int rowNum = 0;
    for (Product product : products) {
      row = sheet.createRow(rowNum++);
      cell = row.createCell(0);
      cell.setCellValue(product.getId());

      cell = row.createCell(1);
      cell.setCellValue(product.getName());

      cell = row.createCell(2);
      cell.setCellValue(product.getCreatedAt().toString());
    }
    watch.stop();

    watch.start("CreateStream");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    workbook.write(out);
    workbook.dispose();
    byte[] buffer = out.toByteArray();
    out.close();
    watch.stop();

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
    headers.setContentDisposition(ContentDisposition.attachment().filename("Products.xlsx").build());

    log.info(watch.prettyPrint());
    return ResponseEntity.ok()
        .headers(headers)
        .body(buffer);
  }
}
