package com.example.demo.controller;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;



import java.util.HashMap;

import java.util.Map;

import com.example.demo.dto.RiskAssessmentRequest;
import com.example.demo.dto.StrategyResponse;
import com.example.demo.entity.RiskAssessment;

import com.example.demo.entity.User;

import com.example.demo.service.RiskAssessmentService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

import jakarta.validation.Valid;



@RestController

@RequestMapping("/api/risk")

@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")

public class RiskController {



    @Autowired

    private RiskAssessmentService riskService;


//	  delete by carly	
   // old risk evaluate function
//    @PostMapping("/evaluate-old")
//
//    public ResponseEntity<?> evaluateRisk(@RequestBody Map<String, Object> payload) {
//
//
//
//        try {
//
//            Long userId = ((Number) payload.get("userId")).longValue();
//
//
//
//            RiskAssessment assessment = new RiskAssessment();
//
//            assessment.setAgeScore(((Number) payload.get("ageScore")).intValue());
//
//            assessment.setAllocationScore(((Number) payload.get("allocationScore")).intValue());
//
//            assessment.setDurationScore(((Number) payload.get("durationScore")).intValue());
//
//            assessment.setExperienceScore(((Number) payload.get("experienceScore")).intValue());
//
//            assessment.setKnowledgeScore(((Number) payload.get("knowledgeScore")).intValue());
//
//            assessment.setToleranceScore(((Number) payload.get("toleranceScore")).intValue());
//
//
//
//            User user = new User();
//
//            user.setId(userId);
//
//            assessment.setUser(user);
//
//
//
//            // 1. 存進資料庫
//
//            RiskAssessment savedResult = riskService.evaluateAndSave(assessment);
//
//
//
//            // ==========================================
//
//            // 🌟 2. 開始打包前端需要的 StrategyResponse 大禮包
//
//            // ==========================================
//
//            Map<String, Object> response = new HashMap<>();
//
//            String level = savedResult.getRiskLevel();
//
//            response.put("userLevel", level);
//
//
//
//            // 簡單判斷：如果年紀大(分數低)但風險承受度超高(分數高)，就跳警告
//
//            boolean isOverMatch = (assessment.getAgeScore() <= 2 && assessment.getToleranceScore() >= 4);
//
//            response.put("isRiskOverMatch", isOverMatch);
//
//
//
//            String advice = "";
//
//            Map<String, Integer> allocation = new HashMap<>();
//
//
//
//            // 根據等級，塞入對應的專家建議和圓餅圖比例
//
//            switch(level) {
//
//                case "CONSERVATIVE":
//
//                    advice = "您屬於保守型投資人，無法承受過大資金波動。建議以保本為首要目標，將大部分資金配置於低風險的定存與高評等債券。";
//
//                    allocation.put("現金與定存", 60);
//
//                    allocation.put("政府債券", 30);
//
//                    allocation.put("大型穩健股", 10);
//
//                    break;
//
//                case "DEFENSIVE":
//
//                    advice = "您屬於穩健型投資人，能在承擔微小風險的前提下追求穩定收益。建議以債券為主，搭配少部分股票。";
//
//                    allocation.put("現金與定存", 30);
//
//                    allocation.put("投資級債券", 50);
//
//                    allocation.put("大型股/ETF", 20);
//
//                    break;
//
//                case "BALANCED":
//
//                    advice = "您屬於平衡型投資人，願意承受適度風險以換取合理報酬。股債平衡是您最好的選擇。";
//
//                    allocation.put("現金與定存", 10);
//
//                    allocation.put("債券", 40);
//
//                    allocation.put("股票/ETF", 50);
//
//                    break;
//
//                case "GROWTH":
//
//                    advice = "您屬於積極型投資人，追求資本長線增值，能忍受市場較大的波動。建議拉高股票資產的比重。";
//
//                    allocation.put("現金", 10);
//
//                    allocation.put("債券", 20);
//
//                    allocation.put("股票/ETF", 70);
//
//                    break;
//
//                case "AGGRESSIVE":
//
//                    advice = "您屬於衝刺型投資人，追求最高報酬，對短線劇烈波動不以為意。可考慮高成長股或科技股等資產。";
//
//                    allocation.put("現金", 5);
//
//                    allocation.put("高收益債", 10);
//
//                    allocation.put("股票/高風險資產", 85);
//
//                    break;
//
//            }
//
//
//
//            response.put("advice", advice);
//
//            response.put("allocation", allocation);
//
//
//
//            // 3. 把打包好的完美 JSON 送給前端！
//
//            return ResponseEntity.ok(response);
//
//
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            return ResponseEntity.badRequest().body("資料解析失敗：" + e.getMessage());
//
//        }
//
//    }

    // fix visitor can fill by carly
    @PostMapping("/evaluate")
    public AppResponse<?> evaluateRisk(@RequestBody @Valid RiskAssessmentRequest request) {
        try {
            // 1. 決定是否要進行會員存檔
            // 若 userId > 0 -> 找 User、存資料庫、回傳結果
            // 若 userId = 0 -> 不找 User、不存資料庫、直接計算回傳
            StrategyResponse result = riskService.evaluateRisk(request);
            
            // 2. 回傳封裝後的成功結果
            return AppResponse.success(result);

        } catch (RuntimeException e) {
            // 捕捉如 "User not found" 等業務異常
            return AppResponse.error(RspCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return AppResponse.error(RspCode.INTERNAL_SERVER_ERROR, "評估解析失敗");
        }
    }
    
    /**
     * 會員回顧模式：根據 UserId 與目前的 RiskLevel 抓取含有分數的歷史紀錄
     */
    @GetMapping("/last-riskresult")
    public AppResponse<?> getRiskResultByRiskLevel( @RequestParam("user_id") Long userId, @RequestParam("level") String level) {
        try {
            // 呼叫 Service 進行比對查詢
            StrategyResponse result = riskService.getRiskResult(userId, level);
            return AppResponse.success(result);
        } catch (RuntimeException e) {
            // 找不到紀錄時的回傳
            return AppResponse.error(RspCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            return AppResponse.error(RspCode.INTERNAL_SERVER_ERROR, "系統查詢失敗");
        }
    }
    
    
}