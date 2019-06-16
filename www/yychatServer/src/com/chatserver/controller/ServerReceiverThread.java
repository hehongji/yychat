package com.chatserver.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.xml.crypto.dsig.spec.HMACParameterSpec;

import com.yychat.model.Message;

public class ServerReceiverThread extends Thread{//����Ҫ��run()����
	Socket s;
	HashMap hmSocket;
	Message mess;
	String sender;
	String receiver;	
	ObjectOutputStream oos;
	 
	public ServerReceiverThread(Socket s,HashMap hmSocket){
		this.s=s;
		this.hmSocket=hmSocket;		
	}
	
	public void run(){		
		ObjectInputStream ois;
		while(true){
			try {
				//����Message��Ϣ
				ois=new ObjectInputStream(s.getInputStream());
				mess=(Message)ois.readObject();//�����û���������������Ϣ��������10���û���100����
				System.out.println("�ȴ��û�����������Ϣ");
				System.out.println(mess.getSender()+"��"+mess.getReceiver()+"˵:"+mess.getContent());
				sender=mess.getSender();
				receiver=mess.getReceiver();
				
				//����µĺ��ѣ�����5��������Ӻ��ѵ���Ϣ
				if(mess.getMessageType().equals(Message.message_AddFriend)){
					String addFriendName=mess.getContent();
					System.out.println("��Ҫ��ӵĺ��ѵ�����Ϊ��"+addFriendName);
					//����6����User���в���addFriendName
					if(!YychatDbUtil.seekUser(addFriendName)){
						//�����ڣ�������Ӻ���
						mess.setMessageType(Message.message_AddFriendFailure_NoUser);
					}else{
						//����7���û�����,��Ѱrelation���У������û��ǲ��Ǻ���
						String friendRelation="1";
						if(YychatDbUtil.seekRelation(sender,addFriendName,friendRelation)){//��ѯfriendRelation��ϵ
							//�Ǻ��ѹ�ϵ
							mess.setMessageType(Message.message_AddFriendFailure_AlreadyFriend);
						}else{
							//����8�����Ǻ��ѹ�ϵ����ʱ������Ӻ���
							int count=YychatDbUtil.addRelation(sender,addFriendName,friendRelation);
							if(count!=0){
								mess.setMessageType(Message.message_AddFriendSuccess);
								String allFriendName=YychatDbUtil.getFriendString(sender);
								System.out.println("�������õ���ȫ���������֣�"+allFriendName);
								mess.setContent(allFriendName);
							}							
						}						
					}
					sendMessage(s, mess);				
				}
				
				//ת��Message��Ϣ
				if(mess.getMessageType().equals(Message.message_Common)){
					String content=mess.getContent();
					YychatDbUtil.addMessage(sender,receiver,content);
					mess.setSendTime(new Date());
					Socket s1=(Socket)hmSocket.get(mess.getReceiver());
					sendMessage(s1,mess);
					System.out.println("������ת������Ϣ"+mess.getSender()+"��"+mess.getReceiver()+"˵:"+mess.getContent());
				}
				
				//��2���裬�������ߺ�����Ϣ���ͻ���
				if(mess.getMessageType().equals(Message.message_RequestOnlineFriend)){
					//����Ҫ�õ����ߺ�����Ϣ
					Set friendSet=StartServer.hmSocket.keySet();//�õ��������ּ���
					Iterator it=friendSet.iterator();//��������Ŀ���Ƕ�friendSet�����е�Ԫ�ؽ��б���
					String friendName;
					String friendString=" ";
					while(it.hasNext()){//�жϻ���û����һ������
						friendName=(String)it.next();//��ȡ��һ����������
						if(!friendName.equals(mess.getSender()))//�ų��Լ�
							System.out.println("��ִ�иô��룡");
						     friendString=friendString+friendName+" ";//ΪʲôҪ�ӿո�
					}
					System.out.println("ȫ�����ѵ����֣�"+friendString);
					
					//��mess��ֵ
					mess.setContent(friendString);
					mess.setReceiver(sender);
					mess.setSender("Server");
					mess.setMessageType(Message.message_OnlineFriend);
					
					//����mess
					Socket s1=(Socket)hmSocket.get(sender);
					sendMessage(s1,mess);
					
				}
				/*if(mess.getMessageType().equals(Message.message_finishedEabelonlinefriend)){
					Set friendSet=StartServer.hmSocket.keySet();//�õ��������ּ���
					Iterator it=friendSet.iterator();//��������Ŀ���Ƕ�friendSet�����е�Ԫ�ؽ��б���
					String friendName;
					mess.setMessageType(Message.message_newfriendupdate);
					while(it.hasNext()){//�жϻ���û����һ������
						friendName=(String)it.next();//��ȡ��һ����������
						if(!friendName.equals(mess.getSender())){
							Socket s1=(Socket)hmSocket.get(friendName);//��ȡÿһ���û���Socket
							mess.setReceiver(friendName);
							ObjectOutputStream oos1;
							oos=new ObjectOutputStream(s1.getOutputStream());
							oos.writeObject(mess);
						}//�ų��Լ�
							System.out.println("��ִ�иô��룡");
					}
				}*/
				
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
			
		
		
		
	}

	public void sendMessage(Socket s,Message mess) throws IOException {
		oos=new ObjectOutputStream(s.getOutputStream());
		oos.writeObject(mess);//ת��Message
	}
	
}
