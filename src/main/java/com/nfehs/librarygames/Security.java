package com.nfehs.librarygames;

import java.util.Base64;

/**
 * This class handles basic security like encrypting passwords
 * 
 * @author Patrick Kenney, Syed Quadri
 * @date 6/13/2018
 */

public abstract class Security {
	/**
	 * Barely encrypts texts so that it cannot be read immediately
	 * @param text
	 * @return
	 */
	public static String encrypt(String text) {
		return Base64.getEncoder().encodeToString(text.getBytes());
	}
}
