package com.poptrade.common.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poptrade.common.result.Result;
import com.poptrade.common.util.JwtUtil;
import com.poptrade.common.util.UserContext;
import com.poptrade.entity.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * JWT 拦截器 —— 校验 Token，设置用户上下文。
 * <p>管理端接口要求 role=0（管理员），普通接口仅校验 Token 有效性。</p>
 */
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String token = extractToken(request);
        if (token == null) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        Claims claims = jwtUtil.parse(token);
        if (claims == null) {
            writeUnauthorized(response, "Token 无效或已过期，请重新登录");
            return false;
        }

        Long userId = jwtUtil.getUserId(claims);
        Integer role = jwtUtil.getRole(claims);

        // 管理端接口校验角色
        String path = request.getRequestURI();
        if (path.startsWith("/api/admin/") && role != User.ROLE_ADMIN) {
            writeForbidden(response, "无权限，仅管理员可访问");
            return false;
        }

        UserContext.set(userId, role);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 从 Authorization 请求头中提取 Bearer Token */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(Result.CODE_UNAUTHORIZED, msg)));
    }

    private void writeForbidden(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(200);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(
                Result.error(Result.CODE_FORBIDDEN, msg)));
    }
}
