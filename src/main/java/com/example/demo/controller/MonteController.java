package com.example.demo.controller;

import com.example.demo.dto.MonteDTO;
import com.example.demo.dto.MonteResponseDTO;
import com.example.demo.service.MonteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/monte")
@CrossOrigin(origins = "${cors.allowed.origins}", allowedHeaders = "*") 
public class MonteController {
	
    @Autowired
    private MonteService monteService;

    @PostMapping("/simulate/{userId}")
    public ResponseEntity<MonteResponseDTO> simulate(
            @PathVariable("userId") Long userId,
            @RequestBody MonteDTO dto) {
    	MonteResponseDTO result = monteService.calculateSimulation(
    			userId,               
                dto.getMonthly(),     
                dto.getYears(),         
                dto.getInitialAmount(), 
                dto.getAllocations(),   
                dto.getInflationRate()  
            );
    	return ResponseEntity.ok(result);
        }        
}