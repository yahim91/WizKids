package tests;

import static org.junit.Assert.*;

import org.junit.Test;

import app.Mediator;

public class TestApp {

	@Test
	public void testPortConfig() {
		Mediator med = new Mediator();
		med.getConfig().readConfigFile();
		assertTrue(9999 == med.getConfig().getPort());
	}

	@Test
	public void testUsernameConfig() {
		Mediator med = new Mediator();
		med.getConfig().readConfigFile();
		assertTrue("wizkid".equals(med.getConfig().getUsername()));
	}
	
	@Test
	public void testAddressConfig() {
		Mediator med = new Mediator();
		med.getConfig().readConfigFile();
		assertTrue("192.168.0.1".equals(med.getConfig().getAddress()));
	}
}
