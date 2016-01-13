package com.pb.thread.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class SerialControll {
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
	private final String pathName = "C:\\Users\\acer\\Documents\\PortDatasLog";
	public final HashMap<Integer, List<Object[]>> portsDatas = new HashMap<Integer, List<Object[]>>();//缓存各个串口的数据
	private final static Map<Integer,CommPortIdentifier> comPorts = getAllAvailabelSerialPort();
	public static Map<Integer,SerialPortBean> usePorts = new HashMap<Integer, SerialPortBean>();
	public static Map<Integer,Boolean> hasMsgFlags = new HashMap<Integer, Boolean>();//port@boolean
	public static Map<Integer,String> msgCaches = new HashMap<Integer, String>();//多线程发送一条信息后缓存结果 port@msg
	public static Map<Integer,StringBuffer[]> msgsCaches = new HashMap<Integer, StringBuffer[]>();//多线程发送多条信息后缓存结果port@msgs

	// 取得计算机上所有可用的串口
	private static Map<Integer,CommPortIdentifier> getAllAvailabelSerialPort() {
		Map<Integer,CommPortIdentifier> comPorts = new HashMap<Integer,CommPortIdentifier>();
		Enumeration<?> ports = CommPortIdentifier.getPortIdentifiers();
		while (ports.hasMoreElements()) {
			CommPortIdentifier port = (CommPortIdentifier) ports.nextElement();
			switch (port.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				if (port.isCurrentlyOwned()) {
					System.err.println(port.getName() + " is in use!");
				} else {
					try {
						CommPort thePort = port.open("SerialPortBean", 50);
						thePort.close();
						int i = Integer.parseInt(port.getName().substring(3));
						comPorts.put(i,port);
						System.out.println(port.getName() + " can be use!");
					} catch (PortInUseException e) {
						System.err.println(port.getName() + " is in use!");
					}
				}
			}
		}
		return comPorts;
	}
	private boolean canUse(int port){
		boolean flag = false;
		if(comPorts.containsKey(port)){
			flag = true;
		}
		return flag;
	}
	public Map<Integer,SerialPortBean> setUsePorts(Object[][] ports){
		for (Object[] port : ports) {
			int portName = (int)port[0];
			if(canUse(portName)){
				SerialPortBean spb = new SerialPortBean(portName,(String)port[1]);
				usePorts.put(portName, spb);
				hasMsgFlags.put((Integer)portName, false);
				portsDatas.put(portName, new ArrayList<Object[]>());
				//create file
				createLogFile(portName);
			}
		}
		return usePorts;
	}
	//6，通过此串口对外通信一条信息（返回这个串口收到的结果）
	public StringBuffer sendMsgByPort(int port,String msg){
		SerialPortBean spb = usePorts.get(port);
		StringBuffer sb = spb.sendMsg(msg);
		return sb;
	}
	//7，通过此串口对外通信多条信息（返回这个串口收到的结果集合，这里不建议使用多个线程进行发送）
	public StringBuffer[] sendMsgsByPort(int port,String[] msgs){
		SerialPortBean spb = usePorts.get(port);
		StringBuffer[] sbs = spb.sendMsgs(msgs);
		return sbs;
	}
	//8，使用多个串口对外通信一条信息（多个串口，传递相同的一条信息）（返回一个map，里面对应各个串口的返回结果）
	public Map<Integer, StringBuffer> sendMsgByPorts(int[] ports,String msg){
		Map<Integer, StringBuffer> r = new HashMap<Integer, StringBuffer>();
		for (int port : ports) {
			StringBuffer r1 = creatThreadToSend(port, msg);
			r.put(port, r1);
		}
		msgCaches = new HashMap<Integer, String>();//清空缓存
		for (Integer flag : hasMsgFlags.keySet()) {//初始化信息标志，下一次仍可以使用
			hasMsgFlags.put(flag, false);
		}
		return r;
	}
	//creat thread for the port to send one msg
	public StringBuffer creatThreadToSend(int port,String msg){
		StringBuffer sb = new StringBuffer();
		Runnable s = new Runnable() {
			public void run() {
				StringBuffer r = sendMsgByPort(port, msg);
				hasMsgFlags.put(port, true);
				msgCaches.put(port, r.toString());
			}
		};
		Thread t = new Thread(s);
		t.start();
		while (!hasMsgFlags.get(port)) {
			try {
				Thread.sleep(10);//*****
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sb.append(msgCaches.get(port));
		return sb;
	}
	//8，使用多个串口对外通信一条信息（多个串口，传递不同的一条信息）（返回一个map，里面对应各个串口的返回结果）
	//每一条信息要和对应的串口数组的下标相同
	public Map<Integer, StringBuffer> sendDMsgByPorts(int[] ports,String[] msgs){
		Map<Integer, StringBuffer> r = new HashMap<Integer, StringBuffer>();
		for (int i = 0; i < ports.length; i++) {
			StringBuffer r1 = creatThreadToSend(ports[i], msgs[i]);
			r.put(ports[i], r1);
		}
		msgCaches = new HashMap<Integer, String>();//清空缓存
		for (Integer flag : hasMsgFlags.keySet()) {//初始化信息标志，下一次仍可以使用
			hasMsgFlags.put(flag, false);
		}
		return r;
	}
	//9，使用多个串口对外通信多条信息（多个串口，传递相同的多条信息）（返回一个map，里面对应各个串口的返回结果）
	public Map<Integer, StringBuffer[]> sendMsgsByPorts(int[] ports,String[] msgs){
		Map<Integer, StringBuffer[]> r = new HashMap<Integer, StringBuffer[]>();
		for (int port : ports) {
			StringBuffer[] rs = creatThreadToSends(port, msgs);
			r.put(port, rs);
		}
		msgsCaches = new HashMap<Integer,StringBuffer[]>();//清空缓存
		for (Integer flag : hasMsgFlags.keySet()) {//初始化信息标志，下一次仍可以使用
			hasMsgFlags.put(flag, false);
		}
		return r;
	}
	//creat thread for the port to send  msgs
	public StringBuffer[] creatThreadToSends(int port,String[] msgs){
		StringBuffer[] sbs = new StringBuffer[msgs.length];
		Runnable s = new Runnable() {
			public void run() {
				StringBuffer[] rs = sendMsgsByPort(port, msgs);
				hasMsgFlags.put(port, true);
				msgsCaches.put(port, rs);
			}
		};
		Thread t = new Thread(s);
		t.start();
		while (!hasMsgFlags.get(port)) {
			try {
				Thread.sleep(10);//*****
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sbs = msgsCaches.get(port);
		return sbs;
	}
	//9，使用多个串口对外通信多条信息（多个串口，传递不同的多条信息）（返回一个map，里面对应各个串口的返回结果）
	//二维数组的第i个数组是对应串口要发送的信息集合
	public Map<Integer, StringBuffer[]> sendDMsgsByPorts(int[] ports,String[][] msgss){
		Map<Integer, StringBuffer[]> r = new HashMap<Integer, StringBuffer[]>();
		for (int i = 0; i < ports.length; i++) {
			StringBuffer[] rs = creatThreadToSends(ports[i], msgss[i]);
			r.put(ports[i], rs);
		}
		msgsCaches = new HashMap<Integer,StringBuffer[]>();//清空缓存
		for (Integer flag : hasMsgFlags.keySet()) {//初始化信息标志，下一次仍可以使用
			hasMsgFlags.put(flag, false);
		}
		return r;
	}
	private static final SerialControll sc = new SerialControll();
	private SerialControll(){
	}
	public static SerialControll getInstance(){
		return sc;
	}
	//get time now 
	public String getNowTime(){
		String s = df.format(new Date());
		return s;
	}
	public void createLogFile(int portName){
		File file = new File(pathName);
		if(!file.exists() && !file.isDirectory()){
			file.mkdir();
		}
		File portFfile = new File(pathName+ "\\PORT" + portName +"_LOG.txt");
		if(!portFfile.exists()){
			try {
				portFfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public void addToLogFile(int portName){
		File portFfile = new File(pathName+ "\\PORT" + portName +"_LOG.txt");
		if(!portFfile.exists()){
			try {
				portFfile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		PrintWriter pw;
		try {
			pw = new PrintWriter(new BufferedWriter(
					                new FileWriter(portFfile)));
			List l = portsDatas.get(portName);
			for (Object object : l) {
				Object[] o = (Object[]) object;
				pw.print(o[0]);
				String s = (String) o[1];
				if(s.equals("S")){
					pw.print(" send: ");
				}else{
					pw.print(" recevied: ");
				}
				pw.print(o[2]);
				pw.print("\r\n");
				pw.print("\r\n");
			}
			pw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				 
	}
}
