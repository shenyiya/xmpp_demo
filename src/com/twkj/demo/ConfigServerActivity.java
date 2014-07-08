package com.twkj.demo;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import com.twkj.util.CommonUtil;
import com.twkj.util.SharedPerferenceUtil;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigServerActivity extends Activity{
	
	private EditText server_address;
	private int id_server_address;
	private ConnectionConfiguration connectionConfiguration;
	private XMPPConnection connection;
	private CommonUtil commonUtil;
	private SharedPerferenceUtil sharedPerferenceUtil;
	private String ip_address;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Resources res=getResources();
		String packageName=getPackageName();
		setContentView(R.layout.config_server_address_layout_);
		id_server_address=res.getIdentifier("server_address", "id", packageName);
		server_address=(EditText) findViewById(id_server_address);
	}
	private void init(String ip) {
		// TODO Auto-generated method stub
		commonUtil=new CommonUtil(this);
		sharedPerferenceUtil=new SharedPerferenceUtil(this);
		connectionConfiguration=new ConnectionConfiguration(ip, 5222);
		connectionConfiguration.setSASLAuthenticationEnabled(false);
		connection=new XMPPConnection(connectionConfiguration);
	}
	public void onBtnClick(View view){
	
		ip_address=server_address.getText().toString();
		init(ip_address);
		connectToServer();		
	}
	Handler h=new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			int what=msg.what;
			System.out.println(what);
			if(what==1){
				commonUtil.showMsg("�Ѿ�����....");
			}else if(what==2){			
				commonUtil.showMsg("���ӳɹ�....");		
			}else if(what==3){	
				commonUtil.showMsg("����ʧ��....");		
		   }
			
		};
	};
	public void  connectToServer(){
		final Message msg=new Message();
		System.out.println(connection.isConnected());
		if(connection.isConnected()){ //�ж��Ƿ��Ѿ�����
			msg.what=1;
			h.sendMessage(msg);
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
                    //û������
					try {
						msg.what=2;
						connection.connect();
					} catch (XMPPException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						msg.what=3;
					}
					h.sendMessage(msg);
				}
			}).start();
		}

	}	
}
