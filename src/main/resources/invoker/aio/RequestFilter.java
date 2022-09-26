//$Id$

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.catalyst.config.ZCThreadLocal;
import com.cop.main.server.ServerConstants;

/**
 * @author thanga-5521
 *
 */
public class RequestFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		JSONObject requestConfig = new JSONObject();
		try {  
			requestConfig.put(ServerConstants.THREAD_LOCAL_REQUEST_UUID_KEY, UUID.randomUUID().toString());
		} catch (JSONException e) {
			throw new ServletException("Unable to set request init params", e);
		}
		ZCThreadLocal.putValue("REQUEST_CONFIG", requestConfig.toString());// No I18N
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}

}
