import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.json.JSONObject;

import com.catalyst.config.ZCThreadLocal;
import com.cop.main.server.ServerConstants;

//$Id$

public class ProjectFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		JSONObject catalystConfig = new JSONObject();
		try {
			catalystConfig.put("project_id", System.getenv(ServerConstants.PROJECTID_HEADER_KEY)); // No I18N
			catalystConfig.put("project_key", System.getenv(ServerConstants.PROJECT_ZAID_HEADER_KEY)); // No I18N
			catalystConfig.put("project_domain", System.getenv(ServerConstants.PROJECT_DOMAIN_HEADER_KEY).replace("https://", ""));
			catalystConfig.put("environment", System.getenv(ServerConstants.PROJECT_ENVIRONMENT_HEADER_KEY)); // No I18N
		} catch(Exception e) {
			throw new ServletException("Unable to set project init params", e);
		}
		ZCThreadLocal.putValue("CATALYST_CONFIG", catalystConfig.toString()); // No I18N
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
