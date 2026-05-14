package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Liability;
import com.example.demo.entity.User;
import com.example.demo.service.LiabilityService;

@RestController
@RequestMapping("/api/liabilities") // 💡 前端呼叫的 API 開頭
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
public class LiabilityController {

    @Autowired
    private LiabilityService liabilityService;

 // 1. 取得某使用者的所有負債 (GET)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Liability>> getUserLiabilities(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(liabilityService.getLiabilitiesByUserId(userId));
    }

    // 2. 新增負債 (POST)
    @PostMapping("/user/{userId}")
    public ResponseEntity<Liability> addLiability(@PathVariable("userId") Long userId, @RequestBody Liability liability) {
        User user = new User();
        user.setId(userId);
        liability.setUser(user);
        return ResponseEntity.ok(liabilityService.createLiability(liability));
    }

    // 3. 刪除負債 (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLiability(@PathVariable("id") Long id) {
        liabilityService.deleteLiability(id);
        return ResponseEntity.ok().build();
    }
    
 // 4. 修改負債 (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<Liability> updateLiability(
            @PathVariable("id") Long id,
            @RequestBody Liability liability) {
        return ResponseEntity.ok(liabilityService.updateLiability(id, liability));
    }
    
    }