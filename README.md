# CSGO-Market

CSGO-Market是一个用于收集、分析和展示CSGO物品市场数据的全栈应用。该项目提供了物品价格趋势分析、市场统计数据和物品区块分析等功能。

## 项目介绍

本项目通过爬取CSGO市场数据，对物品价格和交易情况进行分析，帮助用户更好地了解市场趋势，为投资和交易决策提供数据支持。

<details open>
<summary>主要功能</summary>

| 功能 | 描述 |
|------|------|
| 物品区块分析 | 分析物品价格区块变动趋势 |
| 市场统计 | 提供市场整体统计数据 |
| 数据可视化 | 使用图表直观展示数据变化 |
| 实时数据更新 | 定时任务自动更新最新市场数据 |

</details>

## 项目架构

```mermaid
graph TD
    A[用户] --> B[前端 Vue]
    B --> C[后端 Spring Boot]
    C --> D[(MongoDB)]
    C --> E[Steam API]
    
    subgraph 前端模块
    B --> F[数据展示]
    B --> G[用户交互]
    B --> H[数据可视化]
    end
    
    subgraph 后端模块
    C --> I[数据爬虫]
    C --> J[数据分析]
    C --> K[API服务]
    C --> L[定时任务]
    end
    
    style A fill:#f9f,stroke:#333,stroke-width:2px
    style B fill:#bbf,stroke:#333,stroke-width:2px
    style C fill:#bfb,stroke:#333,stroke-width:2px
    style D fill:#fbb,stroke:#333,stroke-width:2px
```

## 数据流程

```mermaid
sequenceDiagram
    participant 爬虫服务
    participant 数据处理服务
    participant 数据库
    participant API服务
    participant 前端应用
    
    爬虫服务->>数据处理服务: 原始市场数据
    数据处理服务->>数据处理服务: 数据清洗与分析
    数据处理服务->>数据库: 存储处理后数据
    前端应用->>API服务: 请求数据
    API服务->>数据库: 查询数据
    数据库->>API服务: 返回结果
    API服务->>前端应用: 响应数据
    前端应用->>前端应用: 渲染数据可视化图表
```

## 技术栈

### 后端
- Spring Boot
- MongoDB
- Spring Data
- RESTful API
- 定时任务调度

### 前端
- Vue 3
- TypeScript
- Element Plus
- ECharts
- Axios
- Pinia
- Vue Router

## 项目结构

```mermaid
classDiagram
    class Backend {
        +Controllers
        +Services
        +Repositories
        +Entities
        +Config
        +Crawler
    }
    
    class Frontend {
        +Views
        +Components
        +Router
        +Store
        +API
        +Utils
    }
    
    class Database {
        +Collections
        +Indexes
        +Authentication
    }
    
    Backend --> Database : 存储/查询
    Frontend --> Backend : API调用
```

## 安装指南

### 前提条件
- JDK 17+
- Node.js 18+
- MongoDB 5+
- Maven 3.8+

### 后端部署
```bash
# 克隆项目
git clone https://github.com/yourusername/steamdt.git
cd steamdt/Spring

# 编译打包
mvn clean package -DskipTests

# 运行
java -jar target/csgo-market-1.0.0.jar
```

### 前端部署
```bash
# 进入前端目录
cd ../vue

# 安装依赖
npm install
# 或
yarn install

# 开发模式运行
npm run dev
# 或
yarn dev

# 构建生产版本
npm run build
# 或
yarn build
```

## 功能流程示例

```mermaid
flowchart TD
    A[开始] --> C[进入主页]
    
    C --> F[浏览功能菜单]
    F --> G[物品区块分析]
    F --> H[市场统计]
    F --> I[数据可视化]
    
    G --> J[查看历史价格]
    G --> K[分析趋势]
    
    H --> L[查看市场概况]
    H --> M[物品分类统计]
    
    I --> N[图表展示]
    I --> O[数据导出]
    
    style A fill:#f9f,stroke:#333,stroke-width:2px
    style C fill:#bbf,stroke:#333,stroke-width:2px
    style G,H,I fill:#bfb,stroke:#333,stroke-width:2px
```

## 使用说明

1. 启动后端服务和前端应用
2. 访问 `http://localhost:5173`（开发模式）或部署后的URL
3. 在首页可查看最新的市场数据和统计信息
4. 使用分析工具探索物品价格趋势和市场变化

## 配置说明

### 后端配置
修改 `Spring/src/main/resources/application.yml` 文件:

```yaml
spring:
  data:
    mongodb:
      host: 你的MongoDB主机
      port: 27017
      database: csgo
      username: 你的用户名
      password: 你的密码
      authentication-database: admin

server:
  port: 8080
```

### 前端配置
如需修改API地址，编辑 `vue/src/api/config.ts` 文件。

## 开发路线图

```mermaid
gantt
    title 项目开发路线图
    dateFormat  YYYY-MM-DD
    section 后端开发
    数据库设计           :done, 2023-09-01, 2023-09-10
    API开发              :done, 2023-09-11, 2023-09-25
    爬虫服务实现         :done, 2023-09-26, 2023-10-10
    数据分析服务         :active, 2023-10-11, 2023-10-30
    
    section 前端开发
    UI设计               :done, 2023-09-05, 2023-09-15
    核心组件开发         :done, 2023-09-16, 2023-10-05
    数据可视化实现       :active, 2023-10-06, 2023-10-25
    响应式布局优化       :2023-10-26, 2023-11-10
    
    section 测试与发布
    单元测试             :2023-11-01, 2023-11-15
    集成测试             :2023-11-16, 2023-11-30
    文档编写             :2023-11-20, 2023-12-05
    首次发布             :milestone, 2023-12-10, 0d
```

## 贡献指南
欢迎贡献代码、报告问题或提出新功能建议！

1. Fork 本仓库
2. 创建你的特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交你的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建一个 Pull Request

## 许可证
[MIT License](LICENSE)

---

> CSGO市场数据分析，让交易更明智，投资更精准。

<!-- TODO: 后续添加更多使用场景和最佳实践示例 -->
