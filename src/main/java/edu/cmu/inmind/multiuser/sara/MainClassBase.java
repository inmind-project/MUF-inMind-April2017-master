package edu.cmu.inmind.multiuser.sara;

import edu.cmu.inmind.multiuser.common.SaraCons;
import edu.cmu.inmind.multiuser.controller.common.Constants;
import edu.cmu.inmind.multiuser.controller.common.Utils;
import edu.cmu.inmind.multiuser.controller.log.Log4J;
import edu.cmu.inmind.multiuser.controller.log.MessageLog;
import edu.cmu.inmind.multiuser.controller.muf.MUFLifetimeManager;
import edu.cmu.inmind.multiuser.controller.muf.MultiuserController;
import edu.cmu.inmind.multiuser.controller.muf.ShutdownHook;
import edu.cmu.inmind.multiuser.controller.plugin.PluginModule;
import edu.cmu.inmind.multiuser.controller.resources.Config;
import edu.cmu.inmind.multiuser.sara.log.ExceptionLogger;
import edu.cmu.inmind.multiuser.sara.net.PreferredInetAddressFinder;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by oscarr on 3/20/17.
 * Main class for MUF 2.8+
 */
public abstract class MainClassBase {

	private static final Path CONNECTION_PROPS_FILE_PATH = Paths.get("connection.properties");

	private static final String FALLBACK_IP_ADDRESS = "127.0.0.1";

	private static final String HOST_ADDRESS_PROP_NAME = "hostname";

	private MultiuserController muf;

	protected void execute() throws Throwable {
		execute(null);
	}

	public void execute(List<ShutdownHook> hooks) throws Throwable {
		// starting the Multiuser framework
		muf = MUFLifetimeManager.startFramework(
				createModules(), createConfig(), null);
		if (hooks != null) {
			for (ShutdownHook hook : hooks) {
				muf.addShutDownHook(hook);
			}
		}

		// just in case you force the system to close or an unexpected error happen.
		Runtime.getRuntime().addShutdownHook(new Thread("ShutdownThread") {
			public void run() {
				MUFLifetimeManager.stopFramework(muf);
			}
		});

		// you can use a loop like this in order to gracefully shutdown the system.
		String command = "";
		Scanner scanner = new Scanner(System.in);
		System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
		while (!command.equals(SaraCons.SHUTDOWN)) {
			if (scanner.hasNextLine()) {
				command = scanner.nextLine();
				if (command.equals(SaraCons.SHUTDOWN)) {
					MUFLifetimeManager.stopFramework(muf);
				}
				System.err.println("Type " + SaraCons.SHUTDOWN + " to stop:");
			} else {
				Utils.sleep(300);
			}
		}
	}

	protected abstract PluginModule[] createModules();

	protected Config createConfig() throws IOException {
		final String logDir = Utils.getProperty("pathLogs");
		ensureExists(logDir, "Log dir path");
		final Properties connectionProps = loadConnectionProperties();
		// use IP instead of 'localhost'
		final String serverAddress = String.format("tcp://%s", connectionProps.getProperty(HOST_ADDRESS_PROP_NAME));
		return new Config.Builder()
				// you can add values directly like this:
				.setSessionManagerPort(Integer.valueOf(Utils.getProperty("SessionManagerPort")))
				.setDefaultNumOfPoolInstances(10)
				.setNumOfSockets(1)
				// or you can refer to values in your config.properties file:
				.setPathLogs(logDir)
				.setCorePoolSize(1000)
				.setSessionTimeout(5, TimeUnit.DAYS) // dirty workaround for broken close-session
				.setServerAddress(serverAddress)
				.setExceptionTraceLevel(Constants.SHOW_ALL_EXCEPTIONS)
				.setExceptionLogger(getExceptionLogger())// MUF Exceptions/NON_MUF Exceptions
				.build();
	}

	private MessageLog getExceptionLogger() throws IOException {
		MessageLog log = new ExceptionLogger();
		log.setId(String.valueOf(System.currentTimeMillis()));
		// Ensure that the exception log exists
		final String exLogDir = Utils.getProperty("pathExceptionLog");
		ensureExists(exLogDir, "Exception log dir path");
		log.setPath(exLogDir);
		return log;
	}

	private Properties createDefaultConnectionProperties() throws SocketException {
		final Properties result = new Properties();

		final Optional<InetAddress> optPreferredAddr = new PreferredInetAddressFinder().get();
		final String hostname = optPreferredAddr.map(preferredAddr -> {
			final String addr = preferredAddr.getHostAddress();
			Log4J.info(this, String.format("Configuring host IP address as \"%s\".", addr));
			return addr;
		}).orElseGet(() -> {
			final String addr = FALLBACK_IP_ADDRESS;
			Log4J.warn(this, String.format("Could not programmatically find a local IP address; falling back to \"%s\".", addr));
			return addr;
		});
		result.setProperty(HOST_ADDRESS_PROP_NAME, hostname);
		return result;
	}

	private void ensureExists(final String dir, final String desc) throws IOException {
		final Path normalizedAbsPath = Paths.get(dir).toAbsolutePath().normalize();
		final boolean alreadyExists = Files.exists(normalizedAbsPath);
		if (alreadyExists) {
			if (Files.isDirectory(normalizedAbsPath)){
				Log4J.debug(this, String.format("%s \"%s\" already exists; Skipping creation thereof.", desc, normalizedAbsPath));
			} else {
				throw new FileAlreadyExistsException(String.format("%s \"%s\" already exists but is not a directory.", desc, normalizedAbsPath));
			}
		} else {
			final Path absDirPath = Files.createDirectories(normalizedAbsPath);
			Log4J.warn(this, String.format("%s \"%s\" did not exist; Created.", desc, absDirPath));
		}
	}

	private Properties loadConnectionProperties() throws IOException {
		Properties result;
		try (final InputStream is = Files.newInputStream(CONNECTION_PROPS_FILE_PATH)) {
			result = new Properties();
			result.load(is);
		} catch (final NoSuchFileException eFile) {
			Log4J.warn(this, String.format("No connection properties file found at \"%s\"; creating one with defaults.", CONNECTION_PROPS_FILE_PATH));
			result = createDefaultConnectionProperties();
			try (final OutputStream os = Files.newOutputStream(CONNECTION_PROPS_FILE_PATH, StandardOpenOption.CREATE_NEW)) {
				result.store(os, "Programmatically-guessed connection properties; it's possible these don't work!");
			}
		}
		return result;
	}

}
