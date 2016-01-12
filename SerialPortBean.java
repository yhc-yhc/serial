package com.pb.thread.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPortBean {
	int port;
	CommPort thisPort;
	CommPortIdentifier thePort;
	String portName;
	OutputStream os;
	InputStream ins;
	String backEndStr ="@";
	StringBuffer buf = new StringBuffer();
	boolean hasMsg = false;
	Thread l_thread;
	public SerialPortBean(int port,String backEndStr){
		this.port = port;
		this.backEndStr = backEndStr;
		String portName = "COM"+port;
		try {
			thePort = CommPortIdentifier.getPortIdentifier(portName);
		} catch (NoSuchPortException e) {
			System.err.println("port" + port+ " is not exsit!");
		}
		openPort();
		initPort();
	}
	//if port is recevies;
	public void receviesPort(){
		try {
			hasMsg = false;
			buf = new StringBuffer();
			String res = "sucess@";
			os.write(res.getBytes());
			System.out.println(port_threadName() + " response : " +res);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//get the port name and thread name
	public String port_threadName(){
		Thread t = Thread.currentThread();
		StringBuffer sb = new StringBuffer(t.getName());
		sb.append(" ");
		sb.append(portName);
		return  sb.toString();
	}
	//打开一个串口
	public void openPort(){
		try {
			thisPort = thePort.open("SerialPortBean", 50);
			portName = thePort.getName();
			ins = thisPort.getInputStream();
			os = thisPort.getOutputStream();
		} catch (PortInUseException e) {
			System.err.println(portName+ " is in use!");
		} catch (IOException e) {
			System.err.println("get portstream false!");
		}
	}
	//关闭一个串口
	public void closePort(){
		thisPort.close();
		try {
			ins.close();
			os.close();
		} catch (IOException e) {
			System.err.println("portstream closed!");
		}
	}
	//处理接收到的数据
	//b,如果监听到的信息是有用的，改变信息标识，通知全局串口观察者对象将   串口@结果    保存
	public void oprateBackData(int data){
		if(hasMsg){
			buf = new StringBuffer();
		}
		char b = (char) data;
		buf.append(b);
		StringBuffer sb =new StringBuffer();
		sb.append(b);
		if(sb.toString().equals(backEndStr) ){
			hasMsg = true;
			System.err.println("=====================================");
			System.err.println(port_threadName()+" recevied "+ buf);
			System.err.println("=====================================");
			if(port%2 != 0){
				receviesPort();
			}
		}
	}
	//初始化一个串口
	//a,开启一个线程，获得串口输入流，在线程中监听流向此串口的数据
	
	//b,如果监听到的信息是有用的，改变信息标识，通知全局串口观察者对象将   串口@结果    保存
	public void initPort(){
		Runnable runb = new Runnable() {
			public void run() {
				System.out.println(port_threadName() +" is inited ! ");
				try {
					while (true) {
						try {
							Thread.sleep(0);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int data = ins.read();
						if(data != -1){
							//System.out.println(data);
							oprateBackData(data);
						}
					}
				} catch (IOException e) {
					System.err.println("read inputsteam failed");
				}
			}
		};
		l_thread = new Thread(runb);
		l_thread.start();
	}
	//使用此串口向外发送一条信息
	public StringBuffer sendMsg(String s){
		if (s == null) {
			return null;
		}
		String msg = s + backEndStr;
		StringBuffer sb = new StringBuffer();
		try {
			os.write(msg.getBytes());
			System.out.println(port_threadName() +" send : "+s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i=1;
		while (true) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(port_threadName() + " checking has new msg : "+hasMsg);
			if (hasMsg) {
				hasMsg = false;
				sb.append(buf);
				buf = new StringBuffer();
				break;
			}
			i=i+1;
			System.out.println(i);
			//if time is to long 
			if(i==3){
				sb.append("timeout");
				break;
			}
			
		}
		System.out.println(port_threadName()+" receved : "+sb);
		return sb;
	}
	//使用此串口向外发送一条信息
	public StringBuffer[] sendMsgs(String[] params){
		StringBuffer[] sbs = new StringBuffer[params.length];
		for (int i = 0; i < params.length; i++) {
			sbs[i] = sendMsg(params[i]);
		}
		return sbs;
	}
}
