package com.inventory.controller;

import com.inventory.common.JwtUtil;
import com.inventory.common.Result;
import com.inventory.entity.User;
import com.inventory.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = "$2a$10$Nk0Zq0x.khGqRdRkQ1W2QeLqB.1U8/Y2VxA3bC4dE5fG6hI7jK8lM";
        System.out.println(encoder.matches("123456", encoded));
        System.out.println(encoder.encode("123456"));
    }

    @PostMapping("/login")
    public Result<Map<String, String>> login(@RequestBody User loginReq) {
        System.out.println("收到登录请求: " + loginReq);  // 加这行
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>()
                .eq("username", loginReq.getUsername())
        );

        if (user == null || !passwordEncoder.matches(loginReq.getPassword(), user.getPassword())) {
            return Result.error(401, "用户名或密码错误");
        }

        String token = JwtUtil.generateToken(user.getUsername());
        Map<String, String> data = new HashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        return Result.success(data);
    }
}