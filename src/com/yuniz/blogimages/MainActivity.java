package com.yuniz.blogimages;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.yuniz.blogimages.R;

import android.R.string;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	public int screenWidth = 0;
	public int screenHeight = 0;
	
	private EditText urlAddress;
	private TextView statusTxt;
	private RelativeLayout loader,loadBoard,mainMenu;
	
	private Button button2;
	private Button button3;
	private Button button4;
	
	private LinearLayout loadPosts;
	
	private String currentURL,pageCodes;
	
	Timer WFT = new Timer();
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		int sdk = android.os.Build.VERSION.SDK_INT;
		
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	    StrictMode.setThreadPolicy(policy);
		
	    Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		
		boolean smallScreen = false;
		try
		{ 
			display.getSize(size); 
			screenWidth = size.x; 
			screenHeight = size.y; 
			smallScreen = false;
		} 
		catch (NoSuchMethodError e) 
		{ 
			screenWidth = display.getWidth(); 
			screenHeight = display.getHeight(); 
			smallScreen = true;
		} 
	    
		urlAddress = (EditText) findViewById(R.id.editText1);
		statusTxt = (TextView) findViewById(R.id.textView2);
		
		loader = (RelativeLayout) findViewById(R.id.loader);
		loadBoard = (RelativeLayout) findViewById(R.id.loadBoard);
		mainMenu = (RelativeLayout) findViewById(R.id.mainMenu);
		
		button2 = (Button) findViewById(R.id.button2);
		button3 = (Button) findViewById(R.id.button3);
		button4 = (Button) findViewById(R.id.button4);
		
		loadPosts = (LinearLayout) findViewById(R.id.loadPosts);
		
		button3.setText("< Previous");
		button4.setText("Next >");
		
		double setNewHeight = screenHeight;
		double setNewWidth = screenWidth;
		
		setNewWidth = screenWidth * 0.3;
		button2.setWidth((int)setNewWidth);
		
		setNewWidth = screenWidth * 0.35;
		button3.setWidth((int)setNewWidth);
		button4.setWidth((int)setNewWidth);
		
		if(!isNetworkAvailable()){
			Toast.makeText(getApplicationContext(), "You need a smooth internet connection before you can use this app." , Toast.LENGTH_LONG).show();
		}
	}

	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
	
	public void loadURLBtn(View v) {
		String urlString = urlAddress.getText().toString();
		currentURL = urlString;
		
		if(urlString.length() == 0){
			Toast.makeText(getApplicationContext(), "Please fill in the Blog Web Address to continue." , Toast.LENGTH_LONG).show();
		}else{
			statusTxt.setText("Connecting to server...");
			mainMenu.setVisibility(View.INVISIBLE);
			loader.setVisibility(View.VISIBLE);
			setScanLoadWFT();
		}
	}
	
	public void newBlogBtn(View v) {
		loadBoard.setVisibility(View.INVISIBLE);
		mainMenu.setVisibility(View.VISIBLE);
	}
	
	public String getUrlContents(String url){
	    String content = "";
	    HttpClient hc = new DefaultHttpClient();
	    HttpGet hGet = new HttpGet(url);
	    ResponseHandler<String> rHand = new BasicResponseHandler();
	    try {
	        content = hc.execute(hGet,rHand);
	    } catch (ClientProtocolException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return content;
	}
	
	public void getPhotos(String rawSource){
		String[] stringSpliter,processStr,processStr2,processStr3;
		ArrayList<String> titles = new ArrayList<String>(),urls = new ArrayList<String>(),tempImages = new ArrayList<String>(),tempImagesOut = new ArrayList<String>();
		ArrayList<ArrayList<String>> imagesGroup = new ArrayList<ArrayList<String>>();
		
		stringSpliter = rawSource.split("\"entry-title\"");
		
		for(int a=1;a<stringSpliter.length;a++){
			// process with titles
			processStr = stringSpliter[a].split("\">");
			processStr = processStr[1].split("</a>");
			titles.add(processStr[0].toString());
			
			// process with urls
			processStr = stringSpliter[a].split("<a href=\"");
			processStr = processStr[1].split("\"");
			urls.add(processStr[0].toString());
			
			// process with images
			processStr3 = stringSpliter[a].split("<div class=\"entry-meta\">");
			processStr = processStr3[0].split("<img src=\"");
			tempImages = new ArrayList<String>();
			for(int b=1;b<processStr.length;b++){
				processStr2 = processStr[b].split("\"");
				tempImages.add(processStr2[0].toString());
			}
			imagesGroup.add(tempImages);
		}
		
		TextView postTitle=new TextView(this);
		TextView postLink=new TextView(this);
		ImageView postImage=new ImageView(this);
		loadPosts.removeAllViewsInLayout();
		Bitmap bitmap = null;
		
		for(int a=0;a<titles.size();a++){
			//Log.v("debug",titles.get(a) + "|" + urls.get(a) + "|" + imagesGroup.get(a).size());
			
			postTitle=new TextView(this);
			postTitle.setText(titles.get(a));
			postTitle.setTextSize(14);
			postTitle.setTextColor(Color.parseColor("#ffffff"));
			postTitle.setBackgroundColor(Color.parseColor("#333333"));
			postTitle.setPadding(5, 5, 5, 5);
			loadPosts.addView(postTitle);
			
			tempImagesOut = imagesGroup.get(a);
			for(int b=0;b<tempImagesOut.size();b++){
				//Log.v("--debug",tempImagesOut.get(b));
				
				try {
					bitmap = BitmapFactory.decodeStream((InputStream)new URL(tempImagesOut.get(b)).getContent());
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				postImage=new ImageView(this);
				postImage.setImageBitmap(bitmap);
				postImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
				postImage.setBackgroundColor(Color.parseColor("#ffffff"));
				postImage.setPadding(5, 5, 5, 5);
				loadPosts.addView(postImage);
			}
			
			postTitle=new TextView(this);
			postTitle.setTextSize(14);
			postTitle.setPadding(5, 5, 5, 5);
			postTitle.setTextColor(Color.parseColor("#ffffff"));
			postTitle.setBackgroundColor(Color.parseColor("#cccccc"));
			loadPosts.addView(postTitle);
		}
		
		loader.setVisibility(View.INVISIBLE);
		loadBoard.setVisibility(View.VISIBLE);
	}
	
	public void setScanLoadWFT() {
        WFT.schedule(new TimerTask() {          
            @Override
            public void run() {
                WFTTimerMethod();
            }
        }, 500); // 4 seconds delay
    }

    private void WFTTimerMethod() {
        this.runOnUiThread(Timer_Tick);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
        	pageCodes = getUrlContents( currentURL );
        	statusTxt.setText("Processing, please wait...");
        	setProcessLoadWFT();
        }
    };
    
    public void setProcessLoadWFT() {
        WFT.schedule(new TimerTask() {          
            @Override
            public void run() {
                WFTTimerMethodProcess();
            }
        }, 500); // 4 seconds delay
    }

    private void WFTTimerMethodProcess() {
        this.runOnUiThread(Timer_TickProcess);
    }

    private Runnable Timer_TickProcess = new Runnable() {
        public void run() {
        	getPhotos( pageCodes );
        }
    };
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
