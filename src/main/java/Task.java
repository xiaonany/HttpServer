package edu.upenn.cis455.hw1;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.net.*;
import javax.servlet.http.HttpServlet;

public class Task implements Runnable{
	private File rootDirectory;
	private int port;
	private Socket connection;
	private HashMap<String,HttpServlet> sc; 
	private HashMap<String,String> servletUrl;
	private List<String> requestContent = new LinkedList<String>();
	
	//constructor 
	public Task(Socket connection,File rootDirectory,int port, HashMap<String,HttpServlet> sc, HashMap<String,String> servletUrl){   
		this.connection = connection;
		if (rootDirectory.isFile()){
			throw new IllegalArgumentException();
		}
		this.rootDirectory = rootDirectory;
		this.port = port;
		this.sc = sc;
		this.servletUrl = servletUrl;
	}
	
	public void run(){
		String root = rootDirectory.getPath();
		//process the request and send response through the connection
		try{
			//read the request content
			String request="";
			InputStreamReader reader = new InputStreamReader(connection.getInputStream());
			BufferedReader in = new BufferedReader(reader);
			Object Fline = in.readLine();
			if(Fline==null){
				return;
			}else{
				String line = (String)Fline;
				while(!line.equals("")){
					requestContent.add(line);
					request += line + " ";
					line = in.readLine();
				}
				StringTokenizer st = new StringTokenizer(request);
				String method = st.nextToken();
				String URL = st.nextToken();
				String version = st.nextToken();
				String contentType = "";
				
				String ServletName = "";
				boolean callServlet = false;
				for (String name:servletUrl.keySet()){
					String UrlPattern = servletUrl.get(name);
					if(URL.contains(UrlPattern)){
						ServletName = name;
						callServlet = true;
					}
				}
				if(callServlet){
					OutputStream output = connection.getOutputStream();
					HttpServlet servlet = sc.get(ServletName);		
					HttpSession session = null;
					String id = null;
					for(String s:requestContent){
						if (s.toLowerCase().contains("cookie:")){
							for (int i = 0; i<s.substring(8).split(";").length; i++) {
								if (s.substring(8).split(";")[i].contains("JSEESIONID"))
									id = s.substring(8).split(";")[i].split("=")[1].trim();
							}
						}
					}
					
					if(id!=null){
						session = HttpServer.sessionMap.get(id);
					}
					MyHttpServletResponse resp = new MyHttpServletResponse(output);
					MyHttpServletRequest req = new MyHttpServletRequest(connection,HttpServer.sc,requestContent,session,resp);			
					servlet.service(req, resp);

				}else{
					//set the date format
					SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss");
					SimpleDateFormat sdf1 = new SimpleDateFormat("EEEEEEE, dd-MMM-yy hh:mm:ss");
					SimpleDateFormat sdf2 = new SimpleDateFormat("EEE MMM dd hh:mm:ss yyyy");
					sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
					sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
					sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
					
					OutputStream raw = new BufferedOutputStream(connection.getOutputStream());
					Writer out = new OutputStreamWriter(raw);
					
					//process request and make response
					if(method.equals("GET")){
						if(version.equals( "HTTP/1.0")){
							//request:shutdown
							if(URL.equals("/shutdown")){
								out.write("HTTP/1.1 200 OK\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");
								out.write("Server: localhost:" + port+"\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>ShutDown</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>Pleas Refresh the page to shutdown the server</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
								HttpServer.setIfShutDown();
							//request:control
							}else if(URL.equals("/control")){
								out.write("HTTP/1.1 200 OK\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Control Panel</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>Control Panel</H1>\r\n");
								out.write("<H2>Name: XIAONAN YANG  SEAS login: xiaonany</H2>\r\n");
								//get the name and states of all workerThreads
								Set<Thread> allThread = Thread.getAllStackTraces().keySet();
								for(Thread t:allThread){
									if(t.getThreadGroup().getName().equals("Alpha")){
										out.write("<H4>Thread: "+t.getName()+"      Status: "+t.getState()+"\r\n</H4>");
									}
								}
								//shutdown click
								out.write("<H2><A HERF=\"/shutdown\"><button>ShutDown!</A></H2>\r\n");
								out.write("</BODY></HTML\r\n");
								out.flush();
							//absolute path
							}else if (URL.startsWith("http://localhost:"+port+"/")){						
								out.write("HTTP/1.1 403 Forbidden\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>HTTP Error 403: Absolute Path Forbidden</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
							//outsider path
							}else if (URL.startsWith("/..")){
								out.write("HTTP/1.1 403 Forbidden\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>HTTP Error 403: Outer Path Forbidden</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
							//normal path
							}else{
								File file = new File(rootDirectory,URL.substring(1,URL.length()));
								if (file.canRead()&&file.getCanonicalPath().startsWith(root)){
									//if URL is file
									if (file.isFile()){
										contentType = getContentType(URL);
										DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
										byte[] data = new byte[(int)file.length()];
										fis.readFully(data);
										fis.close();
										out.write("HTTP/1.1 200 OK\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										String lastModifiedTime = sdf.format(file.lastModified());
										out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
										out.write("Content-length: "+data.length+"\r\n");
										out.write("Content-type: "+contentType+"\r\n\r\n");
										out.flush();
										raw.write(data);
										raw.flush();
									//if URL is directory
									}else{
										out.write("HTTP/1.1 200 OK\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										String lastModifiedTime = sdf.format(file.lastModified());
										out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
										out.write("Content-type: text/html\r\n\r\n");
										File[] files = file.listFiles();
										out.write("<HTML>\r\n");
										out.write("<HEAD><TITLE>Files and Directory in "+URL.substring(1,URL.length())+"</TITLE></HEAD>\r\n");
										out.write("<BODY>\r\n");
										out.write("<H1>Files and Directory</H1><HR><PRE>\r\n");
										for (File f:files){
											out.write("<A HERF=\"localhost:8088/"+f+"\">"+f.getName()+"</A>\r\n");
										}
										out.write("</PRE><HR></BODY>\r\n");
										out.write("</HTML>\r\n");
										out.flush();
									}
								//not existed file
								}else{
									out.write("HTTP/1.1 404 Not Found\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>File Not Found</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>HTTP Error 404: File Not Found</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();	
								}
							}
							
						}else if (version.equals("HTTP/1.1")){
							//check if the request has a Host: header
							if(!request.toLowerCase().contains("host:")){
								out.write("HTTP/1.1 400 Bad Request\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("connection: close\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Bad Request</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H2>No Host: header received</H2>");
								out.write("HTTP 1.1 requests must include the Host: header.");
								out.write("</BODY></HTML>\r\n");
								out.flush();
							}else{
								//request:shutdown
								if(URL.equals("/shutdown")){
									out.write("HTTP/1.1 200 OK\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>ShutDown</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>Pleas Refresh the page to shutdown the server</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
									HttpServer.setIfShutDown();
								//request:control
								}else if(URL.equals("/control")){
									out.write("HTTP/1.1 200 OK\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Control Panel</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>Control Panel</H1>\r\n");
									out.write("<H2>Name: XIAONAN YANG  SEAS login: xiaonany</H2>\r\n");
									
									Set<Thread> allThread = Thread.getAllStackTraces().keySet();
									for(Thread t:allThread){
										if(t.getThreadGroup().getName().equals("Alpha")){
											out.write("<H4>Thread: "+t.getName()+"      Status: "+t.getState()+"\r\n</H4>");
										}
									}
									out.write("<H2><A HERF=\"/shutdown\"><button>ShutDown!</A></H2>\r\n");
									out.write("</BODY></HTML\r\n");
									out.flush();
								//request:absolute path
								}else if (URL.startsWith("http://localhost:"+port+"/")){						
									out.write("HTTP/1.1 403 Forbidden\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>HTTP Error 403: Absolute Path Forbidden</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
								//request:outsider path
								}else if (URL.startsWith("/..")){
									out.write("HTTP/1.1 403 Forbidden\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>HTTP Error 403: Outer Path Forbidden</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
								}
								//normal request
								else{
									File file = new File(rootDirectory,URL.substring(1,URL.length()));
									if (file.canRead()&&file.getCanonicalPath().startsWith(root)){
										//URL: file
										if(file.isFile()){
											contentType = getContentType(URL);
											Boolean outPutAlready = false;
											for (int i=3; i<st.countTokens(); i++){
												String message = st.nextToken();
												//check if request has If-Modified-Since: header
												if(message.toLowerCase().equals("if-modified-since:")){
													//get requested time
													String partTime;
													String checkTime = "";
													for(int j=0; j<4; j++){
														partTime = st.nextToken();
														checkTime += partTime+" ";
													}
													partTime = st.nextToken();
													while(Character.isDigit(partTime.charAt(partTime.length()-1))){
														checkTime += partTime+" ";
														partTime = st.nextToken();
													}
													Date ifModifiedSince = new Date();
													try{
														ifModifiedSince = sdf.parse(checkTime);
													}catch(Exception e1){
														try{
															ifModifiedSince = sdf1.parse(checkTime);
														}catch(Exception e2){
															try{
																ifModifiedSince = sdf2.parse(checkTime);
															}catch(Exception e3){
																//throw invalid if no date format matches
																out.write("HTTP/1.1 304 Not Modified\r\n");
																Date date = new Date();
																String gmtDate = sdf.format(date);
																out.write("Date: "+gmtDate+" GMT\r\n");;
																out.write("Server: localhost:" + port+"\r\n");
																out.write("connection: close\r\n");
																out.write("Content-type: text/html\r\n\r\n");
																out.write("<HTML>\r\n");
																out.write("<HEAD><TITLE>Not Modified</TITLE></HEAD>\r\n");
																out.write("<BODY>\r\n");
																out.write("<H1>HTTP Error 304: Not Modified</H1>");
																out.write("</BODY></HTML>\r\n");
																out.flush();
																outPutAlready = true;
																break;
															}
														}
													}
													//get lastModifiedTime and do comparison
													Date lastModifiedDate = new Date(file.lastModified());
													if (ifModifiedSince.after(lastModifiedDate)){
														out.write("HTTP/1.1 304 Not Modified\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-type: text/html\r\n\r\n");
														out.write("<HTML>\r\n");
														out.write("<HEAD><TITLE>File Not Modified</TITLE></HEAD>\r\n");
														out.write("<BODY>\r\n");
														out.write("<H1>HTTP Error 304: File Not Modified</H1>");
														out.write("</BODY></HTML>\r\n");
														out.flush();	
														outPutAlready = true;
													}else{
														DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
														byte[] data = new byte[(int)file.length()];
														fis.readFully(data);
														fis.close();
														out.write("HTTP/1.1 200 OK\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-length: "+data.length+"\r\n");
														out.write("Content-type: "+contentType+"\r\n\r\n");
														out.flush();
														raw.write(data);
														raw.flush();
														outPutAlready = true;					
													}
												//check if request has If-Unmodified-Since: header
												}else if(message.toLowerCase().equals("if-unmodified-since:")){
													//get requested time
													String partTime;
													String checkTime = "";
													for(int j=0; j<4; j++){
														partTime = st.nextToken();
														checkTime += partTime+" ";
													}
													partTime = st.nextToken();
													while(Character.isDigit(partTime.charAt(partTime.length()-1))){
														checkTime += partTime+" ";
														partTime = st.nextToken();
													}
													Date ifUnmodifiedSince = new Date();
													try{
														ifUnmodifiedSince = sdf.parse(checkTime);
													}catch(Exception e1){
														try{
															ifUnmodifiedSince = sdf1.parse(checkTime);
														}catch(Exception e2){
															try{
																ifUnmodifiedSince = sdf2.parse(checkTime);
															}catch(Exception e3){
																out.write("HTTP/1.1 304 Not Modified\r\n");
																Date date = new Date();
																String gmtDate = sdf.format(date);
																out.write("Date: "+gmtDate+" GMT\r\n");;
																out.write("Server: localhost:" + port+"\r\n");
																out.write("connection: close\r\n");
																out.write("Content-type: text/html\r\n\r\n");
																out.write("<HTML>\r\n");
																out.write("<HEAD><TITLE>Not Modified</TITLE></HEAD>\r\n");
																out.write("<BODY>\r\n");
																out.write("<H1>HTTP Error 304: Not Modified</H1>");
																out.write("</BODY></HTML>\r\n");
																out.flush();
																outPutAlready = true;
																break;
															}
														}
													}
													Date lastModifiedDate = new Date(file.lastModified());
													if (ifUnmodifiedSince.after(lastModifiedDate)){
														DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
														byte[] data = new byte[(int)file.length()];
														fis.readFully(data);
														fis.close();
														 
														out.write("HTTP/1.1 200 OK\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-length: "+data.length+"\r\n");
														out.write("Content-type: "+contentType+"\r\n\r\n");
														out.flush();
														raw.write(data);
														
														raw.flush();
														outPutAlready = true;
													}else{
														out.write("HTTP/1.1 412 Precondition Failed\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-type: text/html\r\n\r\n");
														out.write("<HTML>\r\n");
														out.write("<HEAD><TITLE>Precondition Failed</TITLE></HEAD>\r\n");
														out.write("<BODY>\r\n");
														out.write("<H1>HTTP Error 412: Precondition Failed</H1>");
														out.write("</BODY></HTML>\r\n");
														out.flush();	
														outPutAlready = true;
													}
												}
											}
											//if have neither If-Modified-Since nor If-Unmodified-Since,output file directory
											if(!outPutAlready){
												DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
												byte[] data = new byte[(int)file.length()];
												fis.readFully(data);
												fis.close();
												 
											
												out.write("HTTP/1.1 200 OK\r\n");
												Date date = new Date();
												String gmtDate = sdf.format(date);
												out.write("Date: "+gmtDate+" GMT\r\n");;
												out.write("Server: localhost:" + port+"\r\n");
												out.write("connection: close\r\n");
												String lastModifiedTime = sdf.format(file.lastModified());
												out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
												out.write("Content-length: "+data.length+"\r\n");
												out.write("Content-type: "+contentType+"\r\n\r\n");
												
												out.flush();
												raw.write(data);
												raw.flush();
											}
										//URL: Directory
										}else{
											out.write("HTTP/1.1 200 OK\r\n");
											Date date = new Date();
											String gmtDate = sdf.format(date);
											out.write("Date: "+gmtDate+" GMT\r\n");;
											out.write("Server: localhost:" + port+"\r\n");
											String lastModifiedTime = sdf.format(file.lastModified());
											out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
											out.write("Content-type: text/html\r\n\r\n");
											File[] files = file.listFiles();
											out.write("<HTML>\r\n");
											out.write("<HEAD><TITLE>Files and Directory in "+URL.substring(1,URL.length())+"</TITLE></HEAD>\r\n");
											out.write("<BODY>\r\n");
											out.write("<H1>Files and Directory</H1><HR><PRE>\r\n");
											for (File f:files){
												out.write("<A HERF=\"localhost:8088/"+f+"\">"+f.getName()+"</A>\r\n");
											}
											out.write("</PRE><HR></BODY>\r\n");
											out.write("</HTML>\r\n");
											out.flush();
										}
									//not existed file
									}else{
										out.write("HTTP/1.1 404 Not Found\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										out.write("connection: close\r\n");
										out.write("Content-type: text/html\r\n\r\n");
										out.write("<HTML>\r\n");
										out.write("<HEAD><TITLE>File Not Found</TITLE></HEAD>\r\n");
										out.write("<BODY>\r\n");
										out.write("<H1>HTTP Error 404: File Not Found</H1>");
										out.write("</BODY></HTML>\r\n");
										out.flush();	
									}
								}
							}
						//if version is neither 1.0 nor 1.1
						}else{
							out.write("HTTP/1.1 505 HTTP Version not supported\r\n");
							Date date = new Date();
							String gmtDate = sdf.format(date);
							out.write("Date: "+gmtDate+"\r\n");
							out.write("Server: localhost:" + port+"\r\n");
							out.write("Content-type: text/html\r\n\r\n");
							out.write("<HTML>\r\n");
							out.write("<HEAD><TITLE> HTTP Version not supported</TITLE></HEAD>\r\n");
							out.write("<BODY>\r\n");
							out.write("<H1>HTTP Error 505: HTTP Version not supported</H1>");
							out.write("</BODY></HTML>\r\n");
							out.flush();	
						}
						
						
						
					// if method is head, all similar to GET without output body	
					}else if(method.equals("HEAD")){                            
						if(version .equals( "HTTP/1.0")){
							if(URL.equals("/shutdown")){
								out.write("HTTP/1.1 200 OK\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>ShutDown</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>Pleas Refresh the page to shutdown the server</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
								HttpServer.setIfShutDown();
							}else if(URL.equals("/control")){
								out.write("HTTP/1.1 200 OK\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Control Panel</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>Control Panel</H1>\r\n");
								out.write("<H2>Name: XIAONAN YANG  SEAS login: xiaonany</H2>\r\n");
								
								Set<Thread> allThread = Thread.getAllStackTraces().keySet();
								for(Thread t:allThread){
									if(t.getThreadGroup().getName().equals("Alpha")){
										out.write("<H4>Thread: "+t.getName()+"      Status: "+t.getState()+"\r\n</H4>");
									}
								}
								out.write("<H2><A HERF=\"/shutdown\"><button>ShutDown!</A></H2>\r\n");
								out.write("</BODY></HTML\r\n");
								out.flush();
							}else if (URL.startsWith("http://localhost:"+port+"/")){						
								out.write("HTTP/1.1 403 Forbidden\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>HTTP Error 403: Absolute Path Forbidden</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
							}
							else if (URL.startsWith("/..")){
								out.write("HTTP/1.1 403 Forbidden\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");;
								out.write("Content-type: text/html\r\n\r\n");
								out.write("<HTML>\r\n");
								out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
								out.write("<BODY>\r\n");
								out.write("<H1>HTTP Error 403: Outer Path Forbidden</H1>");
								out.write("</BODY></HTML>\r\n");
								out.flush();
							}else{
								File file = new File(rootDirectory,URL.substring(1,URL.length()));
								if (file.canRead()&&file.getCanonicalPath().startsWith(root)){
									if (file.isFile()){
										contentType = getContentType(URL);
										DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
										byte[] data = new byte[(int)file.length()];
										fis.readFully(data);
										fis.close();
										 
										out.write("HTTP/1.1 200 OK\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										String lastModifiedTime = sdf.format(file.lastModified());
										out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
										out.write("Content-length: "+data.length+"\r\n");
										out.write("Content-type: "+contentType+"\r\n");
										out.flush();
									}else{
										out.write("HTTP/1.1 200 OK\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										String lastModifiedTime = sdf.format(file.lastModified());
										out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
										out.write("Content-type: text/html\r\n");
										out.flush();
									}
								}else{
									out.write("HTTP/1.1 404 Not Found\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("Content-type: text/html\r\n");
									out.flush();	
								}
							}
						}else if (version.equals("HTTP/1.1")){
							if(!request.toLowerCase().contains("host:")){
								out.write("HTTP/1.1 400 Bad Request\r\n");
								Date date = new Date();
								String gmtDate = sdf.format(date);
								out.write("Date: "+gmtDate+" GMT\r\n");;
								out.write("Server: localhost:" + port+"\r\n");
								out.write("connection: close\r\n");
								out.write("Content-type: text/html\r\n");
								out.flush();
							}else{
								if(URL.equals("/shutdown")){
									out.write("HTTP/1.1 200 OK\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>ShutDown</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>Pleas Refresh the page to shutdown the server</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
									HttpServer.setIfShutDown();
								}else if(URL.equals("/control")){
									out.write("HTTP/1.1 200 OK\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Control Panel</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>Control Panel</H1>\r\n");
									out.write("<H2>Name: XIAONAN YANG  SEAS login: xiaonany</H2>\r\n");
									
									Set<Thread> allThread = Thread.getAllStackTraces().keySet();
									for(Thread t:allThread){
										if(t.getThreadGroup().getName().equals("Alpha")){
											out.write("<H4>Thread: "+t.getName()+"      Status: "+t.getState()+"\r\n</H4>");
										}
									}
									out.write("<H2><A HERF=\"/shutdown\"><button>ShutDown!</A></H2>\r\n");
									out.write("</BODY></HTML\r\n");
									out.flush();
								}else if (URL.startsWith("http://localhost:"+port+"/")){						
									out.write("HTTP/1.1 403 Forbidden\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>HTTP Error 403: Absolute Path Forbidden</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
								}else if (URL.startsWith("/..")){
									out.write("HTTP/1.1 403 Forbidden\r\n");
									Date date = new Date();
									String gmtDate = sdf.format(date);
									out.write("Date: "+gmtDate+" GMT\r\n");;
									out.write("Server: localhost:" + port+"\r\n");
									out.write("connection: close\r\n");
									out.write("Content-type: text/html\r\n\r\n");
									out.write("<HTML>\r\n");
									out.write("<HEAD><TITLE>Forbidden</TITLE></HEAD>\r\n");
									out.write("<BODY>\r\n");
									out.write("<H1>HTTP Error 403: Outer Path Forbidden</H1>");
									out.write("</BODY></HTML>\r\n");
									out.flush();
								}
								else{
									File file = new File(rootDirectory,URL.substring(1,URL.length()));
									if (file.canRead()&&file.getCanonicalPath().startsWith(root)){
										if(file.isFile()){
											contentType = getContentType(URL);
											Boolean outPutAlready = false;
											for (int i=3; i<st.countTokens(); i++){
												String message = st.nextToken();
												if(message.toLowerCase().equals("if-modified-since:")){
													//get requested time
													String partTime;
													String checkTime = "";
													for(int j=0; j<4; j++){
														partTime = st.nextToken();
														checkTime += partTime+" ";
													}
													partTime = st.nextToken();
													while(Character.isDigit(partTime.charAt(partTime.length()-1))){
														checkTime += partTime+" ";
														partTime = st.nextToken();
													}
													Date ifModifiedSince = new Date();
													try{
														ifModifiedSince = sdf.parse(checkTime);
													}catch(Exception e1){
														try{
															ifModifiedSince = sdf1.parse(checkTime);
														}catch(Exception e2){
															try{
																ifModifiedSince = sdf2.parse(checkTime);
															}catch(Exception e3){
																out.write("HTTP/1.1 304 Not Modified\r\n");
																Date date = new Date();
																String gmtDate = sdf.format(date);
																out.write("Date: "+gmtDate+" GMT\r\n");;
																out.write("Server: localhost:" + port+"\r\n");
																out.write("connection: close\r\n");
																out.write("Content-type: text/html\r\n");
																out.flush();
																outPutAlready = true;
																break;
															}
														}
													}
													Date lastModifiedDate = new Date(file.lastModified());
													if (ifModifiedSince.after(lastModifiedDate)){
														out.write("HTTP/1.1 304 Not Modified\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-type: text/html\r\n");
														out.flush();	
													}else{
														DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
														byte[] data = new byte[(int)file.length()];
														fis.readFully(data);
														fis.close();
														 
														out.write("HTTP/1.1 200 OK\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-length: "+data.length+"\r\n");
														out.write("Content-type: "+contentType+"\r\n");
														out.flush();
													
													}
													outPutAlready = true;
												}else if(message.toLowerCase().equals("if-unmodified-since:")){
													//get requested time
													String partTime;
													String checkTime = "";
													for(int j=0; j<4; j++){
														partTime = st.nextToken();
														checkTime += partTime+" ";
													}
													partTime = st.nextToken();
													while(Character.isDigit(partTime.charAt(partTime.length()-1))){
														checkTime += partTime+" ";
														partTime = st.nextToken();
													}
													Date ifUnmodifiedSince = new Date();
													try{
														ifUnmodifiedSince = sdf.parse(checkTime);
													}catch(Exception e1){
														try{
															ifUnmodifiedSince = sdf1.parse(checkTime);
														}catch(Exception e2){
															try{
																ifUnmodifiedSince = sdf2.parse(checkTime);
															}catch(Exception e3){
																out.write("HTTP/1.1 304 Not Modified\r\n");
																Date date = new Date();
																String gmtDate = sdf.format(date);
																out.write("Date: "+gmtDate+" GMT\r\n");;
																out.write("Server: localhost:" + port+"\r\n");
																out.write("connection: close\r\n");
																out.write("Content-type: text/html\r\n");
																out.flush();
																outPutAlready = true;
																break;
															}
														}
													}
													Date lastModifiedDate = new Date(file.lastModified());
													if (ifUnmodifiedSince.after(lastModifiedDate)){
														DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
														byte[] data = new byte[(int)file.length()];
														fis.readFully(data);
														fis.close();
														 
														out.write("HTTP/1.1 200 OK\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-length: "+data.length+"\r\n");
														out.write("Content-type: "+contentType+"\r\n");
														out.flush();
													}else{
														out.write("HTTP/1.1 412 Precondition Failed\r\n");
														Date date = new Date();
														String gmtDate = sdf.format(date);
														out.write("Date: "+gmtDate+" GMT\r\n");;
														out.write("Server: localhost:" + port+"\r\n");
														out.write("connection: close\r\n");
														String lastModifiedTime = sdf.format(file.lastModified());
														out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
														out.write("Content-type: text/html\r\n\r\n");
														out.flush();
													}
												}
												outPutAlready = true;
											}
											if(!outPutAlready){
												DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
												byte[] data = new byte[(int)file.length()];
												fis.readFully(data);
												fis.close();
												 
												out.write("HTTP/1.1 200 OK\r\n");
												Date date = new Date();
												String gmtDate = sdf.format(date);
												out.write("Date: "+gmtDate+" GMT\r\n");;
												out.write("Server: localhost:" + port+"\r\n");
												out.write("connection: close\r\n");
												String lastModifiedTime = sdf.format(file.lastModified());
												out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
												out.write("Content-length: "+data.length+"\r\n");
												out.write("Content-type: "+contentType+"\r\n");
												out.flush();
											}
										}else{
											out.write("HTTP/1.1 200 OK\r\n");
											Date date = new Date();
											String gmtDate = sdf.format(date);
											out.write("Date: "+gmtDate+" GMT\r\n");;
											out.write("Server: localhost:" + port+"\r\n");
											String lastModifiedTime = sdf.format(file.lastModified());
											out.write("Last-Modified: "+lastModifiedTime+" GMT\r\n");
											out.write("Content-type: text/html\r\n");
											out.flush();
										}
									}else{
										out.write("HTTP/1.1 404 Not Found\r\n");
										Date date = new Date();
										String gmtDate = sdf.format(date);
										out.write("Date: "+gmtDate+" GMT\r\n");;
										out.write("Server: localhost:" + port+"\r\n");
										out.write("connection: close\r\n");
										out.write("Content-type: text/html\r\n");
										out.flush();	
									}
								}
							}
						}else{
							out.write("HTTP/1.1 505 HTTP Version not supported\r\n");
							Date date = new Date();
							String gmtDate = sdf.format(date);
							out.write("Date: "+gmtDate+"\r\n");
							out.write("Server: localhost:" + port+"\r\n");
							out.write("Content-type: text/html\r\n");
							out.flush();	
						}
					//if method is neither GET nor HEAD
					}else if((method.toLowerCase().equals("get")&&!method.equals("GET"))||(method.toLowerCase().equals("head")&&!method.equals("HEAD"))){
						out.write("HTTP/1.1 400 Bad Request\r\n");
						Date date = new Date();
						String gmtDate = sdf.format(date);
						out.write("Date: "+gmtDate+"\r\n");
						out.write("Server: localhost:" + port+"\r\n");
						out.write("Content-type: text/html\r\n\r\n");
						out.write("<HTML>\r\n");
						out.write("<HEAD><TITLE> Bad Request</TITLE></HEAD>\r\n");
						out.write("<BODY>\r\n");
						out.write("<H1>HTTP Error 400: Bad Request</H1>");
						out.write("</BODY></HTML>\r\n");
						out.flush();	
					}else{
						out.write("HTTP/1.1 501 Not Implemented\r\n");
						Date date = new Date();
						String gmtDate = sdf.format(date);
						out.write("Date: "+gmtDate+"\r\n");
						out.write("Server: localhost:" + port+"\r\n");
						out.write("Content-type: text/html\r\n\r\n");
						out.write("<HTML>\r\n");
						out.write("<HEAD><TITLE> Not Implemented</TITLE></HEAD>\r\n");
						out.write("<BODY>\r\n");
						out.write("<H1>HTTP Error 501: Not Implemented</H1>");
						out.write("</BODY></HTML>\r\n");
						out.flush();	
					}
				}
			connection.close();
			}		
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//get the type of output content type according to fileName
	public static String getContentType(String name){
		String contentType = "";
		if(name.endsWith(".html")){
			contentType = "text/html";
		}else if(name.endsWith(".txt")){
			contentType = "text/plain";
		}else if(name.endsWith(".gif")){
			contentType = "image/gif";
		}else if(name.endsWith(".jpg")){
			contentType = "image/jpeg";
		}else if(name.endsWith(".png")){
			contentType = "image/png";
		}
		return contentType;
	}
}
	

	



