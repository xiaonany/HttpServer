import java.io.*;
import java.net.*;
import java.util.HashMap;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
public class HttpServer {
	static class Handler extends DefaultHandler {
		private int state = 0;
		private String servletName;
		private String paramName;
		private HashMap<String,String> servlets = new HashMap<String,String>();
		private HashMap<String,String> contextParams = new HashMap<String,String>();
		private HashMap<String,HashMap<String,String>> servletParams = new HashMap<String,HashMap<String,String>>();
		private HashMap<String,String> servletUrl = new HashMap<String, String>();
		private String sessionTimeout = "";
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if (qName.compareTo("servlet-name") == 0) {
				state = 1;
			} else if (qName.compareTo("servlet-class") == 0) {
				state = 2;
			} else if (qName.compareTo("url-pattern") == 0){
				state = 3;
			} else if (qName.compareTo("context-param") == 0) {
				state = 4;
			} else if (qName.compareTo("init-param") == 0) {
				state = 5;
			} else if (qName.compareTo("param-name") == 0) {
				state = (state == 4) ? 10 : 20;
			} else if (qName.compareTo("param-value") == 0) {
				state = (state == 10) ? 11 : 21;
			} else if (qName.compareTo("seesion-timeout") ==0) {
				state = 6;
			}
		}
		public void characters(char[] ch, int start, int length) {
			String value = new String(ch, start, length);
			if (state == 1) {
				servletName = value;
				state = 0;
			} else if (state == 2) {
				servlets.put(servletName, value);
				state = 0;
			} else if (state == 3) {
				servletUrl.put(servletName, value);
				state = 0;
			} else if (state == 10 || state == 20) {
				paramName = value;
			} else if (state == 11) {
				if (paramName == null) {
					System.err.println("Context parameter value '" + value + "' without name");
					System.exit(-1);
				}
				contextParams.put(paramName, value);
				paramName = null;
				state = 0;
			} else if (state == 21) {
				if (paramName == null) {
					System.err.println("Servlet parameter value '" + value + "' without name");
					System.exit(-1);
				}
				HashMap<String,String> p = servletParams.get(servletName);
				if (p == null) {
					p = new HashMap<String,String>();
					servletParams.put(servletName, p);
				}
				p.put(paramName, value);
				paramName = null;
				state = 0;
			} else if (state == 6) {
				sessionTimeout = value;
				state = 0;
			}
		}
	}
	
	private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private static ServletContext createContext(Handler h) {
		ServletContext sc = new ServletContext();
		for (String param : h.contextParams.keySet()) {
			sc.setInitParam(param, h.contextParams.get(param));
		}
		return sc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(Handler h, ServletContext fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.servlets.keySet()) {
			ServletConfig config = new ServletConfig(servletName, fc);
			String className = h.servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}
	
	public static int SizeOfPool = 100;
	public static boolean ifShutDown = false;
	public static ServletContext sc;
	public static HashMap<String, HttpServlet> servlets = new HashMap<String, HttpServlet>();
	static HashMap<String, String> UrlPattern = new HashMap<String, String>();
	static int port;
	public static HashMap<String, HttpSession> sessionMap = new HashMap<String, HttpSession>();
	public static void main(String[] args) throws Exception{
		//get the arguments
		if (args.length==0){
			System.out.println("Name: XIAONAN YANG");
			System.out.println("SEAS Login: xiaonany");
			return;
		}else if(args.length==1){
			System.out.println("Please enter rootDirectory and the location of Web.xml");
			return;
		}else if(args.length==2){
			System.out.println("Please enter the location of Web.xml");
			return;
		}
		
		port = Integer.parseInt(args[0]);
		File rootDir = new File(args[1]);
		String webDir = args[2];
		//construct and start threadPool(including starting workerThread)
		ThreadPool tp = new ThreadPool("Alpha",SizeOfPool);
		tp.startThreadPool();
		
		try{
			Handler h = parseWebdotxml(webDir);
			UrlPattern = h.servletUrl;
			sc = createContext(h);
			servlets = createServlets(h,sc);
		}catch(Exception e){
			e.printStackTrace();
		}
		//Keep accepting connections from socket
		ServerSocket server = null;
		try{
			server = new ServerSocket(port);
		}catch (IOException e){
			e.printStackTrace();
		}

		while(true){
			Socket socket = null;
			try{
				socket = server.accept();
				Task task = new Task(socket,rootDir,port, servlets,UrlPattern );
				tp.addRequest(task);
			}catch (IOException e){
				e.printStackTrace();
				continue;
			}
			//keep tracking shutingDown flag
			if(ifShutDown){
				tp.stopThreadPool();
				System.out.println("Server is closing...");
				try {
					server.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		
		System.out.println("Server is closed");
		
    }
	
	public static void setIfShutDown(){
		ifShutDown = true;
	}
}
  

