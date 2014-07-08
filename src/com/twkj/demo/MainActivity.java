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
	 * ���ӷ�����
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
						connection.login("shenyiya", "123");  //��¼
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					boolean be=connection.isAuthenticated();
					//ע��
				//	String result=regist("shenyiya", "123");
				//	boolean be1=deleteAccount(connection);
					setPresence(2);  //�����û���״̬
					logout(connection);
					Message msg=new Message();
					msg.obj=be+"---";
					handler.sendMessage(msg);
			}
		}).start();
	}

	/**
	 * 
	 * ע��
	 * @param account
	 *            ע���ʺ�
	 * @param password
	 *            ע������
	 * @return 1��ע��ɹ� 
	 * 		   0��������û�з��ؽ��
	 * 		   2������˺��Ѿ�����  
	 * 		   3��ע��ʧ��
	 */
	public String regist(String account, String password) {
		if (connection == null){
			return "0";
		}	
		Registration reg = new Registration();
		reg.setType(IQ.Type.SET);
		reg.setTo(connection.getServiceName());
		reg.setUsername(account);
		// ע������createAccountע��ʱ��������username������jid���ǡ�@��ǰ��Ĳ��֡�
		reg.setPassword(password);
		reg.addAttribute("android", "geolo_createUser_android");//
		// ���addAttribute����Ϊ�գ������������������־��android�ֻ������İ�!!!!!
		PacketFilter filter = new AndFilter(new PacketIDFilter(
				reg.getPacketID()), new PacketTypeFilter(IQ.class));
		PacketCollector collector = connection.createPacketCollector(filter);
		connection.sendPacket(reg);
		IQ result = (IQ) collector.nextResult(SmackConfiguration
				.getPacketReplyTimeout());
		// Stop queuing results
		collector.cancel();// ֹͣ����results(�Ƿ�ɹ��Ľ��)
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
     * �޸����� 
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
	 * �����û�״̬
	 */
	public void setPresence(int code) {
		if (connection == null)
			return;
		Presence presence;
		switch (code) {
			case 0:
				presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				Log.v("state", "��������");
				break;
			case 1:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.chat);
				connection.sendPacket(presence);
				Log.v("state", "����Q�Ұ�");
				System.out.println(presence.toXML());
				break;
			case 2:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.dnd);
				connection.sendPacket(presence);
				Log.v("state", "����æµ");
				System.out.println(presence.toXML());
				break;
			case 3:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Presence.Mode.away);
				connection.sendPacket(presence);
				Log.v("state", "�����뿪");
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
				// ��ͬһ�û��������ͻ��˷�������״̬
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(connection.getUser());
				presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
				connection.sendPacket(presence);
				Log.v("state", "��������");
				break;
			case 5:
				presence = new Presence(Presence.Type.unavailable);
				connection.sendPacket(presence);
				Log.v("state", "��������");
				break;
			default:
				break;
			}
		}
    /** 
     * ɾ����ǰ�û� 
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
     * ע��
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
