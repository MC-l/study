一，什么是 SPI
    service provider Interface  服务提供者接口
    jdk 1.6 开始支持
    做法：
        在项目的类路径下提供一个META/services/xx文件
        jdk 使用 ServiceLoader.load() 方法会去解析 xx文件

二，例子
    在jdbc中，jdk提供了driver(数据库)接口，但是不同的厂商实现起来的方式不同，
    比如mysql、oracle、sqlLite等厂商底层的实现逻辑都是不同的,因此在对数据库驱动driver实现方式上,可以采用SPI机制。
    这样不同的数据库根据自身的不同逻辑按需扩展了Driver的能力，这就是SPI的最大好处
    mysql:
        比如在mysql-contactor.jar包中会在META/services路径下,这里相当于扩展了java.sql.Driver接口,jdk会在META/services路径下扫描该文件,
        然后加载mysql的diver实现类com.mysql.cj.jdbc.Driver，就相当于扩展了Driver的接口能力，按需加载mysql的实现类。

    dubbo:
        在 dubbo-2.7.3.jar!/META-INF/dubbo/internal/ 下有大量的SPI配置文件

三，动手实践
    创建 接口 Animal.sound()
    实现 Cat implements Animal
    实现 Dog implements Animal
    在 resources 下创建 META-INF/services 文件夹，创建 spi.demo.Animal 文件
    在 spi.demo.Animal 文件中配置 Cat 和 Dog 的全限定名
    编写 main:
        ServiceLoader<Animal> animals = ServiceLoader.load(Animal.class);
        Iterator<Animal> it = animals.iterator();
        while (it.hasNext()){
            Animal animal = it.next();
            animal.sound();
        }

    源码原理：
        ServiceLoader.load(Animal.class);
            初始化 LazyIterator（Animal.class, ClassLoader）  懒加载器，只有在真正使用（it.next()）的时候，才去加载
        it.hasNext()
        判断
            拼接 PREFIX （硬编码 "META-INF/services/"） 和 Animal的全限定名 为 fullName ,
            调用 ClassLoader.getSystemResources(fullName), 得到 目标 SPI 提供者 Cat 的全限定名
            返回true
        it.next()
            调用 Class.forName(cn, false, loader); 加载 Cat，这里对应的是懒加载原理（LazyIterator）
            调用 c.newInstance() 初始化 Cat，所以要求 SPI 提供者必须实现提供一个 无参构造方法,否则抛异常（ServiceConfigurationError： Provider spi.demo.Cat could not be instantiated）
            所有的SPI会保存在 providers中，这是个 LinkedHashMap，key是 SPI 提供者的全限定名（如 "spi.demo.Cat"），value是 具体的实例


四，java的SPI具有以下缺点：

    1，无法按需加载。虽然 ServiceLoader 做了延迟载入，使用了LazyIterator,但是基本只能通过遍历全部获取，接口的实现类得全部载入并实例化一遍。
        如果你并不想用某些实现类，或者某些类实例化很耗时，它也被载入并实例化了,假如我只需要其中一个,其它的并不需要这就形成了一定的资源消耗浪费

    2，不具有IOC的功能,假如我有一个实现类,如何将它注入到我的容器中呢，类之间依赖关系如何完成呢？

    3，serviceLoader不是线程安全的,会出现线程安全的问题

五，dubbo的SPI
    有点：
        配置文件中，以 key=val 形式存储，不同于 java的SPI，只保存 val
        按需加载，根据 key 获取 加载指定的 SPI 提供者，而不是java 的迭代器全量加载方式

    重点：
        @SPI("defaultKey")：   标记
        @Adaptive("key1", "key2")： Decide which target extension to be injected。 决定哪个目标 扩展实例 被注入到容器（IOC）中，
            查找过程：按序查找（先key1，再key2），如果配置的 类 没有找到，则采用 默认的key（由@SPI指定）
            如果不指定 key，则根据当前被标记的类的名字，按驼峰拆分成几个单词，然后用.拼接成一个 key，在去 META-INF/dubbo/com.xxx.yyy文件中查找,
            如： YyyInvokerWrapper --> yyy.invoker.wrapper
        入口类 ExtensionLoader 加载器 方法 getExtensionLoader() getExtension() createExtension() injectExtension()

    源码：
        AbstractClusterInvoker.initLoadBalance()：
            ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(DEFAULT_LOADBALANCE);

        解析：
            getExtension(DEFAULT_LOADBALANCE), DEFAULT_LOADBALANCE = "random"   // 传入 key = random，返回 实例
                getOrCreateHolder(name);    // 从缓存中拿出实例，如果不存在，则执行下面代码创建实例
                instance = createExtension(name);   // 根据 “random” 去配置文件中找到对应的 SPI提供者(random=org.apache.dubbo.rpc.cluster.loadbalance.RandomLoadBalance)
                    Class<?> clazz = getExtensionClasses().get(name);
                        Map<String, Class<?>> classes  = loadExtensionClasses();
                            cacheDefaultExtensionName();   // 根据传进来的 LoadBalance, 获取其注解 @SPI(RandomLoadBalance.NAME)，然后去 配置文件中加载 RandomLoadBalance.class，并缓存起来
                            loadDirectory(extensionClasses, DUBBO_INTERNAL_DIRECTORY, type.getName());
                                loadResource(extensionClasses, classLoader, resourceURL);
                                    loadClass(extensionClasses, resourceURL, Class.forName(line, true, classLoader), name);
                                        if (clazz.isAnnotationPresent(Adaptive.class)) {    // 如果添加了注解 @Adaptive("loadbalance")
                                                    cacheAdaptiveClass(clazz);
                                        }
                holder.set(instance);   // 放入缓存
