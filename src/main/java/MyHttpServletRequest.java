import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;


public class MyHttpServletRequest implements javax.servlet.http.HttpServletRequest{
	private HttpSession session = null;
	private String characterEncoding = "ISO-8859-1";
	private BufferedReader reader;
	private List<String> requestContent;
	private Properties Attributes;
	private Properties Parameters;
	private ServletContext sc;
	private int localPort;
	private String localAddress;
	private String localName;
	private String RemoteAddress;
	private String RemoteName;
	private int RemotePort;
	private MyHttpServletResponse resp;
	private boolean read = false;
	
	
	public MyHttpServletRequest(Socket connection, ServletContext sc,List<String> requestContent,HttpSession session,MyHttpServletResponse resp) throws IOException{
		this.localAddress = connection.getLocalAddress().getHostAddress();
		this.localName = connection.getLocalAddress().getHostName();
		this.localPort = connection.getLocalPort();
		this.RemoteAddress = connection.getInetAddress().getHostAddress();
		this.RemoteName = connection.getInetAddress().getHostName();
		this.RemotePort = connection.getPort();
		this.requestContent = requestContent;
		InputStreamReader raw = new InputStreamReader(connection.getInputStream());
		BufferedReader reader = new BufferedReader(raw);
		this.reader = reader;
		Attributes = new Properties();
		Parameters = new Properties();
		this.sc = sc;
		this.session = session;
		this.resp = resp;
	}
	
	@Override
	public Object getAttribute(String name) {
		return Attributes.get(name);
	}

	@Override
	public Enumeration getAttributeNames() {
		return Attributes.keys();
	}

	@Override
	public String getCharacterEncoding() {
		return characterEncoding;
	}

	@Override
	public int getContentLength() {
		int length = 0;
		for(String line:requestContent){
			if (line.toLowerCase().contains("content-length")){
				length = Integer.parseInt(line.substring("content-length".length()+1).trim());
			}
		}
		return length;
	}

	@Override
	public String getContentType() {
		String type = "";
		for(String line:requestContent){
			if (line.toLowerCase().contains("content-type")){
				type = line.substring("content-type".length()+1).trim();
			}
		}
		return type;
	}
		
	@Override
	public String getLocalAddr() {
		return this.localAddress;
	}

	@Override
	public String getRemoteAddr() {
		return this.RemoteAddress;
	}
	
	@Override
	public int getLocalPort() {
		return this.localPort;
	}
	
	@Override
	public String getLocalName() {
		return localName;
	}

	@Override
	public Locale getLocale() { 
		String language = "";
		String country = "";
		for(String header:this.requestContent){
			if(header.toLowerCase().contains("accept-language:")){
				StringTokenizer st = new StringTokenizer(header.substring("accept-language:".length()).trim());
				while(st.hasMoreTokens()){
					String part = st.nextToken();
					if (part.contains("-")){
						int index = part.indexOf("-");
						language = part.substring(0,index);
						country = part.substring(index+1);
					}
				}
			}
		}
		return new Locale(language,country);
	}
	
	@Override
	public boolean isRequestedSessionIdValid() {
		return session.isValid();
	}
	
	@Override
	public String getRequestedSessionId() {
		String SESSIONID = null;
		Cookie[] cookies = getCookies();
		if (cookies != null){
			for(int i=0; i<cookies.length; i++){
				if (cookies[i].getName().equals("JSESSIONID")){
					SESSIONID = cookies[i].getValue();
				}
			}
		}
		return SESSIONID;
	}
	


	@Override
	public String getRemoteHost() {
		return RemoteName;
	}

	@Override
	public int getRemotePort() {
		return RemotePort;
	}
	
	@Override
	public String getRemoteUser() {
		String user = "";
		for(String header:requestContent){
			if (header.toLowerCase().contains("user-agent:")){
				user = header.substring("user-agent:".length()).trim();
			}
		}
		return user;
	}
		
	@Override
	public String getParameter(String name) {
		String value;
		if(getMethod().equals("GET"))
		{
			value = Parameters.getProperty(name);
			if(value != null)
				return value.split("#")[0];
			return null;
		}
		else if(getMethod().equals("POST"))
		{
			if(!read)
			{
				read = true;
				StringBuffer sb = new StringBuffer();
				int ch;
				int contentLength = Integer.parseInt(getHeader("content-length"));
				try {
					while(true)
					{
						    ch = getReader().read();
						    if(ch == -1)
						    	break;
							if(contentLength == 1){
								sb.append((char)ch);
								break;
							}
							sb.append((char)ch);
							contentLength--;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				String lines[];
				String parameterStr = sb.toString();
				if(parameterStr.contains("\n"))
				{
					lines = parameterStr.split("\n");
					for(String line:lines)
					{
						String[] paramValue = line.split("=");
						setParameter(paramValue[0], paramValue[1]);
					}
				}else
				{
					String[] parameters = parameterStr.split("&");
					
					for (String parameter: parameters)
					{
						String[] paramValue = parameter.split("=");
						setParameter(paramValue[0],paramValue[1]);
					}
				}
				
			}
		}
		value = Parameters.getProperty(name);
		if(value != null)
			return value.split("#")[0];
		return null;
	}

	public void setParameter(String name, String value){
		if(Parameters.getProperty(name)==null){
			Parameters.setProperty(name, value);
		}else{
			String s = Parameters.getProperty(name);
			s += "#" + value;
			Parameters.setProperty(name, s);
		}
	}
	
	@Override
	public Map getParameterMap() {
		getParameter("xxxx");
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		for(Entry<Object, Object>entry:Parameters.entrySet()){
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			String[] values = value.split("#");
			parameterMap.put(key, values);
		}
		return parameterMap;
	}

	@Override
	public Enumeration getParameterNames() {
		getParameter("xxxx");
		return Parameters.keys();
	}

	@Override
	public String[] getParameterValues(String name) {         
		getParameter("xxxx");
		String value = Parameters.getProperty(name);
		if(value!=null){
			String[] values = value.split("#");
			return values;
		}
		return null;
	}

	@Override
	public String getProtocol() {
		String line = requestContent.get(0);
		String Protocal = line.trim().substring(line.length()-8);
		return Protocal;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return reader;
	}

	@Override
	public String getScheme() {
		return "http";
	}

	@Override
	public String getServerName() {
		return this.localName;
	}

	@Override
	public int getServerPort() {
		return localPort;
	}

	@Override
	public boolean isSecure() {
		return false;
	}

	@Override
	public void removeAttribute(String name) {
		Attributes.remove(name);
	}

	@Override
	public void setAttribute(String name, Object attribute) {
		Attributes.put(name, attribute);
	}

	@Override
	public void setCharacterEncoding(String characterEncoding)
			throws UnsupportedEncodingException {
		this.characterEncoding = characterEncoding;
		
	}

	@Override
	public String getAuthType() {
		return BASIC_AUTH;
	}

	@Override
	public String getContextPath() {
		return "";
	}

	@Override
	public Cookie[] getCookies() {
		Cookie [] cookies = null;
		for(String line:requestContent){
			if (line.toLowerCase().contains("cookie:")){
				String[] pairs = line.substring(8).split(";");
				int num = pairs.length;
				cookies = new Cookie[num];
				for(int i=0; i<num; i++){
					String[] keyValue = pairs[i].split("=");
					Cookie cookie = new Cookie(keyValue[0].trim(),keyValue[1].trim());
					cookies[i] = cookie;
				}
				break;
			}
		}
		return cookies;
	}

	@Override
	public long getDateHeader(String header) {
		String Time;
		Date time = null;
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
		SimpleDateFormat sdf1 = new SimpleDateFormat("EEEEEEE, dd-MMM-yy hh:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
		sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
		for(String line:requestContent){
			if (line.toLowerCase().contains(header)){
				Time = line.substring(header.length()+1).trim();
				try{
					time = sdf.parse(Time);
				}catch(Exception e1){
					try{
						time = sdf1.parse(Time);
					}catch(Exception e2){
						try{
							time = sdf2.parse(Time);
						}catch(Exception e3){
							
						}
					}
				}
			}
		}
		return time.getTime();	
	}

	@Override
	public String getHeader(String name) {
		String value = "";
		for(String line:requestContent){
			if(line.toLowerCase().contains(name)){
				value = line.substring(name.length()+1).trim();
				break;
			}
		}
		return value;
	}

	@Override
	public Enumeration<String> getHeaderNames() {
		String header = "";
		Vector<String> atts = new Vector<String>();
		List<String> tempContent = requestContent.subList(1, requestContent.size());
		for(String line:tempContent){
			StringTokenizer st = new StringTokenizer(line.trim());
			header = st.nextToken();
			if (header.contains(":"))
				header = header.substring(0,header.length()-1);
			atts.add(header);
		}
		return atts.elements();
	}

	@Override
	public Enumeration<String> getHeaders(String name) {   
		Vector<String> atts = new Vector<String>();
		List<String> tempContent = requestContent.subList(1, requestContent.size());
		for(String line:tempContent){
			if (line.contains(name)){
				String valueStr = line.substring(name.length()+1).trim();
				String[] value = valueStr.split(",");
				for(String s:value){
					atts.add(s.trim());
				}
			}
		}
		return atts.elements();
	}

	@Override
	public int getIntHeader(String name) {
		int value = 0;
		for(String line:requestContent){
			if(line.toLowerCase().contains(name)){
				value = Integer.parseInt(line.substring(name.length()+1).trim());
			}
		}
		return value;
	}

	@Override
	public String getMethod() {
		String line = requestContent.get(0);
		StringTokenizer st = new StringTokenizer(line.trim());
		String method = st.nextToken();
		return method;
	}

	@Override
	public String getPathInfo() {              //special
		String pathInfo = "";
		StringBuffer URL = this.getRequestURL();
		HashMap<String, String> UrlPattern = HttpServer.UrlPattern;
		for(String name:UrlPattern.keySet()){
			String ServletUrl = UrlPattern.get(name);
			if(URL.toString().contains(ServletUrl)){
				if(ServletUrl.endsWith("*")){
					return null;
				}
				pathInfo = URL.substring(URL.indexOf(name)+name.length());
			}
		}
		if(!pathInfo.startsWith("/")&&pathInfo.length()!=0){
			pathInfo = "/" + pathInfo;
		}else{
			return null;
		}
		return pathInfo;
	}

	@Override
	public String getQueryString() {      //special
		String line = requestContent.get(0);
		String QueryString = "";
		if(line.contains("?")){
			int start = line.indexOf("?");
			QueryString = line.substring(start+1,line.length()-8).trim();
		}else{
			QueryString = null;
		}
		return QueryString;
	}

	@Override
	public String getRequestURI() {
		String line = requestContent.get(0);
		String URI = "";
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken();
		if(line.contains("http://")){
			String URL = st.nextToken();
			URI = URL.substring(("http://"+getHeader("host")).length());
		}else{
			URI = st.nextToken();
		}
		if(URI.contains("?")){
			int endPoint = line.indexOf("?");
			URI = URI.substring(0, endPoint);
		}
		return URI;
	}

	@Override
	public StringBuffer getRequestURL() {
		String line = requestContent.get(0);
		String rawURL = "";
		String URL = "";
		StringTokenizer st = new StringTokenizer(line);
		st.nextToken();
		if(line.contains("http://")){
			rawURL = st.nextToken();
		}else{
			String host = getHeader("host");
			rawURL = "http://"+ host + st.nextToken();
		}
		if(rawURL.contains("?")){
			int endPoint = line.indexOf("?");
			URL = rawURL.substring(0, endPoint);
		}else{
			URL = rawURL;
		}
		return new StringBuffer(URL);
	}

	@Override
	public String getServletPath() {
		String line = requestContent.get(0);
		String ServletUrl = "";
		for(String name:HttpServer.UrlPattern.keySet()){
			if (line.contains(HttpServer.UrlPattern.get(name))){
				ServletUrl = HttpServer.UrlPattern.get(name);
			}
			if(ServletUrl.endsWith("/*")){
				ServletUrl = "";
			}
		}
		return ServletUrl;
	}

	@Override
	public HttpSession getSession() {
		if (! hasSession()) {
			session = new HttpSession();
			HttpServer.sessionMap.put(session.getId(), session);
			Cookie cookie = new Cookie("JSEESIONID",session.getId());
			resp.addCookie(cookie);
		}else{
			session.isNew = false;
		}
		return session;
	}

	@Override
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				session = new HttpSession();
				HttpServer.sessionMap.put(session.getId(), session);
				Cookie cookie = new Cookie("JSEESIONID",session.getId());
				resp.addCookie(cookie);
			}else{
				session.isNew = false;
			}
		} else {
			if (! hasSession()) {
				session = null;
			}else{
				session.isNew = false;
			}
		}
		return session;
	}

	@Override
	public boolean isRequestedSessionIdFromCookie() {
		return true;
	}

	@Override
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	@Override
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}
	
	public boolean hasSession(){
		return ((session != null) && session.isValid());
	}
	
	@Override
	public boolean isUserInRole(String arg0) {  //exception 
		return false;
	}
	
	@Override
	public Principal getUserPrincipal() {   //exception
		return null;
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {   //exception
		return null;
	}
	
	@Override
	public Enumeration<?> getLocales() {   //exception
		return null;
	}
	
	@Override
	public RequestDispatcher getRequestDispatcher(String arg0) {   //exception
		return null;
	}
	
	@Override
	public String getPathTranslated() {   //exception
		return null;
	}
	
	@Override
	public String getRealPath(String arg0) {              //deprecate
		return null;
	}
}


