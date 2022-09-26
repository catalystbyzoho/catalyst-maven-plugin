import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.eclipse.jetty.http.HttpHeader;
import org.json.JSONObject;

import com.catalyst.config.ZCThreadLocal;
import com.cop.main.server.ServerConstants;

/**
 * @author thanga-5521
 *
 */
public class AuthFilter implements Filter {
	
	private static boolean isProjectUser(String cookies, String auth) throws IOException {
		boolean isProjectUser = false;
		String domainUrl = System.getenv(ServerConstants.PROJECT_DOMAIN_HEADER_KEY) + "/baas/v1/project/" + System.getenv(ServerConstants.PROJECTID_HEADER_KEY) + "/project-user/current";
		System.out.println(domainUrl);
		URL obj = new URL(domainUrl);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setRequestMethod("GET");
		con.setRequestProperty(HttpHeader.COOKIE.asString(), cookies);
		con.setRequestProperty(HttpHeader.AUTHORIZATION.asString(), auth);
		int responseCode = con.getResponseCode();
		System.out.println("GET Response Code :: " + responseCode);
		if (responseCode == HttpURLConnection.HTTP_OK) { // success
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			// print result
			System.out.println("response" + response.toString());
		} else {
			System.out.println("GET request not worked");
		}
		return isProjectUser;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) req;
		Cookie[] cookies = httpReq.getCookies();
		boolean hasADT = false;
		boolean hasBDT = false;
		boolean hasCSRF = false;
		if(cookies != null) {
			for(Cookie cookie : cookies) {
				if(cookie.getName().equals("ZD_CSRF_TOKEN") && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
					hasCSRF = true;
				}
				else if(cookie.getName().startsWith("_iamadt_client") && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
					hasADT = true;
				}
				else if(cookie.getName().startsWith("_iambdt_client") && cookie.getValue() != null && !cookie.getValue().isEmpty()) {
					hasBDT = true;
				}
			}
		}
		String adminAuthToken = System.getenv("X_ZOHO_CATALYST_ADMIN_TOKEN");
		String adminAuthHeaderType = "token";
		String userAuthToken = null;
		String userAuthHeaderType = null;
		String userCredUserType = null;
		String cookieStr = null;
		if (hasCSRF && hasADT && hasBDT) {
			// cookie based authentication
			userCredUserType = "project-user";
			cookieStr = httpReq.getHeader(HttpHeader.COOKIE.asString());
		} else if (httpReq.getHeader(HttpHeader.AUTHORIZATION.asString()) != null) {
			String authStr = httpReq.getHeader(HttpHeader.AUTHORIZATION.asString());
			if(isProjectUser(httpReq.getHeader(HttpHeader.COOKIE.asString()), authStr)) {
				userCredUserType = "project-user";
				userAuthHeaderType = "token";
				userAuthToken = authStr.split(" ")[1];
			} else {
				userAuthHeaderType = "token";
				userAuthToken = adminAuthToken;
				userCredUserType = "admin";
			}
		} else {
			userAuthHeaderType = "token";
			userAuthToken = adminAuthToken;
			userCredUserType = "admin";
		}
		JSONObject catalystAuth = new JSONObject();
		try {
			JSONObject adminAuth = new JSONObject();
			adminAuth.put((adminAuthHeaderType.equals(ServerConstants.TOKEN)) ? "access_token" : "ticket", adminAuthToken);
			catalystAuth.put(ServerConstants.ADMIN_CRED, adminAuth);

			JSONObject clientAuth = new JSONObject();
			if (cookieStr != null) {
				clientAuth.put("cookie", cookieStr);
				ZCThreadLocal.putValue("client_cookie", cookieStr); // for backward compatibility
			} else if (userAuthHeaderType != null && userAuthToken != null) {
				clientAuth.put((userAuthHeaderType.equals(ServerConstants.TOKEN)) ? "access_token" : "ticket", userAuthToken);// No I18N
			}
			if (userCredUserType != null) {
				clientAuth.put(ServerConstants.THREAD_LOCAL_USER_TYPE_KEY, userCredUserType);
			}
			catalystAuth.put(ServerConstants.CLIENT_CRED, clientAuth);
		} catch(Exception e) {
			throw new ServletException("Unable to set auth init params", e);
		}
		ZCThreadLocal.putValue("CATALYST_AUTH", catalystAuth.toString()); // No I18N
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
