package com.health.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS = 30;
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(1);

    private final Map<String, SlidingWindow> store = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();
        if (!path.startsWith("/api/ai/")) {
            return true;
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        String key = ip + ":" + path;

        long now = System.currentTimeMillis();
        SlidingWindow window = store.computeIfAbsent(key, k -> new SlidingWindow(now));

        synchronized (window) {
            if (now - window.windowStart > WINDOW_MS) {
                window.windowStart = now;
                window.count = 0;
            }
            if (window.count >= MAX_REQUESTS) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(429);
                response.getWriter().write("{\"code\":429,\"msg\":\"请求过于频繁，请稍后再试\"}");
                return false;
            }
            window.count++;
        }
        return true;
    }

    private static class SlidingWindow {
        long windowStart;
        int count;

        SlidingWindow(long start) {
            this.windowStart = start;
            this.count = 0;
        }
    }
}
