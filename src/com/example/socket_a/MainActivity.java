package com.example.socket_a;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	private static final String TAG = "Socket_A.MainActivity";
	private EditText ed1;
	private EditText ed2;
	private Button bt1;
	private TextView tv1,tv2;
	private boolean Socket_latch = true;
	private ServerSocket mServerSocket; //192.168.43.217
	private Socket mSocket = null ; //192.168.2.103
	private Socket mSocket_accept = null ;
	private BufferedReader mBufferedReader;
	private PrintWriter mPrintWriter;
	private Thread server_Thread = null;
	private Thread client_Thread = null;
	private InputStreamReader mInputStreamReader;
	private OutputStreamWriter mOutputStreamWriter;
	private Context mContext;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		object_initialize();
		getIP();
	}
	
	private void getIP(){
		try {   
            for (Enumeration<NetworkInterface> en = NetworkInterface   
                    .getNetworkInterfaces(); en.hasMoreElements();) {   
                NetworkInterface intf = en.nextElement();   
                for (Enumeration<InetAddress> enumIpAddr = intf   
                        .getInetAddresses(); enumIpAddr.hasMoreElements();) {   
                    InetAddress inetAddress = enumIpAddr.nextElement();   
                    if (!inetAddress.isLoopbackAddress()) {   
                        //return inetAddress.getHostAddress().toString();
                    	tv2.setText("your ip is "+inetAddress.getHostAddress().toString());
                    }   
                }   
            }   
        } catch (SocketException ex) {   
            Log.e("WifiPreference IpAddress", ex.toString());   
        }   
	}
	
	protected void onResume(){
		super.onResume();
		try {
			//mSocket = new Socket();
			mServerSocket = new ServerSocket(8080);
			socket_initialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	protected void onPause(){
		super.onPause();
		try {
			Socket_latch = false;
			mServerSocket.close();
			if(mSocket!=null)mSocket.close();
			if(mSocket_accept!=null)mSocket_accept.close();
			if(server_Thread!=null)server_Thread.interrupt();
			if(client_Thread!=null)client_Thread.interrupt();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void object_initialize(){
		tv1 = (android.widget.TextView)this.findViewById(R.id.tv1);
		tv2 = (TextView)this.findViewById(R.id.tv2);
		ed1 = (android.widget.EditText)this.findViewById(R.id.ed1);
		ed2 = (EditText)this.findViewById(R.id.ed2);
		bt1 = (Button)this.findViewById(R.id.bt1);
		bt1.setOnClickListener(CLICK);
		mContext = MainActivity.this;
	}
	
	private void socket_initialize(){
		server_Thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					while(Socket_latch){
					mSocket_accept = mServerSocket.accept();
					if(mSocket_accept!=null){
						Socket_latch = false;
						change2SERVER();
					}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}});
		server_Thread.start();
		
	}
	
	private void change2SERVER() throws IOException{
		boolean readerLatch = true;
		//
		//mServerSocket.close();
		//Socket_latch=false;
		//
		
		mBufferedReader = new BufferedReader(new InputStreamReader(mSocket_accept.getInputStream()));
		//while(readerLatch){
		String message = mBufferedReader.readLine();
		message = message +" sent from "+ mSocket_accept.getLocalAddress();
		if(!message.equals("")){
				//
			Message msg = new Message();
			msg.what=1;
			msg.obj=message;
			handler.sendMessage(msg);
		}
		//}
		mBufferedReader.close();
		//mPrintWriter.close(); //check_point
	}
	
	public Handler handler = new Handler(){
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:
				String message = (String)msg.obj;
				tv1.setText(tv1.getText().toString()+"\n\r"+message);
				break;
			case 999:
				tv1.setText(tv1.getText().toString()+"\n\r"+ed2.getText().toString()+" sent from me");
			}
			super.handleMessage(msg);
		}
	};
	
	private View.OnClickListener CLICK = new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int id = v.getId();
			switch(id){
			case R.id.bt1:
				if(!(ed1.getText().toString().equals("")&&ed2.getText().toString().equals(""))){
					server_Thread.interrupt();
					//mServerSocket.close();
					//Socket_latch=false;
					sendMessage();
					Socket_latch = true;
					socket_initialize();
				}
				break;
			}
		}
	};
	
	private void sendMessage(){
		final Message mMessage = new Message();
		client_Thread = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					mSocket = new Socket(InetAddress.getByName(ed1.getText().toString()),8080);
					if(mSocket!=null){
						//mPrintWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
						mPrintWriter = new PrintWriter(new OutputStreamWriter(mSocket.getOutputStream()));
						mPrintWriter.println(ed2.getText().toString());
						mMessage.what=999;
						handler.sendMessage(mMessage);
					}
					mPrintWriter.close();
					mSocket.close();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}});
		client_Thread.start();
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
