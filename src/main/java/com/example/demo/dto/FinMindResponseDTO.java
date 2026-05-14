package com.example.demo.dto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FinMindResponseDTO extends BaseFinMindResponse<StockDataDTO>{
	private String msg;
	private int status;
	private List<StockDataDTO> data;
}
