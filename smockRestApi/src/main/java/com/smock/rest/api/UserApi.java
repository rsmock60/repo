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

import com.smock.rest.model.ErrorCode;
import com.smock.rest.model.ErrorInfo;
import com.smock.rest.model.User;
import com.smock.rest.model.UserManager;

@Path("user")
public class UserApi {
	
	@POST
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_XML)
	public Response createUser(@FormParam("firstName") String firstName, @FormParam("lastName") String lastName, @FormParam("email") String email, @FormParam("phone") String phone) {

		UserManager mgr = UserManager.getInstance();
		User theUser = new User(firstName, lastName, email, phone);
		mgr.addUser(theUser);
		
		Response response = Response.ok().entity(theUser).build();
		return response;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_XML)
	public Response getUser(@PathParam("id") String id) {

		UserManager userMgr = UserManager.getInstance();
		Map<String, User> map = userMgr.getUserMap();
		
		if (map.containsKey(id)) {
			User user = map.get(id);	
			Response response = Response.ok().entity(user).build();
			return response;
		} else {
			ErrorInfo ei = buildError(id, ErrorCode.INVALID_USER_ID);
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
	private ErrorInfo buildError(String id, ErrorCode code) {
		ErrorInfo ei = new ErrorInfo();
		
		if (ErrorCode.INVALID_USER_ID.equals(code)) {
			ei.setCode(code);
			ei.setField("ID");
			StringBuilder sb = new StringBuilder();
			sb.append("User ID of ");
			sb.append(id);
			sb.append(" does not exist.");
			ei.setMessage(sb.toString());
		}
		
		return ei;
	}
}
