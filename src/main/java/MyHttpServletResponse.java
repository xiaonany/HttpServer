import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;

public class MyHttpServletResponse implements javax.servlet.http.HttpServletResponse{
	private String characterEncoding = "ISO-8859-1";
	private String contentType = "text/html";
	private boolean isCommited;
	private ArrayList<String> initialLine = new ArrayList<String>();
	private HashMap<String,ArrayList<String>> headers = new HashMap<String,ArrayList<String>>();
	ArrayList<Cookie> cookies = new ArrayList<Cookie>();
	private Locale locale = null;
	//private MyOutputStream buffer;
	private DataOutputStream out;
	private MyWriter writer;
	
	
	public MyHttpServletResponse(OutputStream outputStream) throws IOException{
		this.out = new DataOutputStream(outputStream);
		//buffer = new MyOutputStream(out);
		initialLine.add("HTTP/1.1");
		initialLine.add(200+"");
		initialLine.add("OK");
		ArrayList<String> type = new ArrayList<String>();
		type.add(contentType);
		headers.put("Content-Type:", type);
		this.writer = new MyWriter(out, initialLine,headers,cookies,this);
	}
	
	@Override
	public void flushBuffer() throws IOException {
		writer.flush();
	}
 
	@Override
	public int getBufferSize() {
		return writer.getSize();
	}

	@Override
	public String getCharacterEncoding() {
		return this.characterEncoding;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public Locale getLocale() {
		return this.locale;
	}

	@Override
	public PrintWriter getWriter() throws IOException {
		return writer;
	}

	@Override
	public boolean isCommitted() {
		if(this.isCommited){
			return true;
		}else{
			return false;
		}
	}

	public void commit(){
		isCommited = true;
	}
	@Override
	public void reset() {
		if(isCommitted()){
			throw new IllegalStateException();
		}else{
			initialLine.clear();
			headers.clear();
			writer.clearAll();
		}
	}

	@Override
	public void resetBuffer() {
		if(isCommitted()){
			throw new IllegalStateException();
		}else{
			writer.clearAll();
		}
	}
	
	@Override
	public String encodeRedirectURL(String URL) {
		return URL;
	}

	@Override
	public String encodeURL(String URL) {
		return URL;
	}
	
	@Override
	public void sendRedirect(String arg0) throws IOException {
		String redirectUrl = null;
		if(arg0.startsWith("http://")){
			redirectUrl = arg0;
		}else if(arg0.startsWith("/")){
			redirectUrl = "http://localhost:" + HttpServer.port + arg0;
		}else{
			redirectUrl = "http://localhost:" + HttpServer.port + "/" + arg0;
		}
		ArrayList<String> info = new ArrayList<String>();
		info.add(redirectUrl);
		headers.put("location", info);
		flushBuffer();
	}

	@Override
	public void setBufferSize(int size) {
	}

	@Override
	public void setCharacterEncoding(String characterEncoding) {
		this.characterEncoding = characterEncoding;
	}

	@Override
	public void setContentLength(int length) {
		ArrayList<String> len = new ArrayList<String>();
		len.add(""+length);
		headers.put("Content-Length",len);
	}

	@Override
	public void setContentType(String contentType) {
		this.contentType = contentType;
		ArrayList<String> type = new ArrayList<String>();
		type.add(""+type);
		headers.put("Content-Type",type);
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
		String country = locale.getCountry();
		String language = locale.getLanguage();
		ArrayList<String> info = new ArrayList<String>();
		info.add(""+language+"-"+country);
		headers.put("Accept-Language",info);
	}

	@Override
	public void addCookie(Cookie cookie) {
		cookies.add(cookie);
	}

	@Override
	public void addDateHeader(String name, long value) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String gmtDate = sdf.format(new Date(value));
		boolean exist = false;
		for(String elem:headers.keySet()){
			if (elem.contains(name)){
				headers.get(elem).add(gmtDate+" GMT");
				exist = true;
			}
		}
		if(!exist){
			setDateHeader(name,value);
		}
	}

	@Override
	public void addHeader(String name, String value) {
		boolean exist = false;
		for(String elem:headers.keySet()){
			if (elem.contains(name)){
				headers.get(elem).add(value);
				exist = true;
			}
		}
		if(!exist){
			setHeader(name,value);
		}
	}

	@Override
	public void addIntHeader(String name, int value) {
		boolean exist = false;
		for(String elem:headers.keySet()){
			if (elem.contains(name)){
				headers.get(elem).add(""+value);
				exist = true;
			}
		}
		if(!exist){
			setIntHeader(name,value);
		}
	}

	@Override
	public boolean containsHeader(String name) {
		boolean exist = false;
		for(String elem:headers.keySet()){
			if (elem.contains(name.toLowerCase())&&headers.get(elem)!=null){
				exist = true;
			}
		}
		return exist;
	}

	@Override
	public void sendError(int sc) throws IOException {
		if(this.isCommitted()){
			throw new IllegalStateException();
		}
		String info = "";
		if(sc==400){
			info = "Bad Request";
		}else if(sc==401){
			info = "Unauthorized";
		}else if(sc==402){
			info = "Payment Required";
		}else if(sc==403){
			info = "Forbidden";
		}else if(sc==404){
			info = "Not Found";
		}else if(sc==405){
			info = "Method Not Allowed";
		}else if(sc==406){
			info = "Not Acceptable";
		}else if(sc==407){
			info = "Proxy Authentication Required";
		}else if(sc==408){
			info = "Request Timeout";
		}else if(sc==409){
			info = "Conflict";
		}else if(sc==410){
			info = "Gone";
		}else if(sc==411){
			info = "Length Required";
		}else if(sc==412){
			info = "Precondition Failed";
		}else if(sc==413){
			info = "Request Entity Too Large";
		}else if(sc==414){
			info = "Request-url Too long";
		}else if(sc==415){
			info = "Unsupported Media Type";
		}else if(sc==417){
			info = "Expectation Failed";
		}else if(sc==500){
			info = "Internal Server Error";
		}else if(sc==501){
			info = "Not Implement";
		}else if(sc==502){
			info = "Bad Gateway";
		}else if(sc==503){
			info = "Service Unavailable";
		}else if(sc==504){
			info = "Gateway Timeout";
		}else if(sc==505){
			info = "HTTP Version Not Supported";
		}
		reset();
		String body = "<HTML><HEAD><TITLE>" +" "+info+" "+"</TITLE></HEAD><BODY><H1>HTTP Error"+" "+ sc+": "+ info+"</H1></BODY></HTML>";  
		ArrayList<String> len = new ArrayList<String>();
		len.add(Integer.toString(body.length()));
		headers.put("content-length", len);
		writer.clearAll();
		writer.write(body);
		flushBuffer();
		return;
	}

	@Override
	public void sendError(int sc, String message) throws IOException {
		if(this.isCommitted()){
			throw new IllegalStateException();
		}
		String info = "";
		if(sc==400){
			info = "Bad Request";
		}else if(sc==401){
			info = "Unauthorized";
		}else if(sc==402){
			info = "Payment Required";
		}else if(sc==403){
			info = "Forbidden";
		}else if(sc==404){
			info = "Not Found";
		}else if(sc==405){
			info = "Method Not Allowed";
		}else if(sc==406){
			info = "Not Acceptable";
		}else if(sc==407){
			info = "Proxy Authentication Required";
		}else if(sc==408){
			info = "Request Timeout";
		}else if(sc==409){
			info = "Conflict";
		}else if(sc==410){
			info = "Gone";
		}else if(sc==411){
			info = "Length Required";
		}else if(sc==412){
			info = "Precondition Failed";
		}else if(sc==413){
			info = "Request Entity Too Large";
		}else if(sc==414){
			info = "Request-url Too long";
		}else if(sc==415){
			info = "Unsupported Media Type";
		}else if(sc==417){
			info = "Expectation Failed";
		}else if(sc==500){
			info = "Internal Server Error";
		}else if(sc==501){
			info = "Not Implement";
		}else if(sc==502){
			info = "Bad Gateway";
		}else if(sc==503){
			info = "Service Unavailable";
		}else if(sc==504){
			info = "Gateway Timeout";
		}else if(sc==505){
			info = "HTTP Version Not Supported";
		}
		reset();
		String body = "<HTML><HEAD><TITLE>" +" "+info+" "+"</TITLE></HEAD><BODY><H1>HTTP Error"+" "+ sc+": "+ message +"</H1></BODY></HTML>";  
		ArrayList<String> len = new ArrayList<String>();
		len.add(Integer.toString(body.length()));
		headers.put("content-length", len);
		writer.clearAll();
		flushBuffer();
		return;
	}

	@Override
	public void setDateHeader(String name, long value) {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String gmtDate = sdf.format(new Date(value));
		ArrayList<String> info = new ArrayList<String>();
		info.add(gmtDate+" GMT");
		headers.put(name,info);
	}

	@Override
	public void setHeader(String name, String value) {
		ArrayList<String> info = new ArrayList<String>();
		info.add(value);
		headers.put(name,info);
	}

	@Override
	public void setIntHeader(String name, int value) {
		ArrayList<String> info = new ArrayList<String>();
		info.add(""+value);
		headers.put(name,info);
	}

	@Override
	public void setStatus(int sc) {
		String info = "";
		if(sc==202){
			info = "Accepted";
		}else if(sc==100){
			info = "Continue";
		}else if(sc==101){
			info = "Switching Protocals";
		}else if(sc==200){
			info = "OK";
		}else if(sc==201){
			info = "Accepted";
		}else if(sc==203){
			info = "Non-authoritative Information";
		}else if(sc==204){
			info = "No Content";
		}else if(sc==205){
			info = "Reset Content";
		}else if(sc==206){
			info = "Partial Content";
		}else if(sc==300){
			info = "Multiple Choices";
		}else if(sc==301){
			info = "Moved Permanently";
		}else if(sc==302){
			info = "Found";
		}else if(sc==303){
			info = "See Other";
		}else if(sc==304){
			info = "Not Modified";
		}else if(sc==305){
			info = "Uer Proxy";
		}else if(sc==306){
			info = "Unused";
		}else if(sc==307){
			info = "Temporary Redirect";
		}
		initialLine.clear();
		initialLine.add("Http/1.1");
		initialLine.add(sc+"");
		initialLine.add(info);
	}

	@Override
	public void setStatus(int arg0, String arg1) {  //deprecate
	}
	
	@Override
	public String encodeUrl(String arg0) {    //deprecate
		return null;
	}
	
	@Override
	public String encodeRedirectUrl(String arg0) {   //deprecate
		return null;
	}
	
	@Override
	public ServletOutputStream getOutputStream() throws IOException {  //exception
		return null;
	}
	
}

