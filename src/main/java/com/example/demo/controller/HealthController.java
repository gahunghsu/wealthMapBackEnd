package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.AssetGrowthDTO;
import com.example.demo.dto.HealthResponseDTO;
import com.example.demo.service.HealthService;

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
public class HealthController {

	@Autowired
	private HealthService healthService;

    @GetMapping("/{userId}")
    public HealthResponseDTO calculate(
    		@PathVariable("userId") Long userId) {
    	
        return healthService.calculate(userId);
    }
    
    @GetMapping("/growth/{userId}")
    public List<AssetGrowthDTO> getGrowth(
            @PathVariable("userId") Long userId){

        return healthService.getAssetGrowth(userId);

    }
}


//	@PostMapping
//	public Map<String, Object> calculateHealth(@RequestBody HealthRequestDTO req){
//		Map<String, Object> res = new HashMap<>();
//
//	    Map<String, Object> rawInfo = new HashMap<>();
//
//	    rawInfo.put("income", req.getIncome());
//	    rawInfo.put("expense", req.getExpense());
//
//	    Map<String, Object> assets = new HashMap<>();
//	    assets.put("savings", req.getSavings());
//	    assets.put("cash", req.getCash());
//
//	    Map<String, Object> debts = new HashMap<>();
//	    debts.put("mortgage", req.getMortgage());
//	    debts.put("carLoan", req.getCarLoan());
//	    debts.put("personalLoan", req.getPersonalLoan());
//	    debts.put("creditCard", req.getCreditCard());
//
//	    rawInfo.put("assets", assets);
//	    rawInfo.put("debts", debts);
//	    rawInfo.put("investmentSuccessRate", req.getInvestmentSuccessRate());
//
//	    res.put("rawInfo", rawInfo);
//
//	    return res;
//	}

//	@GetMapping
//	public Map<String, Object> getHealth() {
//
//	    Map<String, Object> res = new HashMap<>();
//
//	    Map<String, Object> rawInfo = new HashMap<>();
//	    rawInfo.put("income", req.getIncome());
//	    rawInfo.put("expense", 40000);
//
//	    Map<String, Object> assets = new HashMap<>();
//	    assets.put("savings", 200000);
//	    assets.put("cash", 40000);
//
//	    Map<String, Object> debts = new HashMap<>();
//	    debts.put("mortgage", 20000);
//	    debts.put("carLoan", 5000);
//	    debts.put("personalLoan", 0);
//	    debts.put("creditCard", 3000);
//
//	    rawInfo.put("assets", assets);
//	    rawInfo.put("debts", debts);
//	    rawInfo.put("investmentSuccessRate", 85);
//
//	    res.put("rawInfo", rawInfo);
//
//	    return res;
//	}
