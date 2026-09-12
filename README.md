# Sunny 农产品电商平台

## 项目简介

Sunny是一个连接城乡的农产品电商平台，主要服务于经常往返于城乡之间的司机群体，将山里的农产品带到城里销售，同时将城里的货物运送到农村。

## 功能特点

- 🌾 **农产品展示**: 干货、蔬菜、生态油、酒、养殖五大分类
- 🚚 **物流信息**: 城乡物流动态展示
- 🛒 **购物车**: 便捷的购物体验
- 📱 **多端支持**: 支持PC、手机、小程序、APP

## 技术栈

### 后端
- Spring Boot 2.7.18
- MyBatis Plus 3.5.3
- MySQL 8.0
- Redis (可选)

### 前端
- UniApp 3.0
- Vue 3.4
- Pinia 2.1

## 项目结构

```
sunny/
├── sunny-backend/      # 后端工程 (Spring Boot)
├── sunny-frontend/     # 前端用户端 (UniApp)
├── sunny-admin/        # 管理后台 (UniApp)
└── docs/               # 文档目录
    ├── design.md       # 设计文档
    ├── development.md  # 开发文档
    └── deployment.md   # 部署文档
```

## 快速开始

### 1. 环境准备
- JDK 1.8
- Node.js 18.20.0
- MySQL 8.0
- Maven 3.6+

### 2. 数据库初始化
```bash
mysql -u root -p < sunny-backend/sql/init.sql
```

### 3. 启动后端
```bash
cd sunny-backend
mvn spring-boot:run
```

### 4. 启动前端
```bash
cd sunny-frontend
npm install
npm run dev:h5
```

### 5. 启动管理后台
```bash
cd sunny-admin
npm install
npm run dev:h5
```

## 默认账号

- 管理后台: admin / admin123

## 访问地址

- 用户端: http://localhost:8081
- 管理后台: http://localhost:8082
- 后端API: http://localhost:8080

## 文档

- [设计文档](docs/design.md)
- [开发文档](docs/development.md)
- [部署文档](docs/deployment.md)

## 商品分类

| 分类 | 包含商品 |
|------|----------|
| 干货 | 白辣椒、豆角干、茄子干、笋干 |
| 蔬菜 | 辣椒、豆角、生姜等 |
| 生态油 | 菜籽油、茶油 |
| 酒 | 米酒、红薯酒 |
| 养殖 | 月子鸡、鸭、羊、牛 |

## 联系方式

- 微信号: sunny-farm
- 抖音号: sunny-farm

## 许可证

MIT License
