# tcc-dispatcher
tcc分布式事务框架  


注意点：  
1.当前只支持服务内的openfeign调用，并且因为全程自己一个人研发，平时的**时间紧张**，代码中有些可抽取的**未进行抽取**，都是以敏捷做出功能为目标写的
然后**未上生产**，还需继续研发！！  
2.未来该框架可以启用多个server，client可以选择任意一个server进行长连接，然后进行rpc调用；每个server使用分库技术来提高并发数，根据传来的tccId来做hash进行分库分散流量  
3.在tcc事务发起服务下，如果当前事务未执行完成或执行完成后，机器突然宕机tcc_controller下发起事务的状态不会改变，一直处于0的状态，当前框架内还未做出对应处理  
    备注：这里还需有个定时扫描数据，超过一定时间还是0状态的事务发起者，应该发送给对应的application_name服务下，让其判断是否成功提交事务，然后server来一一发送到对应的事务参与者是执行confirm还是cancel  


使用流程：  
1.把tcc-dispatcher-client打成maven依赖  
2.在需要使用tcc事务的服务下引入该依赖  
3.在需要使用tcc事务的impl实现类上（需要被spring管理）加上@TccClass注解  
4.然后在try方法上加上@TccMethod(confirmMethod="commit",cancelMethod="rollback")注解，同个服务下try和confirm，cancel方法名不能相同需唯一  
5.在需要feign调用的接口方法加上@TccFeign注解  
6.然后本地跑起tcc-dispatcher-server服务  
7.然后需要在yml上加上,ips是对应的步骤6的ip  
netty:  
  server:  
     port: 9001  
     ips: 127.0.0.1  
再跑起对应的依赖了client的服务  



client：  
1.spring启动后会去BeanPostProcessor接口After下拦截对应的Tcc相关的注解类和接口做代理，保存后续tcc业务的支撑，  
2.以request请求头某个标识来判断是tcc事务的发起者还有tcc事务的参与者  

server：  
调度者，控制tcc链路  

表介绍：  
tcc_controller:事务发起者  
tcc_participator:事务参与者  


