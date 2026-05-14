package com.example.demo.controller;

import com.example.demo.dto.CashFlowDTO;
import com.example.demo.service.CashFlowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cash-flows")
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
public class CashFlowController {

    @Autowired
    private CashFlowService cashFlowService;

    // GET /api/cash-flows/list/{userId}
    @GetMapping("/list/{userId}")
    public ResponseEntity<List<CashFlowDTO>> getHistory(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(cashFlowService.getByUserId(userId));
    }

    // POST /api/cash-flows/add
    @PostMapping("/add")
    public ResponseEntity<CashFlowDTO> addRecord(
            @RequestBody CashFlowDTO dto) {
        return ResponseEntity.ok(cashFlowService.addRecord(dto));
    }

    // DELETE /api/cash-flows/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRecord(
            @PathVariable("id") Long id) {
        cashFlowService.deleteRecord(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<CashFlowDTO> updateRecord(
            @PathVariable("id") Long id,
            @RequestBody CashFlowDTO dto) {
        return ResponseEntity.ok(cashFlowService.updateRecord(id, dto));
    }
}