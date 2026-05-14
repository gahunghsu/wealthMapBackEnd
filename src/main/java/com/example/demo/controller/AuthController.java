package com.example.demo.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.UserAdminViewDTO;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AuthService;
import com.example.demo.service.EmailService;
import com.example.demo.vo.AppResponse;
import com.example.demo.vo.RspCode;

/**
 * 【迪士尼票務櫃檯】 這裡是遊客進入迪士尼樂園的第一站。 櫃檯人員（Controller）負責收件，核對沒問題後，就會請後台核發魔法手環。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
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
	private AuthService authService;
	
	@Autowired
	private UserRepository userRepository;

	/**
	 * 【登入入口 - 領取今日魔法手環】 已經有會員身分的遊客，憑信箱與密碼來換取手環。
	 * 生活例子：就像你到樂園門口，刷出你的電子購票證明，換取一個感應手環入園。
	 */
	@PostMapping("/login")
	public AppResponse<LoginResponseDTO> login(@RequestBody LoginDTO loginDTO) {
		try {
			// 呼叫後台主管製作一個專屬手環 (Token)
			String token = authService.login(loginDTO);
//			String role = authService.getRole(loginDTO);
			return AppResponse.success(new LoginResponseDTO(token));
		} catch (Exception e) {
			// 身分核對失敗，不能隨便放人入園喔
			return AppResponse.error(RspCode.UNAUTHORIZED, "Invalid email or password");
		}
	}

	/**
	 * 【註冊入口 - 成為迪士尼新會員】 第一次來的遊客，需要填寫詳細資料建檔。 生活例子：填寫迪士尼官方會員申請表，設定好你的專屬暗號（密碼）。
	 */
	@PostMapping("/register")
	public AppResponse<LoginResponseDTO> register(@RequestBody RegisterDTO registerDTO) {
		try {
			// 呼叫主管進行新遊客建檔
			String token = authService.register(registerDTO);
			// 建檔成功，貼心地直接給他手環，祝他玩得愉快
			return AppResponse.success(new LoginResponseDTO(token));
		} catch (RuntimeException e) {
			// 這個信箱已經註冊過了，可能你以前來過？
			return AppResponse.error(RspCode.DUPLICATE_ERROR, e.getMessage());
		} catch (Exception e) {
			// 樂園系統維護中或其他意外
			return AppResponse.error(RspCode.INTERNAL_SERVER_ERROR, "Registration failed");
		}
	}
	
	//忘記密碼寄信
	@GetMapping("/send-mail")
	public AppResponse<String> sendForgotPasswordMail(@RequestParam("to") String to) {
	    try {
	        // 呼叫整合後的方法，裡面已經包含產生亂碼、存資料庫、寄信
	        authService.processForgotPassword(to);
	        
	        return AppResponse.success("發送成功！請檢查信箱");
	    } catch (Exception e) {
	        return AppResponse.error(RspCode.NOT_FOUND, e.getMessage());
	    }
	}
	
	//修改密碼
	@PostMapping("/change-password")
	public AppResponse<String> changePassword(@RequestBody ChangePasswordDTO dto, Authentication auth) {
	    // 💡 關鍵點：從 SecurityContext (Token) 拿 Email，而不是從前端傳來的 DTO 拿
	    String currentLoginEmail = auth.getName(); 
	    
	    // 這樣不論前端傳什麼，後端永遠只會修改「目前登入者」的資料
	    authService.updateUserPassword(currentLoginEmail, dto);
	    
	    return AppResponse.success("修改成功");
	}
	
	//管理者的全使用者清單
	@GetMapping("/user-list")
	public AppResponse<List<UserAdminViewDTO>> getAllUsers(Authentication auth) {
        // 1. 從 Token 中獲取目前登入者的權限
//        boolean isAdmin = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        if (!isAdmin) {
//            return AppResponse.error(RspCode.FORBIDDEN, "權限不足，僅限管理員查看");
//        }

        // 2. 撈取所有使用者並轉換為輕量級 DTO
        List<UserAdminViewDTO> userList = userRepository.findAll().stream()
                .map(user -> {
                	UserAdminViewDTO dto = new UserAdminViewDTO();
                    dto.setId(user.getId());
                    dto.setName(user.getName());
                    dto.setEmail(user.getEmail());
                    dto.setRole(user.getRole());
                    dto.setRiskLevel(user.getRiskLevel());
                    dto.setEnabled(user.getEnabled()); // 這是你預計新增的狀態欄位
                    return dto;
                }).toList();

        return AppResponse.success(userList);
    }
	
	// 停用或啟用使用者帳號
    @PatchMapping("/{userId}/enabled")
    public AppResponse<String> toggleUserEnabled(@PathVariable("userId") Long userId, Authentication auth) {
        // 1. 安全檢查：同樣只允許管理員操作
//        boolean isAdmin = auth.getAuthorities().stream()
//                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
//
//        if (!isAdmin) {
//            return AppResponse.error(RspCode.FORBIDDEN, "權限不足，無法變更使用者狀態");
//        }

        // 2. 執行狀態切換
        return userRepository.findById(userId).map(user -> {
            // 反轉目前的狀態 (!true = false, !false = true)
            user.setEnabled(!user.getEnabled());
            userRepository.save(user);
            
            String status = user.getEnabled() ? "啟用" : "停用";
            return AppResponse.success("使用者 [" + user.getName() + "] 已成功" + status);
        }).orElse(AppResponse.error(RspCode.NOT_FOUND, "找不到該使用者"));
    }
	
}