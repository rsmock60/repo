package com.smock.rest.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class AccountTransfer {
	private Double originalSourceAmount;
	private Double originalTargetAmount;
	private Double newSourceAmount;
	private Double newTargetAmount;
	
    public AccountTransfer() {
        super();
    }
    
	public AccountTransfer(Double originalSourceAmount, Double originalTargetAmount, Double newSourceAmount,
			Double newTargetAmount) {
		super();
		this.originalSourceAmount = originalSourceAmount;
		this.originalTargetAmount = originalTargetAmount;
		this.newSourceAmount = newSourceAmount;
		this.newTargetAmount = newTargetAmount;
	}
	
	public Double getOriginalSourceAmount() {
		return originalSourceAmount;
	}
	public void setOriginalSourceAmount(Double originalSourceAmount) {
		this.originalSourceAmount = originalSourceAmount;
	}
	public Double getOriginalTargetAmount() {
		return originalTargetAmount;
	}
	public void setOriginalTargetAmount(Double originalTargetAmount) {
		this.originalTargetAmount = originalTargetAmount;
	}
	public Double getNewSourceAmount() {
		return newSourceAmount;
	}
	public void setNewSourceAmount(Double newSourceAmount) {
		this.newSourceAmount = newSourceAmount;
	}
	public Double getNewTargetAmount() {
		return newTargetAmount;
	}
	public void setNewTargetAmount(Double newTargetAmount) {
		this.newTargetAmount = newTargetAmount;
	}
	
	

}
