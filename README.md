


串口具有独占性，最好不要对一个串口打开多个线程来竞争使用，

否则可能会导致同一时间一个串口向外发送了两个线程中携带的信息。

因此当一个串口要发多个信息时，建议使用消息队列，不建议开启另一个线程对这个串口发另一信息。



1，项目开始启动，检查本机所有可用串口

2，占用项目要使用的所有串口，如果占用失败，项目退出启动

3，监控所有使用串口的离当前最近的10条新信息（创建全局唯一的串口观察者对象，有一个map对象，保存住所有使用串口的最新20条信息）

	(当map里数据满了后创建文件保存)
	
4，使用串口前，需要对此串口初始化

	串口初始化时操作步骤如下：
	
		a,开启一个线程，获得串口输入流，在线程中监听流向此串口的数据
		
		b,如果监听到的信息是有用的，改变信息标识，通知全局串口观察者对象将   串口@结果    保存
		
5，如果不用此串口了，需要清除此串口所占用资源（关闭串口的输入流，使此串口监听线程停止）

//6，通过此串口对外通信一条信息（返回这个串口收到的结果）

	（在主线程中向此串口输入流对象中输入信息）
	
	（此时监听线程会把结果也保存到观察者的信息列表中去）
	
	（阻塞主线程等待通信结果，死循环检查此串口信息标识，返回得到的信息；超过指定时间自动退出循环并返回超时）

//7，通过此串口对外通信多条信息（返回这个串口收到的结果集合，这里不建议使用多个线程进行发送）

	

//8，使用多个串口对外通信一条信息（多个串口，传递相同的一条信息）（返回一个map，里面对应各个串口的返回结果）
	
	（创建一个map）

	（开启多个线程，绑定发使用串口的输入流对象，向其输入信息）
	
	（此时每个串口的监听线程会把结果保存到观察者的信息列表中去）
	
	（阻塞开启的每一个线程，死循环检查此串口信息标识，返回得到的信息加入到map中；超过指定时间自动退出循环并把超时标志加入到map中）
	
	（阻塞主线程，死循环检查map的长度是否为使用的串口数量）
	
//8，使用多个串口对外通信一条信息（多个串口，传递不同的一条信息）（返回一个map，里面对应各个串口的返回结果）
	//每一条信息要和对应的串口数组的下标相同
	
//9，使用多个串口对外通信多条信息（多个串口，传递相同的多条信息）（返回一个map，里面对应各个串口的返回结果）

//9，使用多个串口对外通信多条信息（多个串口，传递不同的多条信息）（返回一个map，里面对应各个串口的返回结果）
//二维数组的第i个数组是对应串口要发送的信息集合

10，考虑线程池的可用性，如果可以使用线程池来管理所有的线程。
  据查一个串口服务器，最多可以开32个串口，一个串口一个监听线程的话也只有32个线程，对服务器而言只要配置可以就能承受，而且一个串口需要监听时


