package com.smock.rest.model;

import java.util.HashMap;
import java.util.Map;

public class AccountManager {

	private static AccountManager accountManager;
	private static Map<String, Account> accountMap;

	private AccountManager() {
	} 

	public static AccountManager getInstance() {
		if (accountManager == null) {
			accountManager = new AccountManager();
			accountMap = new HashMap<String, Account>();
		}

		return accountManager;
	}
	
	public void addAccount(Account account) {
		accountMap.put(account.getAccountId(), account);
	}

	public Map<String, Account> getAccountMap() {
		return accountMap;
	}
}
