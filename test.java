package com.pb.thread.serial;

import java.util.Map;

public class test {

	public static void main(String[] args) {
		/*SerialPortBean spb2 = new SerialPortBean(2,"@");
		SerialPortBean spb3 = new SerialPortBean(3,"@");
		SerialPortBean spb4 = new SerialPortBean(4,"@");
		SerialPortBean spb5 = new SerialPortBean(5,"@");
		try {
			Thread.currentThread();
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		spb2.sendMsg("hello world@");
		spb2.sendMsg("ni hao@");
		spb4.sendMsg("再见@");
		String[] args1 = {"你好@","再见@"};
		spb2.sendMsgs(args1);*/
		SerialControll sc = SerialControll.getInstance();
		
		Object[][] appUserPorts = {{2,"@"},{3,"@"},{4,"@"},{5,"@"}};
		//Object[][] appUserPorts = {{3,"@"}};
		sc.setUsePorts(appUserPorts);
		int[] sendPorts = {2,4};
		String msg = "hello world!";
		String[] msgs = {"hello","javascript"};
		String[] msgss = {"hello","world","javascript"};
		String[][] msgss1 = {{"hello","world","javascript"},{"nihao","shijie","java"}};
		//6，通过此串口对外通信一条信息（返回这个串口收到的结果）
		//sc.sendMsgByPort(2, msg);
		//7，通过此串口对外通信多条信息（返回这个串口收到的结果集合，这里不建议使用多个线程进行发送）
		//sc.sendMsgsByPort(2, msgs);
		//8，使用多个串口对外通信一条信息（多个串口，传递相同的一条信息）（返回一个map，里面对应各个串口的返回结果）
		/*int[] ports1 = {2,4};
		Map<Integer, StringBuffer> mp = sc.sendMsgByPorts(ports1, msg);
		for (int port : mp.keySet()) {
			String s = mp.get(i).toString();
			System.out.println(port + " : send " + msg + "==========>" +s);
		}*/
		//8，使用多个串口对外通信一条信息（多个串口，传递不同的一条信息）（返回一个map，里面对应各个串口的返回结果）
		//每一条信息要和对应的串口数组的下标相同
		/*Map<Integer, StringBuffer> mp1 = sc.sendDMsgByPorts(sendPorts, msgs);
		for (int i = 0; i < sendPorts.length; i++) {
			int port = sendPorts[i];
			String s = mp1.get(port).toString();
			System.out.println(port + " : send " + msgs[i] + "==========>" +s);
		}*/
		//9，使用多个串口对外通信多条信息（多个串口，传递相同的多条信息）（返回一个map，里面对应各个串口的返回结果）
		/*Map<Integer, StringBuffer[]> rs = sc.sendMsgsByPorts(sendPorts, msgss);
		for (int port : rs.keySet()) {
			StringBuffer[] sbs = rs.get(port);
			for (int i = 0; i < sbs.length; i++) {
				System.out.println(port + " : send " + msgss[i] + "==========>" +sbs[i]);
			}
		}*/
		//9，使用多个串口对外通信多条信息（多个串口，传递不同的多条信息）（返回一个map，里面对应各个串口的返回结果）
		//二维数组的第i个数组是对应串口要发送的信息集合
		Map<Integer, StringBuffer[]> rs1 = sc.sendDMsgsByPorts(sendPorts, msgss1);
		for (int i = 0; i < sendPorts.length; i++) {
			int port = sendPorts[i];
			StringBuffer[] sbs = rs1.get(port);
			for (int j = 0; j < sbs.length; j++) {
				System.out.println(port + " : send " + msgss1[i][j] + "==========>" +sbs[j]);
			}
		}
	}
}
