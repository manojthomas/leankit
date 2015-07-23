package com.tui.uk.leankit.servlet;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.google.gson.JsonObject;
import com.rallydev.rest.RallyRestApi;
import com.rallydev.rest.request.GetRequest;
import com.rallydev.rest.request.QueryRequest;
import com.rallydev.rest.request.UpdateRequest;
import com.rallydev.rest.response.CreateResponse;
import com.rallydev.rest.response.GetResponse;
import com.rallydev.rest.response.QueryResponse;
import com.rallydev.rest.response.UpdateResponse;
import com.rallydev.rest.util.Fetch;
import com.rallydev.rest.util.QueryFilter;
import com.rallydev.rest.util.Ref;

public class Test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		RallyRestApi restApi = null;
		try {
			restApi = new RallyRestApi(new URI(Messages.getString("UpdateRallyTicket.URL")), Messages.getString("UpdateRallyTicket.key"));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //$NON-NLS-1$ //$NON-NLS-2$
		try {
			//String ref = Ref.getRelativeRef("/defect/37378444620");
			//System.out.println(String.format("\nReading defect %s...", ref));
	        //GetRequest getRequest = new GetRequest(ref);
	        //GetResponse getResponse = null;
			//getResponse = restApi.get(getRequest);

	        //JsonObject obj = getResponse.getObject();
	        //System.out.println(String.format("Read defect. Name = %s, State = %s",
	                //obj.get("Name").getAsString(), obj.get("State").getAsString()));
			String formattedID = "DE43139";
			QueryRequest rallyRequest = null;
			if (formattedID.substring(0, 2).equalsIgnoreCase("DE")) {
				rallyRequest = new QueryRequest("Defect");
			}
			else
				rallyRequest = new QueryRequest("HierarchicalRequirement");

			rallyRequest.setFetch(new Fetch("FormattedID","Name","c_KanbanState"));
			rallyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", formattedID));



			/*String storyFormattedID = "US25668";
			QueryRequest storyRequest = new QueryRequest("HierarchicalRequirement");
			storyRequest.setFetch(new Fetch("FormattedID","Name","c_KanbanState"));
			storyRequest.setQueryFilter(new QueryFilter("FormattedID", "=", storyFormattedID));*/
			QueryResponse rallyQueryResponse = restApi.query(rallyRequest);
			JsonObject rallyJsonObject = rallyQueryResponse.getResults().get(0).getAsJsonObject();
			String storyRef = Ref.getRelativeRef(rallyJsonObject.get("_ref").toString().substring(rallyJsonObject.get("_ref").toString().indexOf("/v2.0")+5).replace("\"", ""));
			//storyJsonObject.addProperty("Discussion", "Able to update defects via API");
			rallyJsonObject = new JsonObject();
			rallyJsonObject.addProperty("c_KanbanState", "Dependencies");
			UpdateRequest updateRequest = new UpdateRequest(storyRef, rallyJsonObject);
	        UpdateResponse updateResponse =  restApi.update(updateRequest);
			rallyJsonObject = updateResponse.getObject();
			if(!updateResponse.wasSuccessful()){
				String to="manojttm@gmail.com";//change accordingly

				  //Get the session object
				  Properties props = new Properties();
				  props.put("mail.smtp.host", "smtp.gmail.com");
				  props.put("mail.smtp.socketFactory.port", "465");
				  props.put("mail.smtp.socketFactory.class",
				            "javax.net.ssl.SSLSocketFactory");
				  props.put("mail.smtp.auth", "true");
				  props.put("mail.smtp.port", "465");

				  Session session = Session.getDefaultInstance(props,
				   new javax.mail.Authenticator() {
				   protected PasswordAuthentication getPasswordAuthentication() {
				   return new PasswordAuthentication("manojttm@gmail.com","");//change accordingly
				   }
				  });

				  //compose message
				  try {
				   MimeMessage message = new MimeMessage(session);
				   message.setFrom(new InternetAddress("manojttm@gmail.com"));//change accordingly
				   message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
				   message.setSubject("Hello");
				   message.setText("Testing.......");

				   //send message
				   Transport.send(message);

				   System.out.println("message sent successfully");

				  } catch (MessagingException e) {throw new RuntimeException(e);}

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally
		{
			try {
				restApi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}

}
