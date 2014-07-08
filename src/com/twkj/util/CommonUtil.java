package com.twkj.util;

import android.content.Context;
import android.widget.Toast;

public class CommonUtil {
	
	private Context context;

	public CommonUtil(Context context) {
		super();
		this.context = context;
	}
	
	public void showMsg(String msg){	
		Toast.makeText(context, msg, 1).show();	
	}
}
