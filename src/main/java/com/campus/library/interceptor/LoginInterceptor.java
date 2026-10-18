package com.campus.library.interceptor;

import com.campus.library.common.Result;
import com.campus.library.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器。
 * 规则：
 * 1. OPTIONS（CORS 预检）一律放行
 * 2. 登录/注册放行
 * 3. 图书与分类的 GET 查询放行（游客可浏览）
 * 4. 其余请求必须带合法 JWT
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        if (path.equals("/api/auth/login") || path.equals("/api/auth/register")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)
                && (path.startsWith("/api/books") || path.startsWith("/api/categories"))) {
            return true;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return unauthorized(response);
        }
        Claims claims = jwtUtil.parse(header.substring(7));
        if (claims == null) {
            return unauthorized(response);
        }
        request.setAttribute("userId", Long.valueOf(claims.getSubject()));
        request.setAttribute("username", claims.get("username", String.class));
        request.setAttribute("role", claims.get("role", String.class));
        return true;
    }

    private boolean unauthorized(HttpServletResponse response) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(401, "未登录或登录已过期")));
        return false;
    }
}
