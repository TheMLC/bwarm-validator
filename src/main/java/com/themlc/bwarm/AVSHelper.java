package com.themlc.bwarm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AVSHelper {

	private ArrayList<String> avsValues = new ArrayList<String>();

	private final Logger LOGGER = LogManager.getLogger();

	public AVSHelper(String avsFile) {
		InputStream resource = getClass().getClassLoader().getResourceAsStream(avsFile);

		Scanner s;
		s = new Scanner(resource);
		while (s.hasNext()) {
			avsValues.add(s.next());
		}
		s.close();

		LOGGER.info(avsValues);
	}

	public boolean contains(String value) {
		return avsValues.contains(value);
	}
}
