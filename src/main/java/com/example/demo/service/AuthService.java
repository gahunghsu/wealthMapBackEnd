package com.example.demo.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtTokenProvider;

/**
 * 【迪士尼後台辦公室】
 * 這裡是樂園的決策核心。負責管理遊客通訊錄、核發手環、
 * 以及各種安全檢查任務。
 */
@Service
public class AuthService {

    // 負責管理所有設施保全的經理
    @Autowired
    private AuthenticationManager authenticationManager;

    // 樂園的歷史遊客檔案櫃
    @Autowired
    private UserRepository userRepository;

    // 專門幫遊客密碼「上鎖」的工具
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private EmailService emailService;

    // 魔法手環 (MagicBand) 的製作與燒錄機
    @Autowired
    private JwtTokenProvider tokenProvider;
    

    /**
     * 【核對資料並製作手環】
     */
    public String login(LoginDTO loginDTO) {
        // 第一步：經理親自核對你的「身分證」與「暗號」
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getEmail(),
                        loginDTO.getPassword()
                )
        );

        // 第二步：核對無誤，將你的狀態標註為「已在園內活動中」
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("AUTH: " + SecurityContextHolder.getContext().getAuthentication());
        
        userRepository.findByEmail(loginDTO.getEmail()).ifPresent(user -> {
            if (userRepository.count() == 1 && "USER".equals(user.getRole())) {
                user.setRole("ADMIN");
                userRepository.save(user);
                System.out.println("Disney Note: Promoted the first guest [" + user.getEmail() + "] to Park Manager.");
            }
        });

        // 第三步：啟動手環製作機，把你的身分燒錄進去，回報給票務櫃檯
        return tokenProvider.generateToken(authentication);
    }

    /**
     * 【新遊客建檔】
     */
    public String register(RegisterDTO registerDTO) {
        // 先查查檔案，看看這名字（Email）是不是有人用了
        if (userRepository.findByEmail(registerDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered in Disney system.");
        }

        // 幫新朋友建檔
        User user = new User();
        user.setName(registerDTO.getName());
        user.setEmail(registerDTO.getEmail());
        
        // 重點：密碼要像米奇寶藏一樣加密，不能直接存在資料庫！
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        
        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }

        // 存入檔案櫃，永續保存
        userRepository.save(user);
        
        // 建檔完畢，直接跳轉到「領手環」程序，祝你玩得開心
        return login(new LoginDTO() {{
            setEmail(registerDTO.getEmail());
            setPassword(registerDTO.getPassword());
        }});
    }
    
    /**
     * 【忘記密碼：寄送臨時密碼】
     * 邏輯與註冊一致：產生亂碼 -> 加密存入 -> 異步寄信
     */
    @Async // 確保非同步執行，前端才不會等 //寄信用
    @Transactional
    public void processForgotPassword(String email) {
        // 1. 檢查這個 Email 是不是我們 WealthMap 的遊客
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到該電子信箱，請確認輸入是否正確。"));

        // 2. 產生 8 位隨機臨時密碼（就像隨機生成的遊園編號）
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("臨時密碼" + tempPassword);
        // 3. 重點：臨時密碼也要加密！這跟 register 裡的寫法完全一樣
        // 這樣使用者拿這串亂碼登入時，passwordEncoder.matches 才能比對成功
        user.setPassword(passwordEncoder.encode(tempPassword));
        
        // 存入檔案櫃
        userRepository.save(user);

        // 4. 準備寄送 Email 通知使用者
        try {
            emailService.sendSimpleEmail(email, "【WealthMap】您的臨時登入密碼", buildEmailContent(tempPassword));
            System.out.println("✅ 臨時密碼已成功寄送至: " + email);
        } catch (Exception e) {
            System.err.println("❌ 郵件發送失敗: " + e.getMessage());
        }
    }

    /**
     * 構建 Email 內容（保持專業且友善的語氣）
     */
    private String buildEmailContent(String tempPassword) {
        return "親愛的使用者您好：\n\n"
                + "系統收到您在 WealthMap 的密碼重設請求。為了保護您的資產安全，我們已為您產生了一個隨機的臨時密碼：\n\n"
                + "臨時密碼：[" + tempPassword + "]\n"
                + "(請注意區分大小寫，建議直接複製使用)\n\n"
                + "💡 提醒：登入成功後，請立即前往「個人檔案」修改為您的專屬密碼。\n\n"
                + "如果您並未要求重設密碼，請忽略此郵件。\n\n"
                + "WealthMap 開發團隊 敬上";
    }
    
    //修改密碼
    @Transactional
    public void updateUserPassword(String email, ChangePasswordDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("使用者不存在"));

        // 🛡️ 雙重保險：驗證舊密碼
        // 就算 Token 被盜，小偷不知道你的「臨時密碼」或「舊密碼」，他也改不了
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("舊密碼（臨時密碼）輸入不正確！");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

}