package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Asset;
import com.example.demo.entity.TaiwanStockList;
import com.example.demo.entity.User;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.TaiwanStockListRepository;
import com.example.demo.service.AssetService;
import com.example.demo.service.StockService; 
import com.example.demo.dto.StrategyDTO;
import com.example.demo.dto.AssetDTO; 
import com.example.demo.dto.ApiResponseDTO;
import com.example.demo.dto.TwStockListDTO;
import com.example.demo.service.StockReferenceService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/assets") 
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true") 
public class AssetController {

    @Autowired
    private AssetService assetService;

    @Autowired
    private StockReferenceService stockRefService;
    
    @Autowired
    private StockService stockService;
    
    @Autowired
    private AssetRepository assetRepository;
    
    
	@Autowired
    private TaiwanStockListRepository stockListRepository;
    // ---------------------------------------------------------
    // 1. 新增一筆資產 (前端 POST)
    @PostMapping("/{userId}")
    public ResponseEntity<AssetDTO> createAsset(@PathVariable("userId") Long userId, @RequestBody AssetDTO assetDTO) {
        
        Asset asset = new Asset();
        asset.setName(assetDTO.name()); 
        asset.setType(assetDTO.type());
        
        // 處理 amount 為 null 的防呆機制
        Double finalAmount = assetDTO.amount();
        if (finalAmount == null) {
            // 如果前端沒傳 amount（例如股票模式下只算 totalCost），
            // 我們就自動拿 totalCost 來頂替，避免資料庫生氣報錯！
            finalAmount = assetDTO.cost();
        }
        asset.setAmount(finalAmount); 
        
        asset.setSymbol(assetDTO.stockId());
        asset.setShares(assetDTO.sharesOwned());
        asset.setCost(assetDTO.cost());

        User user = new User();
        user.setId(userId);
        asset.setUser(user);

        // 存入資料庫
        Asset savedAsset = assetService.createAsset(asset);
        // 將 Entity 轉回 DTO
        AssetDTO savedDTO = new AssetDTO(
            savedAsset.getId(),
            savedAsset.getName(),
            savedAsset.getType(),
            savedAsset.getAmount(),
            savedAsset.getSymbol(),
            savedAsset.getShares(),
            savedAsset.getCost()
        );

        return ResponseEntity.ok(savedDTO);
    }

    // 獲取某個使用者的所有資產
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AssetDTO>> getAssetsByUserId(@PathVariable("userId") Long userId) {
        
        List<Asset> assets = assetService.getAssetsByUserId(userId);
        
        // 將 List<Asset> 轉為 List<AssetDTO>
        List<AssetDTO> assetDTOs = assets.stream()
            .map(asset -> new AssetDTO(
                asset.getId(),
                asset.getName(),
                asset.getType(),
                asset.getAmount(),
                asset.getSymbol(), 
                asset.getShares(),asset.getCost()
            ))
            .collect(Collectors.toList());

        return ResponseEntity.ok(assetDTOs);
    }
    
    // ---------------------------------------------------------
    // 3. 刪除資產 (前端 DELETE)
    // ---------------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(@PathVariable("id") Long id) {
        assetService.deleteAsset(id);
        return ResponseEntity.ok().build(); 
    }
    
    // 4. 修改資產 (前端 PUT)
    @PutMapping("/{id}")
    public ResponseEntity<AssetDTO> updateAsset(@PathVariable("id") Long id, @RequestBody AssetDTO assetDTO) {
        
        // 呼叫 Service 執行更新 (這就是我們剛剛在 AssetService 準備好的引擎)
        Asset updatedAsset = assetService.updateAsset(id, assetDTO);

        // 將更新後的 Entity 轉回 DTO 傳給前端
        AssetDTO updatedDTO = new AssetDTO(
            updatedAsset.getId(),
            updatedAsset.getName(),
            updatedAsset.getType(),
            updatedAsset.getAmount(),
            updatedAsset.getSymbol(),
            updatedAsset.getShares(),
            updatedAsset.getCost()
        );

        return ResponseEntity.ok(updatedDTO);
    }

    // ---------------------------------------------------------
    // 4. 輸入股票代碼帶出代碼名稱 by carly
    // ---------------------------------------------------------
    @GetMapping("/search-stock/{stock_id}")
	public AppResponse<TaiwanStockList> searchStock(@PathVariable("stock_id") String stock_id) {
		return stockListRepository.findById(stock_id)
	            .map(stock -> AppResponse.success(stock))
	            .orElseGet(() -> AppResponse.error(RspCode.NOT_FOUND)); 
	}
    
//    @GetMapping("/sync-stocks-now")
//    public ResponseEntity<String> syncTaiwanStocksManually() {
//        stockService.fetchTWStockApi(); 
//        return ResponseEntity.ok("✅ 手動觸發台股清單同步成功！請查看後端 Console 確認進度。");
//    }
    

    // 在「新增彈跳視窗」中，讓使用者從他的持股中選擇 (資產再平衡使用)。
    @GetMapping("/rebalance/available-stocks/{userId}")
    public ResponseEntity<List<String>> getRebalanceAvailableStocks(@PathVariable("userId") Long userId) {
        // 從 AssetRepository 撈出該用戶目前持有的所有 symbol
    	List<String> symbols = assetRepository.findRebalanceAvailableSymbolsByUserId(userId);
        return ResponseEntity.ok(symbols);
    }
    
    
}