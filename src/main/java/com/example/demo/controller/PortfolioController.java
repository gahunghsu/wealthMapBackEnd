package com.example.demo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.constant.RiskLevel;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

/*
 * 提供投資組合建議
*/
@RestController
@RequestMapping("/api/portfolio")
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
public class PortfolioController {

    @Autowired
    private UserRepository userRepository;

//    @GetMapping("/recommend/{userId}")
//    public ResponseEntity<?> getRecommendedPortfolio(@PathVariable("userId") Long userId) {
//        try {
//            // 1. 去資料庫找這個人，並拿出他的風險屬性
//            User user = userRepository.findById(userId)
//                    .orElseThrow(() -> new RuntimeException("找不到使用者"));
//            
//            String level = user.getRiskLevel();
//            if (level == null ) {
//                return ResponseEntity.badRequest().body("該使用者尚未進行風險評估");
//            }
//            
//            if (level == null || level.isEmpty()) {
//                return ResponseEntity.badRequest().body("該使用者尚未進行風險評估");
//            }
//            
//            List<Map<String, String>> recommendations = new ArrayList<>();
//
//            // 3. 根據屬性給予不同的推薦標的 (你可以自由修改這些標的)
//            switch (level) {
//                case "CONSERVATIVE": // 保守型
//                    recommendations.add(Map.of("name", "美國短期公債 ETF", "symbol", "SHV", "type", "債券", "description", "極低風險，適合資金停泊"));
//                    recommendations.add(Map.of("name", "綜合債券 ETF", "symbol", "BND", "type", "債券", "description", "穩定配息，波動小"));
//                    break;
//                case "DEFENSIVE": // 穩健型
//                    recommendations.add(Map.of("name", "全球投資級公司債", "symbol", "LQD", "type", "債券", "description", "收益率優於公債，風險可控"));
//                    recommendations.add(Map.of("name", "高股息 ETF", "symbol", "VYM", "type", "股票", "description", "挑選高配息大型股，相對抗跌"));
//                    break;
//                case "BALANCED": // 平衡型
//                    recommendations.add(Map.of("name", "標普500指數 ETF", "symbol", "VOO", "type", "股票", "description", "追蹤美國前500大企業，長期穩健"));
//                    recommendations.add(Map.of("name", "全球總體債券 ETF", "symbol", "BNDW", "type", "債券", "description", "分散單一國家風險"));
//                    break;
//                case "GROWTH": // 積極型
//                    recommendations.add(Map.of("name", "納斯達克100 ETF", "symbol", "QQQ", "type", "股票", "description", "聚焦科技巨頭，成長動能強"));
//                    recommendations.add(Map.of("name", "全美股市 ETF", "symbol", "VTI", "type", "股票", "description", "包辦美國大中小企業，捕捉全面成長"));
//                    break;
//                case "AGGRESSIVE": // 衝刺型 (你截圖的等級)
//                    recommendations.add(Map.of("name", "資訊科技板塊 ETF", "symbol", "VGT", "type", "股票", "description", "高波動、高報酬，專注尖端科技"));
//                    recommendations.add(Map.of("name", "半導體 ETF", "symbol", "SOXX", "type", "股票", "description", "掌握 AI 與晶片產業爆發力"));
//                    break;
//                default:
//                    return ResponseEntity.badRequest().body("未知的風險屬性");
//            }
//
//            // 4. 打包回傳給前端
//            Map<String, Object> response = new HashMap<>();
//            response.put("riskLevel", level);
//            response.put("recommendations", recommendations);
//
//            return ResponseEntity.ok(response);
//
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body("獲取推薦組合失敗：" + e.getMessage());
//        }
//    }
    
//  by carly
//  支援兩路徑：帶 ID 查資料庫，或直接帶 Level (訪客用)
//    @GetMapping({"/recommend/{level}", "/recommend/{level}/{userId}"})
//    public AppResponse<?> getRecommendedPortfolio(@PathVariable("level") String level,@PathVariable(value = "userId", required = false) Long userId) {
    @GetMapping({"/recommend/{level}"})
    public AppResponse<?> getRecommendedPortfolio(@PathVariable("level") String level) {
        
        try {
            // 如果有 userId 且 level 為 null 時，才去查資料庫 (備援邏輯)
            // 但現在我們讓前端直接傳 level 過來，這樣效能最好且支援訪客
            
            List<Map<String, Object>> recommendations = new ArrayList<>();

            // 🌟 根據你的三種風險類別，給予台灣市場標的
            switch (level) {
                case "CONSERVATIVE": // 保守型 (權益15, 固定80, 另類5)
                    recommendations.add(createItem("元大美債20年", "00679B", "固定收益", "穩定配息，避險首選 (RR2)"));
                    recommendations.add(createItem("元大AAA至A級公司債", "00751B", "固定收益", "高評等公司債，風險極低 (RR2)"));
                    recommendations.add(createItem("元大台灣高股息", "0056", "權益型資產", "老牌高股息，波動相對小 (RR3)"));
                    break;

                case "DEFENSIVE": // 穩健型 (權益45, 固定45, 另類10)
                    recommendations.add(createItem("元大台灣50", "0050", "權益型資產", "追蹤台股龍頭，長期成長穩健 (RR4)"));
                    recommendations.add(createItem("復華台灣科技優息", "00929", "權益型資產", "穩定月配息，抗波動性佳 (RR4)"));
                    recommendations.add(createItem("群益投資級金融債", "00724B", "固定收益", "金融龍頭債券，收益穩定 (RR2)"));
                    recommendations.add(createItem("群益台灣地產ETF", "00712", "另類投資", "分散配置，抗通膨首選 (RR4)"));
                    break;

                case "GROWTH": // 積極型 (權益75, 固定15, 10)
                    recommendations.add(createItem("富邦台美高科技", "0052", "權益型資產", "聚焦半導體與AI，成長動能強 (RR5)"));
                    recommendations.add(createItem("國泰費城半導體", "00830", "權益型資產", "跨國布局半導體龍頭 (RR5)"));
                    recommendations.add(createItem("元大S&P黃金", "00635U", "另類投資", "避險與抗通膨之商品資產 (RR5)"));
                    break;

                default:
                    return AppResponse.error(RspCode.PARAM_ERROR, "未知的風險屬性");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("riskLevel", level);
            result.put("recommendations", recommendations);

            return AppResponse.success(result);

        } catch (Exception e) {
            return AppResponse.error(RspCode.INTERNAL_SERVER_ERROR, "獲取推薦失敗");
        }
    }

    private Map<String, Object> createItem(String name, String symbol, String type, String desc) {
        return Map.of("name", name, "symbol", symbol, "type", type, "description", desc);
    }

    
    
}