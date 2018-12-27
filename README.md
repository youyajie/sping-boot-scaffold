# sping-boot-scaffold
spring boot 项目基础功能搭建

### 1.数据库部分
#### 使用 mysql + mybatis
* 使用 Mybatis Generator 生成数据库层代码
    1. 每个生成的 Mapper 继承通用 Mapper(BaseMapper)。基础操作在通用 Mapper 中。
    2. 每个 model 不生成对应的 Example 类，定义通用 Example 类（BaseExample）。
    3. 实现分页，使用 offset+limit 实现。
    4. 配合参数查询，实现HttpServletRequest 查询参数转换成 通用Example（BaseExample）查询，依赖 Model 中每个属性添加@Colunm 注解，注解值为数据库对应字段名。
    5. 自定义了 Mybatis Generator 插件，位于包：com.yyj.springbootscaffold.mybatis.generator.plugins。引用的插件需要单独打包，并且在 pom 引入插件 Mybatis generator中添加依赖。
* 配置多数据源，主从，事务
    1. 多数据源和主从使用同一种思路解决。项目的基础数据库主从命名为DataSourceType.MASTER和DataSourceType.SLAVE,另加的数据源需要定义 annotation,并标注在具体的 Mapper类上来指定数据源。
    2. 数据源配置在DataSourceConfig类中。routeDataSource（）方法初始化支持的数据源，其中另加的数据源的放入map 中的 key 为注解的名称，为 AOP 动态切换数据源服务。
    3. 切面 DataSourceAsp 用来动态切换数据源，还支持 Mapper 接口中方法参数不为空的检查。动态切换的逻辑，Mapper 类中是否有自定义数据源的注解，有的话，取对应的数据源。无则判断在事务中或者以"insert, update..."开头的方法，则使用项目配置的主数据源，其他则使用从数据源。Mapper接口方法参数有判断不为空的场景，使用注解@NotNull（javax.validation.constraints.NotNull）实现。
    4. 事务沿用 spring boot 支持的@tranditional注解。在需要的方法上添加。
### 2.缓存部分
* 多数据源
* 使用 redisTemplate操作缓存，中间切库查询
* 使用模板模式，封装基础操作

**添加功能为使用项目中整理出来，原项目中经过测试，整理出来未重新测试。**
