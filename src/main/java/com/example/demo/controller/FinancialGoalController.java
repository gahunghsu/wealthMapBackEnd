package com.example.demo.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;

import com.example.demo.entity.FinancialGoal;
import com.example.demo.entity.User;
import com.example.demo.service.FinancialGoalService;

@RestController
@RequestMapping("/api/goals") // 💡 這是前端發球機要瞄準的網址
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true") // 💡 打開跨域防護罩，允許 Angular 連線
public class FinancialGoalController {

    @Autowired
    private FinancialGoalService goalService;

    // 🚀 1. 新增財務目標 (對應前端的 POST)
    @PostMapping("/{userId}")
    public ResponseEntity<FinancialGoal> createGoal(@PathVariable("userId") Long userId, @RequestBody FinancialGoal goal) {
        // 把目標綁定給這個 User
        User user = new User();
        user.setId(userId);
        goal.setUser(user);

        // 防呆機制：如果是剛建好的目標，目前已存金額預設為 0
        if (goal.getCurrentAmount() == null) {
            goal.setCurrentAmount(0.0);
        }

        FinancialGoal savedGoal = goalService.createGoal(goal);
        return ResponseEntity.ok(savedGoal);
    }

    // 🚀 2. 取得會員的所有目標 (對應前端的 GET 列表)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FinancialGoal>> getGoalsByUserId(@PathVariable("userId") Long userId) {
        List<FinancialGoal> goals = goalService.getGoalsByUserId(userId);
        return ResponseEntity.ok(goals);
    }

    // 🚀 3. 刪除目標 (對應前端的 DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGoal(@PathVariable("id") Long id) {
        goalService.deleteGoal(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<FinancialGoal> updateGoal(
            @PathVariable("id") Long id,
            @RequestBody FinancialGoal goal) {
        return ResponseEntity.ok(goalService.updateGoal(id, goal));
    }
    
}