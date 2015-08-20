package org.ops4j.pax.web.itest.undertow;

import java.util.Dictionary;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.web.itest.base.VersionUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Achim Nierbeck
 */
@RunWith(PaxExam.class)
public class Servlet3WarIntegrationTest extends ITestBase {

	private static final Logger LOG = LoggerFactory.getLogger(Servlet3WarIntegrationTest.class);

	private Bundle installWarBundle;

	@Configuration
	public static Option[] configure() {
		return configureUndertow();
	}

	@Before
	public void setUp() throws BundleException, InterruptedException {
		LOG.info("Setting up test");

		initWebListener();

		String bundlePath = WEB_BUNDLE
				+ "mvn:org.ops4j.pax.web.samples/helloworld-servlet3/"
				+ VersionUtil.getProjectVersion() + "/war?" + WEB_CONTEXT_PATH + "=/war3";
		installWarBundle = bundleContext.installBundle(bundlePath);
		installWarBundle.start();

		waitForWebListener();
	}

	@After
	public void tearDown() throws BundleException {
		if (installWarBundle != null) {
			installWarBundle.stop();
			installWarBundle.uninstall();
		}
	}

	/**
	 * You will get a list of bundles installed by default plus your testcase,
	 * wrapped into a bundle called pax-exam-probe
	 */
	@Test
	public void listBundles() {
		for (Bundle b : bundleContext.getBundles()) {
			if (b.getState() != Bundle.ACTIVE) {
				fail("Bundle should be active: " + b);
			}

			Dictionary<String, String> headers = b.getHeaders();
			String ctxtPath = (String) headers.get(WEB_CONTEXT_PATH);
			if (ctxtPath != null) {
				System.out.println("Bundle " + b.getBundleId() + " : "
						+ b.getSymbolicName() + " : " + ctxtPath);
			} else {
				System.out.println("Bundle " + b.getBundleId() + " : "
						+ b.getSymbolicName());
			}
		}

	}

	@Test
	public void testWC() throws Exception {

		testClient.testWebPath("http://127.0.0.1:8181/war3/hello", "<h1>Hello World</h1>");

	}

	@Test
	public void testFilterInit() throws Exception {
		testClient.testWebPath("http://127.0.0.1:8181/war3/hello/filter", "Have bundle context in filter: true");
	}
	
	@Test
	public void testDuplicateDefinitionServlet() throws Exception {
		testClient.testWebPath("http://127.0.0.1:8181/war3/duplicate", "<h1>Duplicate Servlet</h1>");
	}
	
	@Test
	public void testMimeImage() throws Exception {
		testWC();

		HttpResponse httpResponse = testClient.getHttpResponse(
				"http://127.0.0.1:8181/war3/images/logo.png", false, null);
		Header header = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
		assertEquals("image/png", header.getValue());
	}

	@Test
	public void testMimeStyle() throws Exception {
		testWC();

		HttpResponse httpResponse = testClient.getHttpResponse(
				"http://127.0.0.1:8181/war3/css/content.css", false, null);
		Header header = httpResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE);
		assertEquals("text/css", header.getValue());
	}
	
	@Test
	public void testWrongServlet() throws Exception {
		testClient.testWebPath("http://127.0.0.1:8181/war3/wrong/", "<h1>Error Page</h1>", 404, false);
	}
}