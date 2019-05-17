package com.smock.rest.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class User {

	private String userId;
	private String firstName;
	private String lastName;
	private String email;
	private String phone;

	private Map<AccountType, String> accountMap;

    public User() {
        super();
    }
    
	public User(String firstName, String lastName, String email, String phone) {
		super();

		this.userId = UUID.randomUUID().toString();
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.phone = phone;
		
		// Initialize accounts on creation of user
		Account checkingAccount = new Account();
		Account savingsAccount = new Account();
		Account moneyManagerAccount = new Account();
		this.accountMap = new HashMap<AccountType, String>();
		this.accountMap.put(AccountType.CHECKING, checkingAccount.getAccountId());
		this.accountMap.put(AccountType.SAVINGS, savingsAccount.getAccountId());
		this.accountMap.put(AccountType.MONEY_MGR, moneyManagerAccount.getAccountId()); 
		
		// Add accounts to AccountManger
		AccountManager accountManager = AccountManager.getInstance();
		accountManager.addAccount(checkingAccount);
		accountManager.addAccount(savingsAccount);
		accountManager.addAccount(moneyManagerAccount);
	}

	public void setUserId(String id) {
		this.userId = id;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public void setAccountMap(Map<AccountType, String> accountMap) {
		this.accountMap = accountMap;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getFirstName() {
		return firstName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getLastName() {
		return lastName;
	}
	
	public Map<AccountType, String> getAccountMap() {
		return accountMap;
	}
}
