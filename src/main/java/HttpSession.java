import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

public class HttpSession implements javax.servlet.http.HttpSession{
	private Properties props = new Properties();
	private boolean valid = true;
	private UUID SESSIONID;
	private Date creationTime;
	private int MaxInactiveInterval;
	boolean isNew;
	private Date lastAcesssTime;
	public HttpSession(){
		isNew = true;
		valid = true;
		this.SESSIONID = UUID.randomUUID();
		this.creationTime = new Date();
		this.lastAcesssTime = new Date();
		MaxInactiveInterval = 1000;
	}
	@Override
	public Object getAttribute(String arg0) {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return props.get(arg0);
	}

	@Override
	public Enumeration<?> getAttributeNames() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return props.keys();
	}

	@Override
	public long getCreationTime() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return creationTime.getTime();
	}

	@Override
	public String getId() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return SESSIONID.toString();
	}

	@Override
	public long getLastAccessedTime() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return lastAcesssTime.getTime();
	}

	@Override
	public int getMaxInactiveInterval() {
		return this.MaxInactiveInterval;
	}

	@Override
	public ServletContext getServletContext() {
		return HttpServer.sc;
	}

	@Override
	public void invalidate() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		valid = false;
	}

	@Override
	public boolean isNew() {
		if(!isValid()){
			throw new IllegalStateException();
		}
		return isNew;
	}



	@Override
	public void removeAttribute(String arg0) {
		if(!isValid()){
			throw new IllegalStateException();
		}
		props.remove(arg0);
	}

	@Override
	public void setAttribute(String arg0, Object arg1) {
		if(!isValid()){
			throw new IllegalStateException();
		}
		props.put(arg0, arg1);
	}

	@Override
	public void setMaxInactiveInterval(int arg0) {
		if(!isValid()){
			throw new IllegalStateException();
		}
		this.MaxInactiveInterval = arg0;
	}
	
	public boolean isValid() {
		Date now = new Date();
		if(now.getTime() - lastAcesssTime.getTime() > TimeUnit.MINUTES.toMillis(MaxInactiveInterval)){
			valid = false;
			HttpServer.sessionMap.remove(SESSIONID.toString());
		}
		return valid;
	}
	
	@Override
	public void removeValue(String arg0) {			//deprecate
		props.remove(arg0);
	}
	
	@Override
	public void putValue(String arg0, Object arg1) {  //deprecate
		props.put(arg0, arg1);
	}
	
	@Override
	public String[] getValueNames() {				//deprecate
		return null;
	}
	
	@Override
	public Object getValue(String arg0) {			//deprecate
		return props.get(arg0);
	}
	
	@Override
	public HttpSessionContext getSessionContext() {  //deprecate
		return null;
	}

}

