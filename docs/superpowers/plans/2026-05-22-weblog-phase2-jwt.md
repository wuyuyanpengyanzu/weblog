# 阶段二：JWT 认证模块 实现计划

> **供执行代理使用：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐个实现。

**目标：** 完整实现 Spring Security 6.x + JWT 登录认证链路，POST /login 调通并返回 Token，后续请求自动校验 Token 并重建 SecurityContext。

**架构：** 双 Filter 设计——JwtAuthenticationLoginFilter 只处理登录请求，TokenAuthenticationFilter 处理后续请求。角色不存 Token，每次请求从 DB 重新加载。

**依赖：** 阶段一骨架必须已通过 `mvn compile`。

---

### 任务 1：完善 UserDetailServiceImpl（从 DB 加载用户）

**涉及文件：**
- 修改：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/service/impl/UserDetailServiceImpl.java`

- [ ] **步骤 1：重写 UserDetailServiceImpl，查询真实用户和角色**

将阶段一的占位实现替换为：

```java
package com.quanxiaoha.weblog.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quanxiaoha.weblog.common.domain.dos.UserDO;
import com.quanxiaoha.weblog.common.domain.dos.UserRoleDO;
import com.quanxiaoha.weblog.common.domain.mapper.UserMapper;
import com.quanxiaoha.weblog.common.domain.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDO userDO = userMapper.selectOne(
                new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, username));
        if (userDO == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }

        List<UserRoleDO> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRoleDO>().eq(UserRoleDO::getUsername, username));
        List<String> roles = userRoles.stream()
                .map(UserRoleDO::getRole)
                .collect(Collectors.toList());

        return User.builder()
                .username(userDO.getUsername())
                .password(userDO.getPassword())
                .roles(roles.toArray(new String[0]))
                .build();
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-admin
```

预期：BUILD SUCCESS

---

### 任务 2：创建登录成功/失败处理器

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/LoginAuthenticationSuccessHandler.java`
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/LoginAuthenticationFailureHandler.java`

- [ ] **步骤 1：创建 LoginAuthenticationSuccessHandler（登录成功 → 生成 Token → 返回 JSON）**

```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt"
```

```java
package com.quanxiaoha.weblog.jwt;

import com.quanxiaoha.weblog.common.Response;
import com.quanxiaoha.weblog.jwt.utils.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class LoginAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenHelper jwtTokenHelper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        String username = authentication.getName();
        String token = jwtTokenHelper.generateToken(username);

        Map<String, String> data = new HashMap<>();
        data.put("token", token);

        ResultUtil.writeJson(response, Response.success(data));
    }
}
```

- [ ] **步骤 2：创建 LoginAuthenticationFailureHandler（登录失败 → 返回错误 JSON）**

```java
package com.quanxiaoha.weblog.jwt;

import com.quanxiaoha.weblog.common.Response;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.jwt.utils.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                         HttpServletResponse response,
                                         AuthenticationException exception) throws IOException {
        ResultUtil.writeJson(response,
                Response.fail(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorCode(),
                        ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorMessage()));
    }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 3：创建未认证/权限不足处理器

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/RestAuthenticationEntryPoint.java`
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/RestAccessDeniedHandler.java`

- [ ] **步骤 1：创建 RestAuthenticationEntryPoint（Token 缺失/过期 → 401 JSON）**

```java
package com.quanxiaoha.weblog.jwt;

import com.quanxiaoha.weblog.common.Response;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.jwt.utils.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ResultUtil.writeJson(response,
                Response.fail(ResponseCodeEnum.UNAUTHORIZED.getErrorCode(),
                        ResponseCodeEnum.UNAUTHORIZED.getErrorMessage()));
    }
}
```

- [ ] **步骤 2：创建 RestAccessDeniedHandler（权限不足 → 403 JSON）**

```java
package com.quanxiaoha.weblog.jwt;

import com.quanxiaoha.weblog.common.Response;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import com.quanxiaoha.weblog.jwt.utils.ResultUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ResultUtil.writeJson(response,
                Response.fail(ResponseCodeEnum.FORBIDDEN.getErrorCode(),
                        ResponseCodeEnum.FORBIDDEN.getErrorMessage()));
    }
}
```

- [ ] **步骤 3：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 4：创建 JwtAuthenticationLoginFilter（只处理 POST /login）

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/JwtAuthenticationLoginFilter.java`

- [ ] **步骤 1：创建登录过滤器（JSON 解析凭证 → 触发认证）**

```java
package com.quanxiaoha.weblog.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;
import java.util.Map;

public class JwtAuthenticationLoginFilter extends AbstractAuthenticationProcessingFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationLoginFilter() {
        super(new AntPathRequestMatcher("/login", "POST"));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                 HttpServletResponse response)
            throws AuthenticationException, IOException {
        Map<String, String> loginData = objectMapper.readValue(request.getInputStream(), Map.class);
        String username = loginData.get("username");
        String password = loginData.get("password");

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(username, password);
        return this.getAuthenticationManager().authenticate(authToken);
    }
}
```

注意：不重写 `successfulAuthentication` 和 `unsuccessfulAuthentication` 方法——Spring Security 会自动调用我们在任务 2 中注册的 SuccessHandler 和 FailureHandler。

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 5：创建 TokenAuthenticationFilter（每个请求校验 Token）

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/TokenAuthenticationFilter.java`

- [ ] **步骤 1：创建 Token 校验过滤器**

```java
package com.quanxiaoha.weblog.jwt;

import com.quanxiaoha.weblog.common.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenHelper jwtTokenHelper;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (StringUtils.hasText(token) && jwtTokenHelper.validateToken(token)) {
            String username = jwtTokenHelper.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 6：创建 JwtAuthenticationSecurityConfig（组装认证链）

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/JwtAuthenticationSecurityConfig.java`

- [ ] **步骤 1：创建认证安全配置（DaoAuthenticationProvider + LoginFilter 注册）**

```java
package com.quanxiaoha.weblog.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationSecurityConfig
        extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final LoginAuthenticationSuccessHandler successHandler;
    private final LoginAuthenticationFailureHandler failureHandler;

    @Override
    public void configure(HttpSecurity http) {
        JwtAuthenticationLoginFilter loginFilter = new JwtAuthenticationLoginFilter();
        loginFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
        loginFilter.setAuthenticationSuccessHandler(successHandler);
        loginFilter.setAuthenticationFailureHandler(failureHandler);

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);

        http.authenticationProvider(provider)
                .addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 7：重写 WebSecurityConfig（完整的 SecurityFilterChain）

**涉及文件：**
- 修改：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/WebSecurityConfig.java`

- [ ] **步骤 1：替换占位 WebSecurityConfig 为完整配置**

```java
package com.quanxiaoha.weblog.admin.config;

import com.quanxiaoha.weblog.jwt.JwtAuthenticationSecurityConfig;
import com.quanxiaoha.weblog.jwt.RestAccessDeniedHandler;
import com.quanxiaoha.weblog.jwt.RestAuthenticationEntryPoint;
import com.quanxiaoha.weblog.jwt.TokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final JwtAuthenticationSecurityConfig jwtAuthenticationSecurityConfig;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;
    private final RestAccessDeniedHandler restAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(restAuthenticationEntryPoint)
                        .accessDeniedHandler(restAccessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/admin/**").authenticated()
                        .anyRequest().permitAll())
                .apply(jwtAuthenticationSecurityConfig);

        http.addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

- [ ] **步骤 2：全量编译验证**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile
```

预期：BUILD SUCCESS

---

### 任务 8：验证登录 API

启动应用（确保 MySQL 已建立 `weblog` 库并导入了 `sql/schema.sql` 和 `sql/data.sql`）：

```bash
cd "E:/git_codes/Web_Blog" && mvn spring-boot:run -pl weblog-web
```

- [ ] **步骤 1：测试登录成功（admin/admin）**

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'
```

预期响应：
```json
{
    "success": true,
    "errorCode": null,
    "message": null,
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9..."
    }
}
```

- [ ] **步骤 2：测试登录失败（错误密码）**

```bash
curl -X POST http://localhost:8081/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"wrong"}'
```

预期响应：
```json
{
    "success": false,
    "errorCode": "10006",
    "message": "用户名或密码错误",
    "data": null
}
```

- [ ] **步骤 3：测试 Token 校验（用登录成功返回的 token 访问受保护资源）**

```bash
curl -X POST http://localhost:8081/admin/dashboard/article/statistics \
  -H "Authorization: Bearer <替换为真实token>"
```

预期：不再是 401，返回业务数据（该 API 将在阶段五实现，此时可能返回 404 或空数据，但不应该是 401）

- [ ] **步骤 4：测试未认证访问**

```bash
curl -X POST http://localhost:8081/admin/dashboard/article/statistics
```

预期响应：
```json
{
    "success": false,
    "errorCode": "10007",
    "message": "未登录或Token已过期",
    "data": null
}
```

---

**阶段二完成。** JWT 认证全链路已打通：

- `POST /login` 用户凭证验证 → JWT Token 生成 → 返回 JSON
- 后续请求从 `Authorization: Bearer <token>` 提取 Token → 校验 → 重建 SecurityContext（含角色信息）
- Token 过期/缺失 → 401 JSON
- 权限不足 → 403 JSON
- 密码校验由 Spring Security 内部 `DaoAuthenticationProvider` + `BCryptPasswordEncoder` 完成
