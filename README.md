# rabbitMQ
一个rabbitMQ的入门demo，包括：  
- hello world
- work queue模式
  - 平均分配模式
  - 能者多劳模式
- 订阅模式
  - 出版订阅模式（Publish/Subscrible）
  - 路由模式(Routing)
  - 通配符模式(Topic)
  - RPC模式（待完成）

## 使用前必读
初次使用时，请先运行Utils下面的Registration类，以注册实验所需的交换机和队列  
如果需要配置rabbitMQ，请在ConnectionUtil中配置
