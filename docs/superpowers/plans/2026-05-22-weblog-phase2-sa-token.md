# 阶段二：sa-token 认证模块 实现计划

> **供执行代理使用：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐个实现。

**目标：** 使用 sa-token 替代 Spring Security，实现 JWT 登录认证链路。POST /login 调通并返回 JWT Token，后续请求自动校验 Token 并加载角色。

**架构：** sa-token 拦截器 + JWT 模式。登录在 Controller 中手动验密后调用 `StpUtil.login()`，角色由 `StpInterfaceImpl` 每次请求从 DB 实时加载。

**依赖：** 阶段一骨架必须已通过 `mvn compile`。

---

### 任务 1：父 POM 添加 sa-token 依赖管理

**涉及文件：**
- 修改：`pom.xml`

- [ ] **步骤 1：添加版本属性**

```xml
<sa-token.version>1.42.0</sa-token.version>
```

- [ ] **步骤 2：添加依赖管理**

```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
    <version>${sa-token.version}</version>
</dependency>
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
    <version>${sa-token.version}</version>
</dependency>
```

- [ ] **步骤 3：验证 POM 有效**

```bash
cd "E:/git_codes/Web_Blog" && mvn validate
```

预期：BUILD SUCCESS

---

### 任务 2：更新 weblog-module-jwt 模块 POM

**涉及文件：**
- 修改：`weblog-module-jwt/pom.xml`

- [ ] **步骤 1：替换依赖**

删除 `spring-boot-starter-security`、`jjwt-api`、`jjwt-impl`、`jjwt-jackson`，替换为：

```xml
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-spring-boot3-starter</artifactId>
</dependency>
<dependency>
    <groupId>cn.dev33</groupId>
    <artifactId>sa-token-jwt</artifactId>
</dependency>
```

同时修复 common 模块 artifact ID（如果是 `weblog-common` 改为 `weblog-module-common`）。

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 3：更新 weblog-module-admin 模块 POM

**涉及文件：**
- 修改：`weblog-module-admin/pom.xml`

- [ ] **步骤 1：添加 spring-security-crypto（仅密码加密，不含 Security 框架）**

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

> 说明：`spring-boot-starter-security` 已从 jwt 模块移除，admin 模块不再能传递依赖获取 `PasswordEncoder`。`spring-security-crypto` 是 Spring Security 的独立加密模块，不含过滤器链、认证管理器等重组件，与 sa-token 不冲突。

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-admin
```

预期：BUILD SUCCESS

---

### 任务 4：更新 application.yaml（sa-token 配置）

**涉及文件：**
- 修改：`weblog-web/src/main/resources/application.yaml`

- [ ] **步骤 1：替换 JWT 配置块**

```yaml
sa-token:
  token-name: Authorization
  token-style: jwt
  jwt-secret-key: "WeBlogSecretKeyForJWTTokenGeneration2024!@#$%^&*()"
  timeout: 86400
  active-timeout: -1
  is-concurrent: true
  is-share: false
  token-prefix: "Bearer "
```

> 配置说明：
> - `token-style: jwt` — 启用 JWT 模式，Token 为无状态 JWT 字符串
> - `timeout: 86400` — 24 小时过期（单位秒）
> - `token-name: Authorization` — 从 `Authorization` 请求头读取 Token
> - `token-prefix: "Bearer "` — 自动剥离前缀，与前端 Axios 拦截器一致
> - `active-timeout: -1` — 关闭"活跃超时"，Token 有效期固定

---

### 任务 5：删除 Spring Security 相关文件

**涉及文件：**
- 删除：`weblog-module-admin/.../config/WebSecurityConfig.java`
- 删除：`weblog-module-admin/.../config/UserDetailServiceImpl.java`
- 删除：`weblog-module-jwt/.../JwtTokenHelper.java`

- [ ] **步骤 1：删除三个文件**

```bash
rm weblog-module-admin/src/main/java/com/example/weblog/admin/config/WebSecurityConfig.java
rm weblog-module-admin/src/main/java/com/example/weblog/admin/config/UserDetailServiceImpl.java
rm weblog-module-jwt/src/main/java/com/example/weblog/jwt/JwtTokenHelper.java
```

- [ ] **步骤 2：保留 PasswordEncoderConfig**

`PasswordEncoderConfig.java` 仍然需要，但它依赖的 `PasswordEncoder` 和 `BCryptPasswordEncoder` 现在来自 `spring-security-crypto` 而非 `spring-boot-starter-security`。

---

### 任务 6：创建 SaTokenConfig（路由拦截配置）

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/example/weblog/jwt/SaTokenConfig.java`

- [ ] **步骤 1：创建路由拦截配置**

```java
package com.example.weblog.jwt;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    SaRouter.match("/admin/**")
                            .check(r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/**")
                .excludePathPatterns("/login");
    }
}
```

> 说明：`/admin/**` 全部需要登录，`/login` 放行，其他路径（前台接口）不拦截。与 Spring Security 的 `SecurityFilterChain` 相比，代码量减少 80%。

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

---

### 任务 7：创建 StpInterfaceImpl（角色权限加载）

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/example/weblog/admin/config/StpInterfaceImpl.java`

- [ ] **步骤 1：创建权限加载实现**

```java
package com.example.weblog.admin.config;

import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.common.domain.dos.UserRole;
import com.example.weblog.common.domain.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRoleMapper userRoleMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>().eq(UserRole::getUserName, loginId.toString()));
        return userRoles.stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());
    }
}
```

> 说明：sa-token 在每次权限校验时自动调用 `getRoleList()`，从 DB 实时加载角色。本项目只有角色（`ROLE_ADMIN` / `ROLE_VISITOR`），无细粒度权限码，所以 `getPermissionList()` 返回空列表。这替代了 Spring Security 的 `UserDetailsService.loadUserByUsername()`。

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-admin
```

---

### 任务 8：创建 AuthController（登录接口）

**涉及文件：**
- 新建：`weblog-module-jwt/src/main/java/com/example/weblog/jwt/controller/AuthController.java`

- [ ] **步骤 1：创建登录控制器**

```java
package com.example.weblog.jwt.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.common.domain.dos.User;
import com.example.weblog.common.domain.mapper.UserMapper;
import com.example.weblog.common.enums.ResponseCodeEnum;
import com.example.weblog.common.utils.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Response<Map<String, String>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUserName, username));
        if (user == null) {
            return Response.fail(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorCode(),
                    ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorMessage());
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Response.fail(ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorCode(),
                    ResponseCodeEnum.USERNAME_OR_PASSWORD_ERROR.getErrorMessage());
        }

        StpUtil.login(user.getUserName());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Response.success(Map.of("token", tokenInfo.getTokenValue()));
    }
}
```

> 对比 Spring Security：不需要 `AbstractAuthenticationProcessingFilter`、`AuthenticationManager`、`DaoAuthenticationProvider`、SuccessHandler、FailureHandler 等 6 个类。一个 Controller 方法完成所有认证逻辑。

- [ ] **步骤 2：全量编译验证**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile
```

预期：全部模块 BUILD SUCCESS

---

### 任务 9：验证登录 API

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
        "token": "eyJhbGciOiJIUzI1NiJ9..."
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

预期：不再是 401（该 API 将在阶段五实现，此时可能返回 404 或空数据，但不应该是 401）

- [ ] **步骤 4：测试未认证访问（无 Token）**

```bash
curl -X POST http://localhost:8081/admin/dashboard/article/statistics
```

预期：返回 401（sa-token 自动处理）

- [ ] **步骤 5：验证角色校验**

在后续业务 Controller 中使用注解：
```java
@SaCheckRole("ROLE_ADMIN")  // 只有管理员能访问
@PostMapping("/admin/article/publish")
```

或代码中：
```java
StpUtil.checkRole("ROLE_ADMIN");
```

---

**阶段二完成。** sa-token 认证全链路已打通：

- `POST /login` 用户凭证验证 → BCrypt 验密 → `StpUtil.login()` → JWT Token 生成 → 返回 JSON
- 后续请求自动从 `Authorization: Bearer <token>` 提取 Token → 校验 → 加载角色
- Token 过期/缺失 → 自动 401
- 权限不足 → `@SaCheckRole` 或 `StpUtil.checkRole()` 阻止访问
- 全部认证逻辑从 8 个类缩减为 3 个类（SaTokenConfig + StpInterfaceImpl + AuthController）
