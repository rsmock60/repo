package com.smock.rest.model;

import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Account {

	private String accountId;
	private Double amount;

	public Account() {
		super();

		this.accountId = UUID.randomUUID().toString();
		
		// Set initial amount in account to be 100 to allow for transfers
		this.amount = (double) 100;
	}
	
	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getAccountId() {
		return accountId;
	}
}
