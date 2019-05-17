package com.smock.rest.api;

import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.smock.rest.model.Account;
import com.smock.rest.model.AccountManager;
import com.smock.rest.model.AccountTransfer;
import com.smock.rest.model.AccountType;
import com.smock.rest.model.ErrorCode;
import com.smock.rest.model.ErrorInfo;
import com.smock.rest.model.User;
import com.smock.rest.model.UserManager;

@Path("account")
public class AccountApi {

	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	public Response transferMoney(@FormParam("sourceUserId") String sourceUserId,
			@FormParam("targetUserId") String targetUserId, @FormParam("sourceAccount") AccountType sourceAccount,
			@FormParam("targetAccount") AccountType targetAccount, @FormParam("amount") Double amount) {

		// Check to see if amount is zero
		if (amount == 0 || amount == null) {
			// No amount specified, Transfer can't happen
			ErrorInfo ei = buildError(sourceAccount, amount, null, ErrorCode.INVALID_TRANSFER_AMOUNT);
			Response badRequest = Response.status(Status.BAD_REQUEST).entity(ei).build();
			return badRequest;
		}

		UserManager userMgr = UserManager.getInstance();
		Map<String, User> userMap = userMgr.getUserMap();
		User sourceUser = userMap.get(sourceUserId);
		User targetUser = userMap.get(targetUserId);

		AccountManager accountMgr = AccountManager.getInstance();
		Map<String, Account> map = accountMgr.getAccountMap();
		Account sourceAcctObj = map.get(sourceUser.getAccountMap().get(sourceAccount));
		Account targetAcctObj = map.get(targetUser.getAccountMap().get(targetAccount));

		Double originalSourceAmount = sourceAcctObj.getAmount();
		Double originalTargetAmount = targetAcctObj.getAmount();

		// Check available balance in source
		if (amount > originalSourceAmount) {
			// Transfer can't happen
			ErrorInfo ei = buildError(sourceAccount, amount, null, ErrorCode.INSUFFICIENT_FUNDS);
			Response badRequest = Response.status(Status.BAD_REQUEST).entity(ei).build();
			return badRequest;
		}

		Double newSourceAmount = originalSourceAmount - amount;
		Double newTargetAmount = originalTargetAmount + amount;

		sourceAcctObj.setAmount(newSourceAmount);
		targetAcctObj.setAmount(newTargetAmount);

		AccountTransfer at = new AccountTransfer(originalSourceAmount, originalTargetAmount, newSourceAmount,
				newTargetAmount);

		Response response = Response.ok().entity(at).build();
		return response;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getAccount(@PathParam("id") String id) {

		AccountManager accountMgr = AccountManager.getInstance();
		Map<String, Account> map = accountMgr.getAccountMap();

		if (map.containsKey(id)) {
			Account account = map.get(id);
			Response response = Response.ok().entity(account).build();
			return response;
		} else {
			ErrorInfo ei = buildError(null, null, id, ErrorCode.INVALID_ACCOUNT_ID);
			Response badRequest = Response.status(Status.BAD_REQUEST).entity(ei).build();
			return badRequest;
		}
	}

	/**
	 * Used to build an ErrorInfo object to pass back with the Response.
	 * 
	 * @param sourceAccount
	 * @param amount
	 * @param id
	 * @param code
	 * @return
	 */
	private ErrorInfo buildError(AccountType sourceAccount, Double amount, String id, ErrorCode code) {
		ErrorInfo ei = new ErrorInfo();

		if (ErrorCode.INSUFFICIENT_FUNDS.equals(code)) {
			ei.setCode(ErrorCode.INSUFFICIENT_FUNDS);
			ei.setField("amount");
			StringBuilder sb = new StringBuilder();
			sb.append("Source account of ");
			sb.append(sourceAccount);
			sb.append(" contains fewer funds than requested transfer amount of ");
			sb.append(amount);
			sb.append(".");
			ei.setMessage(sb.toString());
		} else if (ErrorCode.INVALID_TRANSFER_AMOUNT.equals(code)) {
			ei.setCode(code);
			ei.setField("amount");
			StringBuilder sb = new StringBuilder();
			sb.append("Amount specified on input of ");
			sb.append(amount);
			sb.append(" is invalid.");
			ei.setMessage(sb.toString());
		} else if (ErrorCode.INVALID_ACCOUNT_ID.equals(code)) {
			ei.setCode(code);
			ei.setField("ID");
			StringBuilder sb = new StringBuilder();
			sb.append("Account ID of ");
			sb.append(id);
			sb.append(" does not exist.");
			ei.setMessage(sb.toString());
		}

		return ei;
	}
}
