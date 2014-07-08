package com.twkj.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPerferenceUtil {
	
	private Context context;
	private SharedPreferences sp;
	private Editor editor;
	public SharedPerferenceUtil(Context context) {
		super();
		this.context = context;
		init();
	}
	
	private void init() {
		// TODO Auto-generated method stub
	 sp=context.getSharedPreferences("twkj_xmpp", Context.MODE_PRIVATE);
	 editor=sp.edit();
	}

	public void addValues(String key,String value){
		editor.putString(key, value);	
		editor.commit();
	}
	public String getValues(String key){
		return sp.getString(key, "");	
	}
}
