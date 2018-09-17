import java.util.Enumeration;
import java.util.*;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;

public class ServletContext implements javax.servlet.ServletContext{
	private HashMap<String,HttpServlet> ContextServlets;
	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	private List<HttpSession> sessions = new LinkedList<HttpSession>();
	public ServletContext() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
		ContextServlets = new HashMap<String,HttpServlet>();
	}
	
	public HttpSession getSession(String ID){
		for(HttpSession s:sessions){
			if (s.getId().equals(ID)){
				return s;
			}
		}
		return null;
	}
	
	public void addSession(HttpSession session){
		sessions.add(session);
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration<String> getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {
		return null;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	public String getMimeType(String file) {         //exception
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	public RequestDispatcher getNamedDispatcher(String name) {   //exception
		return null;
	}
	
	public String getRealPath(String path) {
		
		return null;
	}
	
	public RequestDispatcher getRequestDispatcher(String name) {     //exception
		return null;
	}
	
	public java.net.URL getResource(String path) {		 //exception
		return null;
	}
	
	public java.io.InputStream getResourceAsStream(String path) {   //exception
		return null;
	}
	
	public Set<?> getResourcePaths(String path) {     //exception
		return null;
	}
	
	public String getServerInfo() {
		return "HttpServer";
	}
	
	public HttpServlet getServlet(String name) {
		return ContextServlets.get(name);
	}
	
	public String getServletContextName() {
		return "HttpServer";
	}
	
	public Enumeration<String> getServletNames() {
		Set<String> keys = ContextServlets.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public Enumeration<Servlet> getServlets() {
		Vector<Servlet> atts = new Vector<Servlet>();
		for(String name:ContextServlets.keySet()){
			atts.add(ContextServlets.get(name));
		}
		return atts.elements();
	}
	
	public void setServlet(String name, HttpServlet servlet){
		ContextServlets.put(name, servlet);
	}
	
	public void log(Exception exception, String msg) {   //exception
	}
	
	public void log(String msg) {						 //exception
		System.err.println(msg);
	}
	
	public void log(String message, Throwable throwable) { //exception
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	public void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

	public String getContextPath() {           //exception
		return null;
	}
}

