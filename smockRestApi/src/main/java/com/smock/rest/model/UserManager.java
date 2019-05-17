package com.smock.rest.model;

import java.util.HashMap;
import java.util.Map;

public class UserManager {

	private static UserManager userManager;
	private static Map<String, User> userMap;

	private UserManager() {
	} 

	public static UserManager getInstance() {
		if (userManager == null) {
			userManager = new UserManager();
			userMap = new HashMap<String, User>();
		}

		return userManager;
	}
	
	public void addUser(User user) {
		userMap.put(user.getUserId(), user);
	}

	public Map<String, User> getUserMap() {
		return userMap;
	}
}
