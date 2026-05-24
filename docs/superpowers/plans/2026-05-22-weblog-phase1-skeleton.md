# 阶段一：项目骨架 实现计划

> **供执行代理使用：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 按任务逐个实现。步骤使用 checkbox（`- [ ]`）语法追踪。

**目标：** 搭建完整的 Maven 多模块项目骨架，包含全部 DO、Mapper、公共类及配置——在 Spring Boot 3.4.5 上编译通过并启动成功。

**架构：** 四个 Maven 模块（common → jwt → admin → web），基于 Spring Boot 3.4.5、MyBatis Plus 3.5.9 和 Spring Security 6.x。全程使用 Jakarta EE 命名空间。

**技术栈：** Spring Boot 3.4.5、MyBatis Plus 3.5.9、jjwt 0.12.6、Minio 8.5.11、Guava 33.4.0、flexmark 0.64.8、ip2region 2.7.0、MapStruct 1.6.3、p6spy 1.10.0

---

### 任务 1：父 POM，含依赖管理

**涉及文件：**
- 修改：`pom.xml`

- [ ] **步骤 1：重写父 POM**

将当前 `pom.xml` 替换为完整的依赖管理：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.5</version>
        <relativePath/>
    </parent>

    <groupId>com.quanxiaoha.weblog</groupId>
    <artifactId>Web_Blog</artifactId>
    <version>1.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>weblog-module-common</module>
        <module>weblog-module-jwt</module>
        <module>weblog-module-admin</module>
        <module>weblog-web</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <mybatis-plus.version>3.5.9</mybatis-plus.version>
        <jjwt.version>0.12.6</jjwt.version>
        <minio.version>8.5.11</minio.version>
        <guava.version>33.4.0-jre</guava.version>
        <flexmark.version>0.64.8</flexmark.version>
        <ip2region.version>2.7.0</ip2region.version>
        <mapstruct.version>1.6.3</mapstruct.version>
        <p6spy.version>1.10.0</p6spy.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>com.baomidou</groupId>
                <artifactId>mybatis-plus-jsqlparser</artifactId>
                <version>${mybatis-plus.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
                <scope>runtime</scope>
            </dependency>
            <dependency>
                <groupId>io.minio</groupId>
                <artifactId>minio</artifactId>
                <version>${minio.version}</version>
            </dependency>
            <dependency>
                <groupId>com.google.guava</groupId>
                <artifactId>guava</artifactId>
                <version>${guava.version}</version>
            </dependency>
            <dependency>
                <groupId>com.vladsch.flexmark</groupId>
                <artifactId>flexmark-all</artifactId>
                <version>${flexmark.version}</version>
            </dependency>
            <dependency>
                <groupId>org.lionsoul</groupId>
                <artifactId>ip2region</artifactId>
                <version>${ip2region.version}</version>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
            <dependency>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </dependency>
            <dependency>
                <groupId>com.github.gavlyukovskiy</groupId>
                <artifactId>p6spy-spring-boot-starter</artifactId>
                <version>${p6spy.version}</version>
            </dependency>
            <dependency>
                <groupId>com.mysql</groupId>
                <artifactId>mysql-connector-j</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 2：验证 POM 有效**

```bash
cd "E:/git_codes/Web_Blog" && mvn validate
```

预期：BUILD SUCCESS

---

### 任务 2：创建 weblog-module-common 模块 POM

**涉及文件：**
- 新建：`weblog-module-common/pom.xml`

- [ ] **步骤 1：创建目录和 POM**

```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-module-common/src/main/java/com/quanxiaoha/weblog/common"
mkdir -p "E:/git_codes/Web_Blog/weblog-module-common/src/main/resources"
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.quanxiaoha.weblog</groupId>
        <artifactId>Web_Blog</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>weblog-module-common</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-spring-boot3-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-jsqlparser</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
        </dependency>
        <dependency>
            <groupId>com.google.guava</groupId>
            <artifactId>guava</artifactId>
        </dependency>
        <dependency>
            <groupId>org.lionsoul</groupId>
            <artifactId>ip2region</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS（可能提示暂无源码）

---

### 任务 3：创建 11 个 DO 实体类

**涉及文件：**
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/UserDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/UserRoleDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/ArticleDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/ArticleContentDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/ArticleCategoryRelDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/ArticleTagRelDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/CategoryDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/TagDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/BlogSettingDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/StatisticsArticlePVDO.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/dos/VisitorRecordDO.java`

- [ ] **步骤 1：创建 UserDO（用户表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user")
public class UserDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **步骤 2：创建 UserRoleDO（用户角色表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_user_role")
public class UserRoleDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String role;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **步骤 3：创建 ArticleDO（文章表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_article")
public class ArticleDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String titleImage;

    private String description;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;

    private Integer readNum;
}
```

- [ ] **步骤 4：创建 ArticleContentDO（文章正文表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_article_content")
public class ArticleContentDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private String content;
}
```

- [ ] **步骤 5：创建 ArticleCategoryRelDO（文章-分类关联表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_article_category_rel")
public class ArticleCategoryRelDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long categoryId;
}
```

- [ ] **步骤 6：创建 ArticleTagRelDO（文章-标签关联表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_article_tag_rel")
public class ArticleTagRelDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long tagId;
}
```

- [ ] **步骤 7：创建 CategoryDO（分类表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_category")
public class CategoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **步骤 8：创建 TagDO（标签表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_tag")
public class TagDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
```

- [ ] **步骤 9：创建 BlogSettingDO（博客设置表，单例）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_blog_setting")
public class BlogSettingDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String blogName;

    private String author;

    private String introduction;

    private String avatar;

    private String githubHome;

    private String csdnHome;

    private String giteeHome;

    private String zhihuHome;
}
```

- [ ] **步骤 10：创建 StatisticsArticlePVDO（PV 统计表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_statistics_article_pv")
public class StatisticsArticlePVDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate pvDate;

    private Long pvCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

- [ ] **步骤 11：创建 VisitorRecordDO（访客记录表）**

```java
package com.quanxiaoha.weblog.common.domain.dos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_visitor_record")
public class VisitorRecordDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String visitor;

    private String ipAddress;

    private String ipRegion;

    private LocalDateTime visitTime;

    private Integer isNotify;
}
```

- [ ] **步骤 12：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS

---

### 任务 4：创建 11 个 Mapper 接口

**涉及文件：**
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/UserMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/UserRoleMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/ArticleMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/ArticleContentMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/ArticleCategoryRelMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/ArticleTagRelMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/CategoryMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/TagMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/BlogSettingMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/StatisticsArticlePVMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper/VisitorRecordMapper.java`

- [ ] **步骤 1：批量创建全部 Mapper 接口**

先创建目录：
```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/domain/mapper"
```

然后逐个创建 Mapper。需要使用批量插入功能的 Mapper 继承 `MyBaseMapper`，其余继承 `BaseMapper`：

**UserMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.UserDO;

public interface UserMapper extends BaseMapper<UserDO> {
}
```

**UserRoleMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.UserRoleDO;

public interface UserRoleMapper extends BaseMapper<UserRoleDO> {
}
```

**ArticleMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleDO;

public interface ArticleMapper extends BaseMapper<ArticleDO> {
}
```

**ArticleContentMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleContentDO;

public interface ArticleContentMapper extends BaseMapper<ArticleContentDO> {
}
```

**ArticleCategoryRelMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleCategoryRelDO;

public interface ArticleCategoryRelMapper extends BaseMapper<ArticleCategoryRelDO> {
}
```

**ArticleTagRelMapper.java（使用 MyBaseMapper 以支持批量插入）：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.quanxiaoha.weblog.common.config.MyBaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.ArticleTagRelDO;

public interface ArticleTagRelMapper extends MyBaseMapper<ArticleTagRelDO> {
}
```

**CategoryMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.CategoryDO;

public interface CategoryMapper extends BaseMapper<CategoryDO> {
}
```

**TagMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.TagDO;

public interface TagMapper extends BaseMapper<TagDO> {
}
```

**BlogSettingMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.BlogSettingDO;

public interface BlogSettingMapper extends BaseMapper<BlogSettingDO> {
}
```

**StatisticsArticlePVMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.StatisticsArticlePVDO;

public interface StatisticsArticlePVMapper extends BaseMapper<StatisticsArticlePVDO> {
}
```

**VisitorRecordMapper.java：**
```java
package com.quanxiaoha.weblog.common.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quanxiaoha.weblog.common.domain.dos.VisitorRecordDO;

public interface VisitorRecordMapper extends BaseMapper<VisitorRecordDO> {
}
```

- [ ] **步骤 2：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS

---

### 任务 5：MyBatis Plus 扩展与配置

**涉及文件：**
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/config/MyBaseMapper.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/config/InsertBatchSqlInjector.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/config/MybatisPlusConfig.java`

- [ ] **步骤 1：创建 MyBaseMapper（支持批量插入）**

```java
package com.quanxiaoha.weblog.common.config;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MyBaseMapper<T> extends BaseMapper<T> {

    int insertBatchSomeColumn(@Param("list") List<T> batchList);
}
```

- [ ] **步骤 2：创建 InsertBatchSqlInjector（注入批量插入 SQL）**

```java
package com.quanxiaoha.weblog.common.config;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.extension.injector.methods.InsertBatchSomeColumn;

import java.util.List;

public class InsertBatchSqlInjector extends DefaultSqlInjector {

    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass, TableInfo tableInfo) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass, tableInfo);
        methodList.add(new InsertBatchSomeColumn());
        return methodList;
    }
}
```

- [ ] **步骤 3：创建 MybatisPlusConfig（分页插件 + SQL 注入器）**

```java
package com.quanxiaoha.weblog.common.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

    @Bean
    public InsertBatchSqlInjector insertBatchSqlInjector() {
        return new InsertBatchSqlInjector();
    }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS

---

### 任务 6：统一响应体与异常处理

**涉及文件：**
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/Response.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/PageResponse.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/exception/BaseExceptionInterface.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/exception/BizException.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/exception/GlobalExceptionHandler.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/enums/ResponseCodeEnum.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/constant/Constants.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/enums/EventEnum.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/eventbus/ArticleEvent.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/eventbus/EventListener.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/config/EventBusConfig.java`

- [ ] **步骤 1：创建 Response.java（统一响应体）**

```java
package com.quanxiaoha.weblog.common;

import lombok.Data;

@Data
public class Response<T> {

    private boolean success = true;
    private String errorCode;
    private String message;
    private T data;

    public static <T> Response<T> success(T data) {
        Response<T> response = new Response<>();
        response.setData(data);
        return response;
    }

    public static <T> Response<T> fail(String errorCode, String message) {
        Response<T> response = new Response<>();
        response.setSuccess(false);
        response.setErrorCode(errorCode);
        response.setMessage(message);
        return response;
    }
}
```

- [ ] **步骤 2：创建 PageResponse.java（分页响应体）**

```java
package com.quanxiaoha.weblog.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageResponse<T> extends Response<T> {

    private long total;
    private long size;
    private long current;
    private long pages;
}
```

- [ ] **步骤 3：创建 BaseExceptionInterface（错误码接口）**

```java
package com.quanxiaoha.weblog.common.exception;

public interface BaseExceptionInterface {

    String getErrorCode();

    String getErrorMessage();
}
```

- [ ] **步骤 4：创建 BizException（业务异常）**

```java
package com.quanxiaoha.weblog.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    public BizException(BaseExceptionInterface baseExceptionInterface) {
        super(baseExceptionInterface.getErrorMessage());
        this.errorCode = baseExceptionInterface.getErrorCode();
        this.errorMessage = baseExceptionInterface.getErrorMessage();
    }
}
```

- [ ] **步骤 5：创建 ResponseCodeEnum（错误码枚举）**

```java
package com.quanxiaoha.weblog.common.enums;

import com.quanxiaoha.weblog.common.exception.BaseExceptionInterface;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResponseCodeEnum implements BaseExceptionInterface {

    SYSTEM_ERROR("10000", "系统异常"),
    PARAM_ERROR("10001", "参数错误"),
    USERNAME_OR_PASSWORD_ERROR("10006", "用户名或密码错误"),
    UNAUTHORIZED("10007", "未登录或Token已过期"),
    FORBIDDEN("10008", "无权限访问"),
    CATEGORY_NAME_DUPLICATED("20001", "分类名称已存在"),
    TAG_NAME_DUPLICATED("20002", "标签名称已存在"),
    ARTICLE_NOT_FOUND("20003", "文章不存在"),
    CATEGORY_NOT_FOUND("20004", "分类不存在"),
    TAG_NOT_FOUND("20005", "标签不存在"),
    FILE_UPLOAD_ERROR("30001", "文件上传失败"),
    ;

    private final String errorCode;
    private final String errorMessage;
}
```

- [ ] **步骤 6：创建 GlobalExceptionHandler（全局异常处理）**

```java
package com.quanxiaoha.weblog.common.exception;

import com.quanxiaoha.weblog.common.Response;
import com.quanxiaoha.weblog.common.enums.ResponseCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    @ResponseBody
    public Response<?> handleBizException(BizException e) {
        log.warn("业务异常: code={}, message={}", e.getErrorCode(), e.getErrorMessage());
        return Response.fail(e.getErrorCode(), e.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Response<?> handleValidationException(MethodArgumentNotValidException e) {
        StringBuilder sb = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(error ->
                sb.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; "));
        return Response.fail(ResponseCodeEnum.PARAM_ERROR.getErrorCode(), sb.toString());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Response.fail(ResponseCodeEnum.SYSTEM_ERROR.getErrorCode(),
                ResponseCodeEnum.SYSTEM_ERROR.getErrorMessage());
    }
}
```

- [ ] **步骤 7：创建常量、枚举和 EventBus 基础设施**

**Constants.java：**
```java
package com.quanxiaoha.weblog.common.constant;

public class Constants {

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_VISITOR = "ROLE_VISITOR";
}
```

**EventEnum.java：**
```java
package com.quanxiaoha.weblog.common.enums;

public enum EventEnum {
    PV_INCREASE
}
```

**ArticleEvent.java：**
```java
package com.quanxiaoha.weblog.common.eventbus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ArticleEvent {

    private Long articleId;
    private String message;
}
```

**EventListener.java：**
```java
package com.quanxiaoha.weblog.common.eventbus;

public interface EventListener {
}
```

- [ ] **步骤 8：创建 EventBusConfig（Guava EventBus 注册）**

```java
package com.quanxiaoha.weblog.common.config;

import com.google.common.eventbus.EventBus;
import com.quanxiaoha.weblog.common.eventbus.EventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class EventBusConfig {

    @Bean
    public EventBus eventBus(List<EventListener> listeners) {
        EventBus eventBus = new EventBus("WeBlog-EventBus");
        for (EventListener listener : listeners) {
            eventBus.register(listener);
        }
        return eventBus;
    }
}
```

- [ ] **步骤 9：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS

---

### 任务 7：AOP 切面与工具类（骨架）

**涉及文件：**
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/aspect/ApiOperationLog.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/aspect/ApiOperationLogAspect.java`
- 新建：`weblog-module-common/src/main/java/com/quanxiaoha/weblog/common/utils/AgentRegionUtils.java`

- [ ] **步骤 1：创建 @ApiOperationLog 注解**

```java
package com.quanxiaoha.weblog.common.aspect;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface ApiOperationLog {

    String description() default "";
}
```

- [ ] **步骤 2：创建 ApiOperationLogAspect（骨架，后续阶段完善）**

```java
package com.quanxiaoha.weblog.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ApiOperationLogAspect {

    @Pointcut("@annotation(apiOperationLog)")
    public void apiOperationLog(ApiOperationLog apiOperationLog) {}

    @Before("apiOperationLog(apiOperationLog)")
    public void before(ApiOperationLog apiOperationLog) {
        log.info("API [{}] 请求", apiOperationLog.description());
    }

    @Around("apiOperationLog(apiOperationLog)")
    public Object around(ProceedingJoinPoint joinPoint, ApiOperationLog apiOperationLog) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long elapsed = System.currentTimeMillis() - start;
        log.info("API [{}] 响应, 耗时: {}ms", apiOperationLog.description(), elapsed);
        return result;
    }
}
```

- [ ] **步骤 3：创建 AgentRegionUtils（骨架，ip2region 在阶段五完善）**

```java
package com.quanxiaoha.weblog.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AgentRegionUtils {

    public static String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getIpRegion(String ip) {
        // 阶段五用 ip2region 实现
        return "未知";
    }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-common
```

预期：BUILD SUCCESS

---

### 任务 8：创建 weblog-module-jwt 模块（POM + JwtTokenHelper + ResultUtil）

**涉及文件：**
- 新建：`weblog-module-jwt/pom.xml`
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/JwtTokenHelper.java`
- 新建：`weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/utils/ResultUtil.java`

- [ ] **步骤 1：创建 jwt 模块目录和 POM**

```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-module-jwt/src/main/java/com/quanxiaoha/weblog/jwt/utils"
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.quanxiaoha.weblog</groupId>
        <artifactId>Web_Blog</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>weblog-module-jwt</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.quanxiaoha.weblog</groupId>
            <artifactId>weblog-module-common</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 2：创建 JwtTokenHelper（使用 jjwt 0.12.x 新 API）**

```java
package com.quanxiaoha.weblog.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenHelper {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(String username) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .subject(username)
                .issuer("weblog")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
```

- [ ] **步骤 3：创建 ResultUtil（向 HttpServletResponse 写 JSON）**

```java
package com.quanxiaoha.weblog.jwt.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ResultUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void writeJson(HttpServletResponse response, Object data) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(data));
        writer.flush();
        writer.close();
    }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-jwt
```

预期：BUILD SUCCESS

---

### 任务 9：创建 weblog-module-admin 模块（POM + 基础配置）

**涉及文件：**
- 新建：`weblog-module-admin/pom.xml`
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/PasswordEncoderConfig.java`
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/MinioConfig.java`
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/MinioProperties.java`
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/ThreadPoolConfig.java`

- [ ] **步骤 1：创建 admin 模块目录和 POM**

```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config"
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.quanxiaoha.weblog</groupId>
        <artifactId>Web_Blog</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>weblog-module-admin</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.quanxiaoha.weblog</groupId>
            <artifactId>weblog-module-jwt</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.minio</groupId>
            <artifactId>minio</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 2：创建 PasswordEncoderConfig（BCrypt）**

```java
package com.quanxiaoha.weblog.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

- [ ] **步骤 3：创建 MinioProperties（配置属性绑定）**

```java
package com.quanxiaoha.weblog.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;
    private String accessKey;
    private String secretKey;
    private String bucket;
}
```

- [ ] **步骤 4：创建 MinioConfig（MinioClient Bean）**

```java
package com.quanxiaoha.weblog.admin.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();
    }
}
```

- [ ] **步骤 5：创建 ThreadPoolConfig（异步线程池）**

```java
package com.quanxiaoha.weblog.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class ThreadPoolConfig {

    @Bean("weblogThreadPool")
    public Executor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("WeblogThreadPool-");
        executor.initialize();
        return executor;
    }
}
```

- [ ] **步骤 6：验证编译**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile -pl weblog-module-admin
```

预期：BUILD SUCCESS

---

### 任务 10：创建 weblog-web 模块（启动入口 + 配置文件）

**涉及文件：**
- 新建：`weblog-web/pom.xml`
- 新建：`weblog-web/src/main/java/com/quanxiaoha/weblog/web/WeblogWebApplication.java`
- 新建：`weblog-web/src/main/resources/application.yaml`
- 新建：`weblog-web/src/main/resources/application-dev.yaml`

- [ ] **步骤 1：创建 web 模块目录结构**

```bash
mkdir -p "E:/git_codes/Web_Blog/weblog-web/src/main/java/com/quanxiaoha/weblog/web"
mkdir -p "E:/git_codes/Web_Blog/weblog-web/src/main/resources"
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.quanxiaoha.weblog</groupId>
        <artifactId>Web_Blog</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>weblog-web</artifactId>
    <packaging>jar</packaging>

    <dependencies>
        <dependency>
            <groupId>com.quanxiaoha.weblog</groupId>
            <artifactId>weblog-module-admin</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>com.vladsch.flexmark</groupId>
            <artifactId>flexmark-all</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct-processor</artifactId>
            <scope>provided</scope>
        </dependency>
        <dependency>
            <groupId>com.github.gavlyukovskiy</groupId>
            <artifactId>p6spy-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **步骤 2：创建启动类（@ComponentScan 通配符扫描全部模块）**

```java
package com.quanxiaoha.weblog.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.quanxiaoha.weblog.*"})
public class WeblogWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeblogWebApplication.class, args);
    }
}
```

- [ ] **步骤 3：创建 application.yaml（主配置）**

```yaml
server:
  port: 8081

spring:
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: GMT+8

mybatis-plus:
  global-config:
    db-config:
      logic-delete-field: isDeleted
      logic-delete-value: 1
      logic-not-delete-value: 0
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl

jwt:
  secret-key: "WeBlogSecretKeyForJWTTokenGeneration2024!@#$%^&*()"
  expiration-time: 86400000

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: weblog
```

- [ ] **步骤 4：创建 application-dev.yaml（开发环境数据源）**

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/weblog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: 123456
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      minimum-idle: 5
      maximum-pool-size: 20
      auto-commit: true
      idle-timeout: 30000
      pool-name: WeblogHikariCP
      max-lifetime: 1800000
      connection-timeout: 30000

decorator:
  datasource:
    p6spy:
      enable-logging: true
      log-format: "%(sql)"
```

- [ ] **步骤 5：全量编译验证**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile
```

预期：全部模块 BUILD SUCCESS

---

### 任务 11：启动必需的占位类（Security 基础配置）

**涉及文件：**
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/config/WebSecurityConfig.java`
- 新建：`weblog-module-admin/src/main/java/com/quanxiaoha/weblog/admin/service/impl/UserDetailServiceImpl.java`

这两个类是 Spring Security 自动装配所需的 Bean，否则启动会报错。完整实现在阶段二完成。

- [ ] **步骤 1：创建占位 WebSecurityConfig（Spring Security 6.x Bean 风格）**

```java
package com.quanxiaoha.weblog.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }
}
```

- [ ] **步骤 2：创建占位 UserDetailServiceImpl**

```java
package com.quanxiaoha.weblog.admin.service.impl;

import com.quanxiaoha.weblog.common.domain.mapper.UserMapper;
import com.quanxiaoha.weblog.common.domain.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 占位实现 — 阶段二完善
        return new User(username, "", Collections.emptyList());
    }
}
```

- [ ] **步骤 3：全量编译验证**

```bash
cd "E:/git_codes/Web_Blog" && mvn compile
```

预期：BUILD SUCCESS

---

**阶段一完成。** 项目骨架搭建完毕：11 个 DO 类、11 个 Mapper、全部公共工具类、AOP 切面、异常体系、EventBus 基础设施、四模块 Maven 依赖链、Spring Boot 启动入口和配置文件。项目可编译通过。

> **备注：** 本项目需要通过 Maven 增加 Lombok 注解处理器支持。如果编译时找不到 Lombok 生成的 getter/setter，请在 IDE 中启用 annotation processing。
