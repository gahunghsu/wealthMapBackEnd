package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.example.demo.entity.StrategySetting;
import com.example.demo.entity.User;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.StrategySettingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.StockService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

import jakarta.validation.Valid;

import com.example.demo.dto.StrategyDTO;
import com.example.demo.dto.StrategyRequestDTO;
import com.example.demo.dto.StrategyResponseDTO;

@RestController
@RequestMapping("/api/strategy-set")
@CrossOrigin(origins = "${cors.allowed.origins}") // 允許 Angular 存取
public class StrategySettingController {

	@Autowired
    private StrategySettingRepository strategyRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
	private StockService stockService;
	
    
    // 1. 取得該使用者的所有加減碼策略 
    @GetMapping("/user/{userId}")
    public AppResponse<List<StrategyResponseDTO>> getByUserId(@PathVariable("userId") Long userId) {
        List<StrategySetting> settings = strategyRepository.findByUserId(userId);
        
        // 手動轉換成 DTO
        List<StrategyResponseDTO> dtoList = settings.stream()
            .map(s -> new StrategyResponseDTO(
                s.getId(), 
                s.getSymbol(), 
                s.getBuyThreshold(), 
                s.getSellThreshold(), 
                s.isActive(),
                s.getUser().getId()
            )).toList();
        
        // 使用你定義的 success 靜態方法封裝
        return AppResponse.success(dtoList);
    }
    
    // 2. 新增加減碼策略 (對應你的 addStrategy() 彈窗提交)
    @PostMapping("/user/{userId}")
    public AppResponse<?> create(@PathVariable("userId") Long userId, @RequestBody @Valid StrategyRequestDTO request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // 檢查是否重複，使用你的 AppResponse.error 格式回傳
        if (strategyRepository.existsByUserIdAndSymbol(userId, request.symbol())) {
            // 假設你的 RspCode 裡有 DATA_ALREADY_EXISTS 或類似定義
            return AppResponse.error(RspCode.DUPLICATE_ERROR, "該股票策略已存在");
        }
            
        StrategySetting setting = new StrategySetting();
        setting.setUser(user);
        setting.setSymbol(request.symbol());
        setting.setBuyThreshold(request.buyThreshold());
        setting.setSellThreshold(request.sellThreshold());
        setting.setActive(true);
        
        StrategySetting saved = strategyRepository.save(setting);
        
        // 回傳成功訊息與轉換後的 DTO
        return AppResponse.success(StrategyRequestDTO.fromEntity(saved));
    }
    
	 // 3. 更改加減碼策略 (對應 saveEdit())
	 // 建議使用 @PutMapping 並對應資源 ID
	 @PutMapping("/{id}") 
	 public AppResponse<StrategyRequestDTO> updateStrategySetting(@PathVariable("id") Long id, @RequestBody @Valid StrategyRequestDTO request) {
	    
	    // 1. 尋找現有策略 (orElseThrow 確保 ID 存在)
	    StrategySetting setting = strategyRepository.findById(id)
	        .orElseThrow(() -> new RuntimeException("找不到該策略設定 ID: " + id));

	    // 2. 更新 Entity 數值
	    // 從 RequestDTO 取得前端修改後的門檻值與開關狀態
	    setting.setBuyThreshold(request.buyThreshold());
	    setting.setSellThreshold(request.sellThreshold());
	    setting.setActive(request.isActive()); 

	    // 3. 執行儲存
	    StrategySetting saved = strategyRepository.save(setting);

	    // 4. 回傳統一格式的 AppResponse，並將更新後的資料轉成 DTO 帶回
	    return AppResponse.success(StrategyRequestDTO.fromEntity(saved));
	}
    
    // 4. 刪除加減碼策略
	 @DeleteMapping("/{id}")
	 public AppResponse<Void> delete(@PathVariable("id") Long id) {
	     // 1. 先檢查該 ID 是否存在 (避免刪除不存在的資料報錯)
	     if (!strategyRepository.existsById(id)) {
	         // 假設你的 RspCode 裡有 DATA_NOT_FOUND
	         return AppResponse.error(RspCode.NOT_FOUND, "找不到該策略，無法刪除");
	     }
	     
	     // 2. 執行刪除
	     strategyRepository.deleteById(id);
	     
	     // 3. 回傳成功，因為刪除不需要帶回資料，Data 傳 null 即可
	     // 你也可以自定義訊息為 "策略已成功刪除"
	     return AppResponse.success(null);
	 }
    
    // 在「新增彈跳視窗」中，讓使用者從他的持股中選擇 (加減碼策略使用)。
    @GetMapping("/user/available-stocks/{userId}")
    public ResponseEntity<List<String>> getAvailableStocks(@PathVariable("userId") Long userId) {
        // 從 AssetRepository 撈出該用戶目前持有的所有 symbol
    	List<String> symbols = assetRepository.findAvailableSymbolsByUserId(userId);
        return ResponseEntity.ok(symbols);
    }
    
    //抓取該股票的現價跟乖離率
    @GetMapping("/quote/{symbol}")
    public AppResponse<StrategyDTO> getStockQuote(@PathVariable("symbol") String symbol) {
    	 StrategyDTO quote = stockService.getQuickQuote(symbol);
         if (quote == null) {
             return AppResponse.error(RspCode.NOT_FOUND, "無法取得該股票報價");
         }
         return AppResponse.success(quote);
    }

	
    
}
