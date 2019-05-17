package com.smock.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.smock.rest.api.AccountApi;
import com.smock.rest.api.UserApi;
import com.smock.rest.model.Account;
import com.smock.rest.model.AccountTransfer;
import com.smock.rest.model.AccountType;
import com.smock.rest.model.ErrorCode;
import com.smock.rest.model.ErrorInfo;
import com.smock.rest.model.User;
import com.smock.rest.util.InMemoryRestServer;

public class MyUserAccountTests {

	// APIs
	public static UserApi userApi = new UserApi();
	public static AccountApi accountApi = new AccountApi();
	public static InMemoryRestServer userSvr;
	public static InMemoryRestServer acctSvr;

	// User One fields
	public static Form userOne_form;
	public static String userOne_firstName = "Donald";
	public static String userOne_lastName = "Duck";
	public static String userOne_email = "donaldduck@test.com";
	public static String userOne_phone = "555-555-1212";

	// User Two fields
	public static Form userTwo_form;
	public static String userTwo_firstName = "Daffy";
	public static String userTwo_lastName = "Duck";
	public static String userTwo_email = "daffyduck@test.com";
	public static String userTwo_phone = "777-777-1212";

	// Account fields
	public static Double expectedBalance = new Double(100);

	@BeforeClass
	public static void beforeClass() throws Exception {
		userSvr = InMemoryRestServer.create(userApi);
		acctSvr = InMemoryRestServer.create(accountApi);

		userOne_form = new Form();
		userOne_form.param("firstName", userOne_firstName);
		userOne_form.param("lastName", userOne_lastName);
		userOne_form.param("email", userOne_email);
		userOne_form.param("phone", userOne_phone);
		
		userTwo_form = new Form();
		userTwo_form.param("firstName", userTwo_firstName);
		userTwo_form.param("lastName", userTwo_lastName);
		userTwo_form.param("email", userTwo_email);
		userTwo_form.param("phone", userTwo_phone);
	}

	@AfterClass
	public static void afterClass() throws Exception {
		userSvr.close();
		acctSvr.close();
	}

	/**
	 * Invokes a POST REST call to create a user. Validates that the REST response returns a 200 and that all of the
	 * user fields have been set properly. It then invokes a GET REST call on the user accounts and validates the
	 * response as well as the account balances. All account balances start off at 100.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testUserCreate() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());

		// Get user from Response & validate values
		User theUser = userCreateResponse.readEntity(User.class);
		assertEquals(userOne_firstName, theUser.getFirstName());
		assertEquals(userOne_lastName, theUser.getLastName());
		assertEquals(userOne_email, theUser.getEmail());
		assertEquals(userOne_phone, theUser.getPhone());
		assertNotNull(theUser.getUserId());
		assertNotNull(theUser.getAccountMap());

		// Obtain account IDs
		String checkingAccountId = theUser.getAccountMap().get(AccountType.CHECKING);
		String savingsAccountId = theUser.getAccountMap().get(AccountType.SAVINGS);
		String moneyMgrId = theUser.getAccountMap().get(AccountType.MONEY_MGR);

		// GET accounts
		Response checkingAcct = acctSvr.newRequest("/account/" + checkingAccountId).request().buildGet().invoke();
		acctSvr = InMemoryRestServer.create(accountApi);
		Response savingsAcct = acctSvr.newRequest("/account/" + savingsAccountId).request().buildGet().invoke();
		acctSvr = InMemoryRestServer.create(accountApi);
		Response moneyMgrAcct = acctSvr.newRequest("/account/" + moneyMgrId).request().buildGet().invoke();

		// Validate responses and balances
		assertEquals(Response.Status.OK.getStatusCode(), checkingAcct.getStatus());
		assertEquals(Response.Status.OK.getStatusCode(), savingsAcct.getStatus());
		assertEquals(Response.Status.OK.getStatusCode(), moneyMgrAcct.getStatus());
		Account checkingAccount = checkingAcct.readEntity(Account.class);
		Account savingsAccount = savingsAcct.readEntity(Account.class);
		Account moneyMgrAccount = moneyMgrAcct.readEntity(Account.class);
		assertEquals(expectedBalance, checkingAccount.getAmount());
		assertEquals(expectedBalance, savingsAccount.getAmount());
		assertEquals(expectedBalance, moneyMgrAccount.getAmount());
	}

	/**
	 * Invokes a POST REST call to create a user. Invokes a POST REST call to transfer money from one account to another
	 * account. In this test, the entire balance is moved from one account to another account.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testEntireBalanceTransferSameUser() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());

		// Obtain user & account IDs from Response
		User theUser = userCreateResponse.readEntity(User.class);
		String userId = theUser.getUserId();
		String checkingAccountId = theUser.getAccountMap().get(AccountType.CHECKING);
		String savingsAccountId = theUser.getAccountMap().get(AccountType.SAVINGS);

		// Build transfer form that transfers entire balance from checking to savings
		Form transferForm = new Form();
		transferForm.param("sourceUserId", userId);
		transferForm.param("targetUserId", userId);
		transferForm.param("sourceAccount", AccountType.CHECKING.toString());
		transferForm.param("targetAccount", AccountType.SAVINGS.toString());
		transferForm.param("amount", "100");

		// POST transfer of money
		Response transfer = acctSvr.newRequest("/account").request().buildPost(Entity.form(transferForm)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), transfer.getStatus());

		// Validate field values returned on transfer object
		AccountTransfer at = transfer.readEntity(AccountTransfer.class);
		assertEquals(expectedBalance, at.getOriginalSourceAmount());
		assertEquals(expectedBalance, at.getOriginalTargetAmount());
		assertEquals(new Double(0), at.getNewSourceAmount());
		assertEquals(new Double(200), at.getNewTargetAmount());

		// GET checking and savings accounts
		Response checkingAcct = acctSvr.newRequest("/account/" + checkingAccountId).request().buildGet().invoke();
		acctSvr = InMemoryRestServer.create(accountApi);
		Response savingsAcct = acctSvr.newRequest("/account/" + savingsAccountId).request().buildGet().invoke();
		assertEquals(Response.Status.OK.getStatusCode(), checkingAcct.getStatus());
		assertEquals(Response.Status.OK.getStatusCode(), savingsAcct.getStatus());

		// Validate actual balances on checking and savings account
		Account checking = checkingAcct.readEntity(Account.class);
		Account savings = savingsAcct.readEntity(Account.class);
		assertEquals(at.getNewSourceAmount(), checking.getAmount());
		assertEquals(at.getNewTargetAmount(), savings.getAmount());
	}
	
	@Test
	public void testEntireBalanceTransferAcrossUsers() throws Exception {

		// POST source user
		Response sourceUserResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), sourceUserResponse.getStatus());
		userSvr = InMemoryRestServer.create(userApi);
		
		// POST target user
		Response targetUserResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userTwo_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), targetUserResponse.getStatus());
		
		// Obtain user & account IDs from Response
		User sourceUser = sourceUserResponse.readEntity(User.class);
		String sourceUserId = sourceUser.getUserId();
		String checkingAccountId = sourceUser.getAccountMap().get(AccountType.CHECKING);
		
		User targetUser = targetUserResponse.readEntity(User.class);
		String targetUserId = targetUser.getUserId();
		String savingsAccountId = targetUser.getAccountMap().get(AccountType.SAVINGS);

		// Build transfer form that transfers entire balance from checking to savings
		Form transferForm = new Form();
		transferForm.param("sourceUserId", sourceUserId);
		transferForm.param("targetUserId", targetUserId);
		transferForm.param("sourceAccount", AccountType.CHECKING.toString());
		transferForm.param("targetAccount", AccountType.SAVINGS.toString());
		transferForm.param("amount", "100");

		// POST transfer of money
		Response transfer = acctSvr.newRequest("/account").request().buildPost(Entity.form(transferForm)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), transfer.getStatus());

		// Validate field values returned on transfer object
		AccountTransfer at = transfer.readEntity(AccountTransfer.class);
		assertEquals(expectedBalance, at.getOriginalSourceAmount());
		assertEquals(expectedBalance, at.getOriginalTargetAmount());
		assertEquals(new Double(0), at.getNewSourceAmount());
		assertEquals(new Double(200), at.getNewTargetAmount());

		// GET checking and savings accounts
		Response checkingAcct = acctSvr.newRequest("/account/" + checkingAccountId).request().buildGet().invoke();
		acctSvr = InMemoryRestServer.create(accountApi);
		Response savingsAcct = acctSvr.newRequest("/account/" + savingsAccountId).request().buildGet().invoke();
		assertEquals(Response.Status.OK.getStatusCode(), checkingAcct.getStatus());
		assertEquals(Response.Status.OK.getStatusCode(), savingsAcct.getStatus());

		// Validate actual balances on checking and savings account
		Account checking = checkingAcct.readEntity(Account.class);
		Account savings = savingsAcct.readEntity(Account.class);
		assertEquals(at.getNewSourceAmount(), checking.getAmount());
		assertEquals(at.getNewTargetAmount(), savings.getAmount());
	}

	/**
	 * Invokes a POST REST call to create a user. Invokes a POST REST call to transfer money from one account to another
	 * account. In this test, the balance in the source account is less than the amount being transferred.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInsufficientFundsTransfer() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());

		// Obtain user from Response
		User theUser = userCreateResponse.readEntity(User.class);
		String userId = theUser.getUserId();

		// Build transfer form that attempts to transfer too much money
		Form transferForm = new Form();
		transferForm.param("sourceUserId", userId);
		transferForm.param("targetUserId", userId);
		transferForm.param("sourceAccount", AccountType.CHECKING.toString());
		transferForm.param("targetAccount", AccountType.SAVINGS.toString());
		transferForm.param("amount", "101");

		// POST transfer of money
		Response transfer = acctSvr.newRequest("/account").request().buildPost(Entity.form(transferForm)).invoke();
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), transfer.getStatus());

		// Validate field values returned on transfer object
		ErrorInfo ei = transfer.readEntity(ErrorInfo.class);
		assertEquals(ErrorCode.INSUFFICIENT_FUNDS, ei.getCode());
		assertEquals("amount", ei.getField());
		assertNotNull(ei.getMessage());
	}
	
	/**
	 * Invokes a POST REST call to create a user. Invokes a POST REST call to transfer money from one account to another
	 * account. In this test, the API should return a failure since there was no amount being transferred.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testZeroAmountTransfer() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());

		// Obtain user & account IDs from Response
		User theUser = userCreateResponse.readEntity(User.class);

		// Build transfer form that transfers entire balance from checking to savings
		Form transferForm = new Form();
		transferForm.param("userId", theUser.getUserId());
		transferForm.param("sourceAccount", AccountType.CHECKING.toString());
		transferForm.param("targetAccount", AccountType.SAVINGS.toString());
		transferForm.param("amount", "0");

		// POST transfer of money
		Response transfer = acctSvr.newRequest("/account").request().buildPost(Entity.form(transferForm)).invoke();
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), transfer.getStatus());

		// Validate field values returned on error info object
		ErrorInfo ei = transfer.readEntity(ErrorInfo.class);
		assertEquals(ErrorCode.INVALID_TRANSFER_AMOUNT, ei.getCode());
		assertEquals("amount", ei.getField());
		assertNotNull(ei.getMessage());
	}
	
	/**
	 * Tests the case when an invalid account ID is passed into the GET request.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidAccountId() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());

		// Obtain user from Response
		User theUser = userCreateResponse.readEntity(User.class);
		String invalidCheckingAccountId = theUser.getAccountMap().get(AccountType.CHECKING) + "1";
		
		// GET invalid account
		Response checkingAcct = acctSvr.newRequest("/account/" + invalidCheckingAccountId).request().buildGet().invoke();
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), checkingAcct.getStatus());

		// Validate field values returned on error info object
		ErrorInfo ei = checkingAcct.readEntity(ErrorInfo.class);
		assertEquals(ErrorCode.INVALID_ACCOUNT_ID, ei.getCode());
		assertEquals("ID", ei.getField());
		assertNotNull(ei.getMessage());
	}
	
	/**
	 * Tests the case when an invalid user ID is passed into the GET request.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInvalidUserId() throws Exception {

		// POST user
		Response userCreateResponse = userSvr.newRequest("/user").request().buildPost(Entity.form(userOne_form)).invoke();
		assertEquals(Response.Status.OK.getStatusCode(), userCreateResponse.getStatus());
		userSvr = InMemoryRestServer.create(userApi);

		// Obtain user from Response
		User theUser = userCreateResponse.readEntity(User.class);
		String invalidUserId = theUser.getUserId() + "1";
		
		// GET invalid user
		Response invalidUser = userSvr.newRequest("/user/" + invalidUserId).request().buildGet().invoke();
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), invalidUser.getStatus());

		// Validate field values returned on error info object
		ErrorInfo ei = invalidUser.readEntity(ErrorInfo.class);
		assertEquals(ErrorCode.INVALID_USER_ID, ei.getCode());
		assertEquals("ID", ei.getField());
		assertNotNull(ei.getMessage());
	}
}
