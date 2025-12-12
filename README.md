# 图书借阅管理系统

一个基于 Spring Boot + Vue 3 的现代化图书借阅管理系统，采用青蓝科技风暗色主题设计。

## 技术栈

### 后端
- **Spring Boot 2.7.x** - 核心框架
- **MyBatis** - ORM框架
- **MySQL** - 数据库
- **BCrypt** - 密码加密
- **Session** - 会话管理

### 前端
- **Vue 3** - 前端框架（Composition API）
- **Element Plus** - UI组件库
- **Vue Router** - 路由管理
- **Axios** - HTTP请求
- **ECharts** - 数据可视化图表
- **Vite** - 构建工具

## 功能特性

### 用户认证
- 用户登录/注销
- 用户注册（含密码强度提示）
- 修改密码
- 登录状态持久化

### 图书管理
- 图书列表分页展示
- 图书新增/编辑/删除
- 图书信息查询

### 用户管理
- 用户列表分页展示
- 用户新增/编辑/删除
- 管理员/普通用户角色区分

### 借阅管理
- 借阅记录分页展示
- 借书/还书操作
- 借阅历史查询

### 部门管理
- 部门列表分页展示
- 部门新增/编辑/删除

### 数据统计
- 首页仪表盘展示系统概览
- ECharts 柱状图/饼图可视化
- 实时统计数据

### 界面特性
- 青蓝科技风暗色主题
- 响应式布局设计
- 流畅的动画效果
- 空状态友好提示

## 项目结构

```
book/
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API接口定义
│   │   ├── router/         # 路由配置
│   │   ├── styles/         # 全局样式
│   │   └── views/          # 页面组件
│   │       ├── Dashboard.vue   # 首页仪表盘
│   │       ├── Login.vue       # 登录页
│   │       ├── Register.vue    # 注册页
│   │       ├── Profile.vue     # 个人中心
│   │       ├── BookList.vue    # 图书管理
│   │       ├── UserList.vue    # 用户管理
│   │       ├── BorrowList.vue  # 借阅管理
│   │       └── DepartList.vue  # 部门管理
│   └── package.json
├── src/main/
│   ├── java/edu/fosu/book/
│   │   ├── controller/     # 控制器层
│   │   ├── service/        # 服务层
│   │   ├── dao/            # 数据访问层
│   │   ├── entity/         # 实体类
│   │   ├── dto/            # 数据传输对象
│   │   ├── common/         # 公共类
│   │   └── config/         # 配置类
│   └── resources/
│       ├── mapper/         # MyBatis映射文件
│       └── application.properties
└── pom.xml                 # Maven配置
```

## 快速开始

### 环境要求
- JDK 8+
- Node.js 16+
- MySQL 5.7+
- Maven 3.6+

### 数据库配置
1. 创建数据库 `book`
2. 执行数据库初始化脚本
3. 修改 `application.properties` 中的数据库连接配置

### 后端启动
```bash
# 在项目根目录
mvn spring-boot:run
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 访问地址
- 前端：http://localhost:5173
- 后端API：http://localhost:8081/api

## API接口

### 认证接口
| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/auth/login | 用户登录 |
| POST | /api/auth/logout | 用户注销 |
| POST | /api/auth/register | 用户注册 |
| POST | /api/auth/change-password | 修改密码 |
| GET | /api/auth/current | 获取当前用户 |

### 图书接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/book/page | 分页查询图书 |
| GET | /api/book/{id} | 获取图书详情 |
| POST | /api/book | 新增图书 |
| PUT | /api/book/{id} | 更新图书 |
| DELETE | /api/book/{id} | 删除图书 |

### 用户接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/user/page | 分页查询用户 |
| GET | /api/user/{id} | 获取用户详情 |
| POST | /api/user | 新增用户 |
| PUT | /api/user/{id} | 更新用户 |
| DELETE | /api/user/{id} | 删除用户 |

### 借阅接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/borrow/page | 分页查询借阅记录 |
| POST | /api/borrow | 新增借阅 |
| PUT | /api/borrow/{userid}/{bookid} | 更新借阅 |
| DELETE | /api/borrow/{userid}/{bookid} | 删除借阅 |

### 部门接口
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/depart/page | 分页查询部门 |
| GET | /api/depart/{id} | 获取部门详情 |
| POST | /api/depart | 新增部门 |
| PUT | /api/depart/{id} | 更新部门 |
| DELETE | /api/depart/{id} | 删除部门 |

## 界面预览

### 登录页
青蓝科技风设计，支持记住密码，提供注册入口。

### 首页仪表盘
- 欢迎区域显示用户信息和时间问候
- 四个统计卡片展示系统数据概览
- 快捷操作入口
- ECharts 柱状图和饼图数据可视化
- 系统信息展示

### 管理页面
- 统一的表格布局和操作按钮
- 分页功能
- 搜索/筛选
- 新增/编辑弹窗
- 空状态友好提示

### 个人中心
- 用户信息展示
- 密码修改功能（含强度提示）

## 开发说明

### 代码规范
- 后端遵循阿里巴巴Java开发规范
- 前端使用 Vue 3 Composition API
- 统一使用 async/await 处理异步

### 安全措施
- 密码使用 BCrypt 加密存储
- 登录拦截器保护敏感接口
- 前端路由守卫
- Session 会话管理

## License

MIT License

## 作者

LiWenJian
