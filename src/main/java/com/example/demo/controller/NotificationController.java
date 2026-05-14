package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.dto.NotificationListDTO;
import com.example.demo.entity.AlertLog;
import com.example.demo.entity.Notification;
import com.example.demo.repository.AlertLogRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

import jakarta.validation.Valid;



@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "${cors.allowed.origins}", allowCredentials = "true")
public class NotificationController {
	
	@Configuration
	public class WebConfig implements WebMvcConfigurer {

	    @Override
	    public void addCorsMappings(CorsRegistry registry) {
	        registry.addMapping("/**")
	                .allowedOrigins("${cors.allowed.origins}") // ✅ 必須明確指定，不能用 "*"
	                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
	                .allowedHeaders("*")
	                .allowCredentials(true) // ✅ 這行就是解決你報錯的關鍵
	                .maxAge(3600); // 預檢請求(Preflight)的快取時間
	    }
	}

    @Autowired
    private NotificationService notificationService;	// 系統通知

    /**
     * 1. 取得所有公告列表 (GET)
     * 用於前端渲染 notificationList 陣列
     */
    @GetMapping("/list")
    public AppResponse<List<Notification>> getList() {
        List<Notification> list = notificationService.getNotificationList();
        return AppResponse.success(list);
    }

    /**
     * 2. 新增公告 (POST)
     * 💡 使用 @Valid 觸發 DTO 中的 @NotBlank 驗證
     */
    @PostMapping("/save")
    public AppResponse<Notification> save(@Valid @RequestBody NotificationListDTO dto) {
        // 新增時不應該帶有 ID
        dto.setId(null);
        Notification saved = notificationService.saveNotification(dto);
        return AppResponse.success(saved);
    }

    /**
     * 3. 更新公告 (PUT)
     */
    @PutMapping("/update")
    public AppResponse<Notification> update(@Valid @RequestBody NotificationListDTO dto) {
        // 💡 檢查是否有提供 ID，若無則回傳你定義的 PARAM_ERROR
        if (dto.getId() == null) {
            return AppResponse.error(RspCode.PARAM_ERROR, "更新公告時必須提供 ID");
        }
        
        try {
            Notification updated = notificationService.saveNotification(dto);
            return AppResponse.success(updated);
        } catch (RuntimeException e) {
            return AppResponse.error(RspCode.NOT_FOUND, e.getMessage());
        }
    }

    /**
     * 4. 刪除公告 (DELETE)
     * 透過 URL 傳入 ID，例如: /api/notifications/5
     */
    @DeleteMapping("/{id}")
    public AppResponse<Void> delete(@PathVariable("id") Long id) {
        try {
            notificationService.deleteNotification(id);
            return AppResponse.success(null);
        } catch (RuntimeException e) {
            return AppResponse.error(RspCode.NOT_FOUND, e.getMessage());
        }
    }
    
    //5.取得單筆
    @GetMapping("/{id}")
    public ResponseEntity<AppResponse<Notification>> getNotificationById(@PathVariable("id") Long id) {
        Notification notification = notificationService.findById(id);
        
        if (notification != null) {
            // ✅ 使用你定義的 success 靜態方法，它會自動處理 RspCode.SUCCESS
            return ResponseEntity.ok(AppResponse.success(notification));
        } else {
            // ✅ 使用你定義的 error 靜態方法，傳入對應的錯誤列舉
            // 假設你的 RspCode 裡面有 NOT_FOUND 或 DATA_NOT_FOUND
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(AppResponse.error(RspCode.NOT_FOUND)); 
        }
    }

    /**
     * 6. 取得通知未讀數 (GET)，新增個人未讀 (系統通知+個人通知)
     */
    @GetMapping("/unread-count-new")
    public AppResponse<Map<String, Long>> getUnreadCount(@RequestParam("userId") Long userId) {
        // 調用 service 取得 Map
    	Map<String, Long> counts = notificationService.getDetailedUnreadCounts(userId);
        return AppResponse.success(counts);
    }

    /**
     * 7. 標記系統公告為已讀 (POST)
     * 💡 當使用者點擊公告內容時呼叫
     */
    @PostMapping("/read")
    public AppResponse<Void> markAsRead( @RequestParam("userId") Long userId, 
            @RequestParam("notificationId") Long notificationId) {
        
    	notificationService.markAsRead(userId, notificationId);
        return AppResponse.success(null);
    }
    
    /**
     * 8. 取得「包含已讀狀態」的公告列表 (GET)
     * 💡 用於前端顯示列表，並決定是否顯示紅點
     */
    @GetMapping("/list-with-status")
    public AppResponse<List<NotificationListDTO>> getListWithStatus(@RequestParam("userId") Long userId) {
        // 💡 呼叫剛才修正過 hasRead 邏輯的 Service 方法
        List<NotificationListDTO> list = notificationService.getNotificationListWithStatus(userId);
        return AppResponse.success(list);
    }
    
	@Autowired
    private AlertLogRepository alertLogRepository;       // 個人通知 by carly
    
    /**
     * Alert Log個人通知的表
     * 1. 使用者是否點開個人通知，以標示已讀
     */
 	@PatchMapping("/{id}/read")
 	public AppResponse<String> markAsRead(@PathVariable("id") Long id) {
 	    return alertLogRepository.findById(id)
 	        .map(log -> {
 	            log.setRead(true);
 	            // log.setReadAt(LocalDateTime.now()); // 如果你決定不留，這行就拿掉
 	            alertLogRepository.save(log);
 	            return AppResponse.success("已讀成功");
 	        })
 	        .orElse(AppResponse.error(RspCode.NOT_FOUND,"找不到該通知紀錄"));
 	}
 	
 	/**
     * 查看個人通知(Alert Log)的表
     * 2. 取得使用者的個人通知
     */
 	@GetMapping("/{userId}/personal-list")
 	public AppResponse<List<AlertLog>> getPersonalAlerts( @PathVariable("userId") Long userId) {
 	    // 調用 Repository 抓取該用戶特定的 Web_Push 訊息
 	    List<AlertLog> logs = notificationService.getPersonalAlerts(userId);
 	    return AppResponse.success(logs);
 	}

}