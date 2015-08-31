package com.tui.uk.leankit.servlet;

import java.io.FileInputStream;
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
		final Properties props = new Properties();
		try {
			props.load(new FileInputStream("messages.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			restApi = new RallyRestApi(new URI(
					props.getProperty("UpdateRallyTicket.URL")),
					props.getProperty("UpdateRallyTicket.key"));

			String formattedID = "US999999";
			JsonObject rallyJsonObject = null;
			UpdateRequest updateRequest = null;
			UpdateResponse updateResponse = null;
			String errorCode = "";
			QueryRequest rallyRequest = null;
			String columnTo = null;
			if (formattedID != null) {
				if (formattedID.substring(0, 2).equalsIgnoreCase("DE")) {
					rallyRequest = new QueryRequest("Defect");
				} else {
					rallyRequest = new QueryRequest("HierarchicalRequirement");
				}

				rallyRequest.setFetch(new Fetch("FormattedID", "Name",
						"ScheduleState"));
				rallyRequest.setQueryFilter(new QueryFilter("FormattedID", "=",
						formattedID));

				QueryResponse rallyQueryResponse = restApi.query(rallyRequest);

				if (rallyQueryResponse.getResults().size() > 0) {
					rallyJsonObject = rallyQueryResponse.getResults().get(0)
							.getAsJsonObject();
					String rallyRef = Ref.getRelativeRef(rallyJsonObject
							.get("_ref")
							.toString()
							.substring(
									rallyJsonObject.get("_ref").toString()
											.indexOf("/v2.0") + 5)
							.replace("\"", ""));

					rallyJsonObject = new JsonObject();
					columnTo = props.getProperty("UpdateRallyTicket.218757890");
					if (columnTo != null) {
						rallyJsonObject.addProperty("ScheduleState", columnTo);
						updateRequest = new UpdateRequest(rallyRef,
								rallyJsonObject);
						updateResponse = restApi.update(updateRequest);
						rallyJsonObject = updateResponse.getObject();
					} else {
						errorCode = errorCode
								.concat("Column mapping not available");
					}
				} else {
					errorCode = errorCode.concat("Invalid Rally ID");
				}
				if (null!=updateResponse && !updateResponse.wasSuccessful()) {
					errorCode = errorCode.concat(updateResponse.getErrors()
							.toString());
				}
				if (!errorCode.isEmpty() && errorCode != null) {
					String to = props
							.getProperty("UpdateRallyTicket.recipientList");

					// Get the session object

					props.put("mail.smtp.host", "smtp.gmail.com");
					props.put("mail.smtp.socketFactory.port", "465");
					props.put("mail.smtp.socketFactory.class",
							"javax.net.ssl.SSLSocketFactory");
					props.put("mail.smtp.auth", "true");
					props.put("mail.smtp.port", "465");

					Session session = Session.getDefaultInstance(props,
							new javax.mail.Authenticator() {
								protected PasswordAuthentication getPasswordAuthentication() {
									return new PasswordAuthentication(
											props.getProperty("UpdateRallyTicket.mailFrom"),
											props.getProperty("UpdateRallyTicket.mailFromPassword"));
								}
							});

					// compose message
					try {
						MimeMessage message = new MimeMessage(session);
						message.setFrom(new InternetAddress(props
								.getProperty("UpdateRallyTicket.mailFrom")));
						message.addRecipient(Message.RecipientType.TO,
								new InternetAddress(to));
						message.setSubject("TEST MAIL PLZ IGNORE Leankit 2 Rally Update FAILED");

						message.setText(String
								.format("Failed ticket = %s, while trying to update %s to %s ",
										formattedID,
										"ScheduleState",
										props.getProperty("UpdateRallyTicket.218757890"))
								.concat(String.format("\n\r"))
								.concat(String.format(
										"Logged error message is : %s",
										errorCode)));

						// send message
						Transport.send(message);

						// System.out.println("message sent successfully");

					} catch (MessagingException e) {
						throw new RuntimeException(e);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				restApi.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
