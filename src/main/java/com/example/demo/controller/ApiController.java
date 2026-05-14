package com.example.demo.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Asset;
import com.example.demo.entity.StockPrice;
import com.example.demo.entity.TaiwanStockList;
import com.example.demo.repository.AlertLogRepository;
import com.example.demo.repository.AssetRepository;
import com.example.demo.repository.StockPriceRepository;
import com.example.demo.repository.TaiwanStockListRepository;
import com.example.demo.service.StockService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;
@RestController
@RequestMapping("/api/strategy-api")
@CrossOrigin(origins = "${cors.allowed.origins}") // 允許 Angular 存取
public class ApiController {
	
	//	股價測試
	@Autowired
	private StockService stockService;
	
	@Autowired
	private AssetRepository assetRepository;
	
	@Autowired
    private StockPriceRepository stockPriceRepository;
	
	@Autowired
    private AlertLogRepository alertLogRepository;
	
	@Autowired
    private TaiwanStockListRepository stockListRepository;

	
	// 寄送加減碼通知 for 手動測試
	@GetMapping("/send-notification")
	public String sendStrategyMail() {
		try {
			stockService.executeFetch();
			return "系統連接成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "連接失敗: " + e.getMessage();
		}
	}
	
    
	// 取得單一股票最近 20 天價格 (供 Chart.js 畫圖)
	@GetMapping("/stock-history/{symbol}")
	public AppResponse<List<StockPrice>> getStockHistory(@PathVariable("symbol") String symbol) {
		List<StockPrice> history = stockPriceRepository.findTop20BySymbolOrderByDateDesc(symbol);
		// 注意：回傳前可以先用 Collections.reverse(history) 讓時間軸由舊到新，方便畫圖
		Collections.reverse(history);
		return AppResponse.success(history);
	}
	
	// 抓取台股總覽列表 for 手動測試
	@GetMapping("/stock-list")
	public String fetchStockList() {
		try {
			stockService.fetchTWStockApi();
			return "系統連接成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "連接失敗: " + e.getMessage();
		}
	}
	
	
	
	// 抓取台股總覽列表 for 手動測試
	@GetMapping("/update-asset-stock")
	public String updateAssetStock() {
		try {
			assetRepository.updateStockAssetsAmount();
			return "資產股價更新成功！";
		} catch (Exception e) {
			e.printStackTrace();
			return "資產股價更新失敗: " + e.getMessage();
		}
	}
	
	// 輸入股票代碼帶出代碼名稱
	// put in AssetController
//	@GetMapping("/search-stock/{stock_id}")
//	public AppResponse<TaiwanStockList> searchStock(@PathVariable("stock_id") String stock_id) {
//		return stockListRepository.findById(stock_id)
//	            .map(stock -> AppResponse.success(stock))
//	            .orElseGet(() -> AppResponse.error(RspCode.NOT_FOUND)); 
//	}

}
