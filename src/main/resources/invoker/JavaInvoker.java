import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.servlet.Filter;

import com.cop.main.server.DefaultCoreServer;

public class JavaInvoker extends DefaultCoreServer {

	private static final Integer MESSAGE_LENGTH = 1500;
	private static int port;

	public class LogHandler extends Handler {

		@Override
		public void publish(LogRecord record) {
			try {
				String exceptionMessage = "";
				if (record.getThrown() != null) {
					exceptionMessage = getStackTraceAsString(record.getThrown());
				}
				String message = new SimpleFormatter().format(record) + " " + exceptionMessage;
				if (message.length() > MESSAGE_LENGTH) {
					message = message.substring(0, MESSAGE_LENGTH);
				}

				System.out.println("[" + record.getLevel().getName() + "] : " + message);

			} catch (Exception e) {
				System.out.println(getStackTraceAsString(e));
			}

		}

		@Override
		public void flush() {
			// TODO Auto-generated method stub

		}

		@Override
		public void close() throws SecurityException {
			// TODO Auto-generated method stub

		}

	}

	private static String getStackTraceAsString(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		throwable.printStackTrace(new PrintWriter(stringWriter));
		return stringWriter.toString();
	}


	@Override
	public void start() throws Exception {
		this.setPort(port);
		super.start();
	}


	public static void main(String[] args) {
		try {
			LogManager manager = LogManager.getLogManager();
			manager.reset();
			Logger rootLogger = manager.getLogger("");
			JavaInvoker aioServer = new JavaInvoker();
			// adding log handler
			LogHandler handler = aioServer.new LogHandler();
			rootLogger.addHandler(handler);
			// adding filter for setting thread local
			ArrayList<Class<? extends Filter>> filterClass = new ArrayList<>();
			filterClass.add(ProjectFilter.class);
			filterClass.add(AuthFilter.class);
			filterClass.add(RequestFilter.class);
			aioServer.setFilterClass(filterClass);

			port = Integer.parseInt(args[0]);
			aioServer.start();
		}
		catch (Exception e) {
			System.out.println(getStackTraceAsString(e));
			System.exit(2);
		}
	}
}
