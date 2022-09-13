//$Id$

import java.io.IOException;

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
public class ThreadlocalFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		String adminAuthHeaderType = System.getenv(ServerConstants.ADMIN_CRED_TYPE_HEADER_KEY);
		String adminAuthToken = System.getenv(ServerConstants.ADMIN_CRED_TOKEN_HEADER_KEY);
		String userAuthHeaderType = System.getenv(ServerConstants.USER_CRED_TYPE_HEADER_KEY);
		String userAuthToken = System.getenv(ServerConstants.USER_CRED_TOKEN_HEADER_KEY);
		String environment = System.getenv(ServerConstants.PROJECT_ENVIRONMENT_HEADER_KEY);
		String cookieStr = System.getenv(ServerConstants.USER_CRED_COOKIE);
		String catalystAPIKey = System.getenv(ServerConstants.CATALYST_PROJECT_SECRET_HEADER_KEY);

		JSONObject catalystConfig = new JSONObject();
		JSONObject catalystAuth = new JSONObject();
		JSONObject requestConfig = new JSONObject();
		try {
			catalystConfig.put("project_id", System.getenv(ServerConstants.PROJECTID_HEADER_KEY)); // No I18N
			catalystConfig.put("project_key", System.getenv(ServerConstants.PROJECT_ZAID_HEADER_KEY)); // No I18N
			catalystConfig.put("project_domain", System.getenv(ServerConstants.PROJECT_DOMAIN_HEADER_KEY));
			catalystConfig.put("environment", environment); // No I18N

			JSONObject adminAuth = new JSONObject();
			adminAuth.put((adminAuthHeaderType.equals(ServerConstants.TOKEN)) ? "access_token" : "ticket",
					// No I18N
					adminAuthToken);
			adminAuth.put(ServerConstants.CATALYST_PROJECT_SECRET_HEADER_KEY, catalystAPIKey);
			catalystAuth.put(ServerConstants.ADMIN_CRED, adminAuth);

			JSONObject clientAuth = new JSONObject();
			if (cookieStr != null) {
				clientAuth.put("cookie", cookieStr);
				ZCThreadLocal.putValue("client_cookie", cookieStr); // for backward compatibility
			} else if (userAuthHeaderType != null && userAuthToken != null) {
				clientAuth.put((userAuthHeaderType.equals(ServerConstants.TOKEN)) ? "access_token" : "ticket", userAuthToken);// No I18N
			}
			if (System.getenv(ServerConstants.USER_CRED_USER_TYPE_HEADER_NAME) != null) {
				clientAuth.put(ServerConstants.THREAD_LOCAL_USER_TYPE_KEY, System.getenv(ServerConstants.USER_CRED_USER_TYPE_HEADER_NAME));
			}
			catalystAuth.put(ServerConstants.CLIENT_CRED, clientAuth);

			if (System.getenv(ServerConstants.REQUEST_UUID_HEADER_KEY) != null) {
				requestConfig.put(ServerConstants.THREAD_LOCAL_REQUEST_UUID_KEY, System.getenv(ServerConstants.REQUEST_UUID_HEADER_KEY));
			}
		} catch (JSONException e) {
			throw new ServletException("Unable to set init params", e);
		}

		ZCThreadLocal.putValue("CATALYST_CONFIG", catalystConfig.toString()); // No I18N
		ZCThreadLocal.putValue("CATALYST_AUTH", catalystAuth.toString()); // No I18N
		ZCThreadLocal.putValue("REQUEST_CONFIG", requestConfig.toString());// No I18N

		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
	}

}
