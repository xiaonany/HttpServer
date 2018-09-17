import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletContext;

public class ServletConfig implements javax.servlet.ServletConfig{
	private String name;
	private ServletContext context;
	private HashMap<String,String> initParams;
	
	public ServletConfig(String name, ServletContext context){
		this.name = name;
		this.context = context;
		this.initParams = new HashMap<String,String>();
	}
	
	@Override
	public String getInitParameter(String name) {
		return initParams.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public String getServletName() {
		return this.name;
	}
	
	public void setInitParam(String name, String value) {
		initParams.put(name, value);
	}

}

