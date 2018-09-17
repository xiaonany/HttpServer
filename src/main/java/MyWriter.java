import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

public class MyWriter extends PrintWriter{
	private int size = 8192;
	private StringBuffer sb;
	boolean Written = false;
	ArrayList<String> initialLine;
	HashMap<String,ArrayList<String>> headers;
	ArrayList<Cookie> cookies;
	private DataOutputStream out;
	MyHttpServletResponse resp;
	public MyWriter(DataOutputStream out, ArrayList<String> initialLine,HashMap<String,ArrayList<String>> headers,ArrayList<Cookie> cookies,MyHttpServletResponse resp){
		super(out);
		this.out = out;
		this.sb = new StringBuffer(size);
		this.cookies = cookies;
		this.headers = headers;
		this.initialLine = initialLine;
		this.resp =resp;
	}
	
	public void setSize(int size){
		if(Written){
			throw new IllegalStateException();
		}
		this.sb = new StringBuffer(size);
	}
	
	public int getSize(){
		return this.size;
	}
	@Override
	public void write(String value){
		Written = true;
		if(sb.length()+value.length()>=sb.capacity()){
			flush();
		}
		sb.append(value);
	}
	
	@Override
	public void write(char[] data){
		Written = true;
		write(String.valueOf(data));
	}
	
	@Override
	public void write(char[] data, int off, int len){
		Written = true;
		write(data.toString().substring(off,off+len));
	}
	
	@Override
	public void write(String str, int off, int len){
		write(str.toString().substring(off,off+len));
	}
	
	@Override
	public void write(int c){
		write(Integer.toString(c));
	}

	public void clearAll(){
		this.sb = new StringBuffer(size);
	}
	
	@Override
	public void flush(){
		try{
			if(!resp.isCommitted()){
				String initLine = "";
				for (String info:initialLine){
					initLine += info + "";
				}
				out.writeBytes(initLine.trim()+"\r\n");
				
				for (String header:headers.keySet()){
					out.writeBytes(header+": ");
					if(headers.get(header).size()!=0){
						if(headers.get(header).size()!=1){
							for (int i=0; i<headers.get(header).size();i++){
								while(i!=headers.get(header).size()-1){
									out.writeBytes(headers.get(header).get(i)+",");
								}
								out.writeBytes(headers.get(header).get(i)+"\r\n");
							}
						}else{
							out.writeBytes(headers.get(header).get(0)+"\r\n");
						}
					}
				}
				
				if (cookies != null){
					for (Cookie cookie:cookies){
						String cstr = cookie.getName()+"="+cookie.getValue();
						SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
						sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
						if(cookie.getMaxAge() != -1){
							Date now = new Date();
							cstr += "; Expires=" + sdf.format(new Date(now.getTime()+cookie.getMaxAge()*1000))+" GMT";
						}
						if(cookie.getDomain() != null){
							cstr += "; Domain="+cookie.getComment();
						}
						if(cookie.getPath() != null){
							cstr += "; Path=" + cookie.getPath();
						}
						out.writeBytes("Set-Cookie: "+ cstr+"\r\n");
					}
				}
				out.writeBytes("\r\n");
				out.flush();
				resp.commit();
			}
			out.writeBytes(sb.toString());
			clearAll();
		}catch (IOException e){
			e.printStackTrace();
		}
	}
}

