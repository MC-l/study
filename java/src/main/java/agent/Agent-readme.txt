1，创建一个简单maven工程 test-agent
2，编写TestAgent类
    2.1，编写 public static void premain(String agentArgs, Instrumentation inst) 方法，名字一定是 premain
    2.2，在项目resource/META-INF/ 下编写 MANIFEST.MF，内容如下：
        Manifest-Version: 1.0
        Can-Redefine-Classes: true
        Can-Retransform-Classes: true
        Premain-Class: agent.TestAgent
3，打包成jar

4，运行目标工程：java -javaagent:testagent.jar -jar 目标jar 参数1 参数2


以上就是整个agent流程
一般情况下，都是要结合 cglib、javassist、ASM 这类字节码增强工具来使用。
如：对某些类进行增强，比如 java.util.Date 的convertToAbbr() 方法进行增强

参考博客: https://www.cnblogs.com/rickiyang/p/11368932.html