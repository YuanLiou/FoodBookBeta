package com.example.mysql_tester.library;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import android.util.Log;

public class JSONParser {
    static InputStream is  = null;
    static JSONObject jObj = null;
    static String json = "";
    static String hostUrl = "http://proposal.yuer.tw/CodeIgniter/index.php/store/api?api=android";
    //constructor
    public JSONParser(){
    }
    //function get json from URL
    //by making HTTP POST or GET method
    public JSONObject makeHttpRequest(String method,List<NameValuePair> params){
		//Making Http Request
    	try{
    	    //check request method
    		if (method.equals("POST")){
    			//defaultHttpClient
    			DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			HttpPost httppost = new HttpPost(hostUrl);
    			UrlEncodedFormEntity mesag = new UrlEncodedFormEntity(params,"UTF-8");
    			mesag.setContentEncoding(HTTP.UTF_8);    //強制編碼成UTF-8
    			httppost.setEntity(mesag);
    			HttpResponse httpResponse = HttpClient.execute(httppost);
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    		else if (method.equals("GET")){
    			//defaultHttpClient
    			DefaultHttpClient HttpClient = new DefaultHttpClient();
    			//hostUrl += "&action=" +action;    //網址放後面版本
    			String paramString = URLEncodedUtils.format(params, "utf-8");
    			hostUrl += "&" + paramString;    //&本來是?
    			HttpGet httpGet = new HttpGet(hostUrl);
    			HttpResponse httpResponse = HttpClient.execute(httpGet);
    			HttpEntity httpEntity = httpResponse.getEntity();
    			is = httpEntity.getContent();
    		}
    	}
    	catch (UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
    	catch (ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch (IOException e){
    		e.printStackTrace();
    	}
    	
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null){
    			sb.append(line + "\n");
    		}
    		is.close();
    		json = sb.toString();
    	}
    	catch (Exception e){
    		Log.e("Buffer Error","Error Converting Result"+e.toString());
    	}
    	
    	//try parse a string to a JSON object
    	try{
    		jObj = new JSONObject(json);
    	}
    	catch(JSONException e) {
    		Log.e("JSON Parser","Error parsing data" +e.toString());
    	}
    	//return JSON String
    	return jObj;
    }
    
    public JSONObject MemberRequest(String url,String method,List<NameValuePair> params){
 		//Making Http Request
     	try{
     	    //check request method
     		if (method.equals("POST")){
     			//defaultHttpClient
     			DefaultHttpClient HttpClient = new DefaultHttpClient();
     			HttpPost httppost = new HttpPost(url);
     			UrlEncodedFormEntity mesag = new UrlEncodedFormEntity(params,"UTF-8");
     			mesag.setContentEncoding(HTTP.UTF_8);    //強制編碼成UTF-8
     			httppost.setEntity(mesag);
     			HttpResponse httpResponse = HttpClient.execute(httppost);
     			HttpEntity httpEntity = httpResponse.getEntity();
     			is = httpEntity.getContent();
     		}
        }
       	catch (UnsupportedEncodingException e){
    		e.printStackTrace();
    	}
    	catch (ClientProtocolException e){
    		e.printStackTrace();
    	}
    	catch (IOException e){
    		e.printStackTrace();
    	}
    	
    	try{
    		BufferedReader reader = new BufferedReader(new InputStreamReader(is,"utf-8"),8);
    		StringBuilder sb = new StringBuilder();
    		String line = null;
    		while ((line = reader.readLine()) != null){
    			sb.append(line + "\n");
    		}
    		is.close();
    		json = sb.toString();
    	}
    	catch (Exception e){
    		Log.e("Buffer Error","Error Converting Result"+e.toString());
    	}
    	
    	//try parse a string to a JSON object
    	try{
    		jObj = new JSONObject(json);
    	}
    	catch(JSONException e) {
    		Log.e("JSON Parser","Error parsing data" +e.toString());
    	}
    	//return JSON String
    	return jObj;
    }
}
