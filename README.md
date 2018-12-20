# sping-boot-scaffold
spring boot 项目基础功能搭建

### 1.数据库部分
#### 使用 mysql + mybatis
* 使用 Mybatis Generator 生成数据库层代码
    1. 每个生成的 Mapper 继承通用 Mapper(BaseMapper)。基础操作在通用 Mapper 中。
    2. 每个 model 不生成对应的 Example 类，定义通用 Example 类（BaseExample）。
    3. 实现分页，使用 offset+limit 实现。
    4. 配合参数查询，实现HttpServletRequest 查询参数转换成 通用Example（BaseExample）查询。
* 配置多数据源，主从，事务
### 2.缓存部分
* 多数据源
* 使用 redisTemplate操作缓存，中间切库查询
* 使用模板模式，封装基础操作
