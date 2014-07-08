package com.twkj.demo;

import java.util.Collection;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static XMPPConnection connection=null;
	private static final String TAG = "sun";
	private TextView tv;
	private int tv_id;
   
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Resources resource = this.getResources();
        String pkgName = this.getPackageName();	
		setContentView(resource.getIdentifier("activity_main", "layout", pkgName));
		tv_id=resource.getIdentifier("tv", "id", pkgName);
		tv=(TextView) findViewById(tv_id);
		conection();
		
	
	}
	Handler handler=new Handler(){
		public void handleMessage(android.os.Message msg) {
			String str= (String) msg.obj;
			tv.setText(str+"------------");
		};
	};
	/**
	 * 
	 * 连接服务器
	 */
	public void conection() {
		new Thread(new Runnable() {
			public void run() {
				// XMPPConnection.DEBUG_ENABLED = true;
			        final ConnectionConfiguration connectionConfig = 
			        		new ConnectionConfiguration(
			                "192.168.0.102", 5222);
			        MainActivity.connection = new XMPPConnection(connectionConfig);
			       // MainActivity.connection.DEBUG_ENABLED = true;		    
			        try {
						connection.connect();
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						connection.login("shenyiya", "123");  //登录
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean be=connection.isAuthenticated();
					//注册
				//	String result=regist("shenyiya", "123");
				//	boolean be1=deleteAccount(connection);
					setPresence(2);  //设置用户的状态
					logout(connection);
					Message msg=new Message();
					msg.obj=be+"---";
					handler.sendMessage(msg);
			}
		}).start();
	}

	/**
	 * 
	 * 注册
	 * @param account
	 *            注册帐号
	 * @param password
	 *            注册密码
	 * @return 1、注册成功 
	 * 		   0、服务器没有返回结果
	 * 		   2、这个账号已经存在  
	 * 		   3、注册失败
	 */
	public String regist(String account, String password) {
		if (connection == null){
			return "0";
		}	
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(connection.getServiceName());
		reg.setUsername(account);
		// 注意这里createAccount注册时，参数是username，不是jid，是“@”前面的部分。
		reg.setPassword(password);
		reg.addAttribute("android", "geolo_createUser_android");//
		// 这边addAttribute不能为空，否则出错。所以做个标志是android手机创建的吧!!!!!
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = connection.createPacketCollector(filter);
		connection.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		// Stop queuing results
		collector.cancel();// 停止请求results(是否成功的结果)
		if (result == null) {
			Log.e("RegistActivity", "No response from server.");
			return "0";
		} else if (result.getType() == IQ.Type.RESULT) {
			return "1";
		} else { // if (result.getType() == IQ.Type.ERROR)
			if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {
				Log.e("RegistActivity", "IQ.Type.ERROR: "
						+ result.getError().toString());
				return "2";
			} else {
				Log.e("RegistActivity", "IQ.Type.ERROR: "
						+ result.getError().toString());
				return "3";
			}
		}
	}
    /** 
     * 修改密码 
     * @param connection 
     * @return 
     */  
    public static boolean changePassword(XMPPConnection connection,String pwd)  
    {  
        try {  
            connection.getAccountManager().changePassword(pwd);  
            return true;  
        } catch (Exception e) {  
            return false;  
        }  
    } 
	/**
	 * 更改用户状态
	 */
	public void setPresence(int code) {
		if (connection == null)
			return;
		Presence presence;
		switch (code) {
			case 0:
				presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				Log.v("state", "设置在线");
				break;
			case 1:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.chat);
				connection.sendPacket(presence);
				Log.v("state", "设置Q我吧");
				System.out.println(presence.toXML());
				break;
			case 2:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.dnd);
				connection.sendPacket(presence);
				Log.v("state", "设置忙碌");
				System.out.println(presence.toXML());
				break;
			case 3:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.away);
				connection.sendPacket(presence);
				Log.v("state", "设置离开");
				System.out.println(presence.toXML());
				break;
			case 4:
				Roster roster = connection.getRoster();
				Collection<RosterEntry> entries = roster.getEntries();
				for (RosterEntry entry : entries) {
					presence = new Presence(Presence.Type.unavailable);
					presence.setPacketID(Packet.ID_NOT_AVAILABLE);
					presence.setFrom(connection.getUser());
					presence.setTo(entry.getUser());
					connection.sendPacket(presence);
					System.out.println(presence.toXML());
				}
				// 向同一用户的其他客户端发送隐身状态
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(connection.getUser());
				presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
				connection.sendPacket(presence);
				Log.v("state", "设置隐身");
				break;
			case 5:
				presence = new Presence(Presence.Type.unavailable);
				connection.sendPacket(presence);
				Log.v("state", "设置离线");
				break;
			default:
				break;
			}
		}
    /** 
     * 删除当前用户 
     * @param connection 
     * @return 
     */  
    public static boolean deleteAccount(XMPPConnection connection)  
    {  
        try {  
            connection.getAccountManager().deleteAccount();  
            return true;  
        } catch (Exception e) {  
            return false;  
        }  
    }
    /** 
     * 注销
     * @param connection 
     * @return 
     */  
    public static boolean logout(XMPPConnection connection)  
    {  
        try {  
            connection.disconnect();  
            return true;  
        } catch (Exception e) {  
            return false;  
        }  
    }

}
