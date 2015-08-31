<%@page import="java.io.FileInputStream"%>
<%@page import="javax.mail.Message"%>
<%@page import="javax.mail.internet.InternetAddress"%>
<%@page import="com.rallydev.rest.util.Ref"%>
<%@page import="com.rallydev.rest.util.QueryFilter"%>
<%@page import="com.rallydev.rest.util.Fetch"%>
<%@page import="java.net.URI"%>
<%@page import="java.io.IOException"%>
<%@page import="javax.mail.MessagingException"%>
<%@page import="javax.mail.Transport"%>
<%@page import="javax.mail.internet.MimeMessage"%>
<%@page import="javax.mail.PasswordAuthentication"%>
<%@page import="javax.mail.Session"%>
<%@page import="java.util.Properties"%>
<%@page import="com.rallydev.rest.response.UpdateResponse"%>
<%@page import="com.rallydev.rest.request.UpdateRequest"%>
<%@page import="com.google.gson.JsonObject"%>
<%@page import="com.rallydev.rest.response.QueryResponse"%>
<%@page import="com.rallydev.rest.request.QueryRequest"%>
<%@page import="com.rallydev.rest.RallyRestApi"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
	<%
		if (request.getHeader("Referer") == null
				|| (request.getHeader("Referer").toString().toLowerCase()
						.indexOf("zapier.com")) < 0) {
			response.sendError(403, "Unauthorized");
		} else {
			RallyRestApi restApi = null;

			try {
				final Properties props = new Properties();
				props.load(new FileInputStream(
						"/var/lib/openshift/55b2118d5973ca1a2f000023/app-root/runtime/dependencies/jbossews/webapps/messages.properties"));
				restApi = new RallyRestApi(new URI(
						props.getProperty("UpdateRallyTicket.URL")),
						props.getProperty("UpdateRallyTicket.key"));

				String formattedID = request.getParameter("ticketID");
				System.out.println("formattedID_" + formattedID);
				QueryRequest rallyRequest = null;
				JsonObject rallyJsonObject = null;
				UpdateRequest updateRequest = null;
				UpdateResponse updateResponse = null;
				String errorCode = "";
				String columnTo = null;
				if (formattedID != null) {
					if (formattedID.substring(0, 2).equalsIgnoreCase("DE")) {
						rallyRequest = new QueryRequest("Defect");
					} else {
						rallyRequest = new QueryRequest(
								"HierarchicalRequirement");
					}
					System.out.println("fieldName_"
							+ request.getParameter("fieldName"));
					System.out.println("columnTo_"
							+ request.getParameter("columnTo"));
					rallyRequest.setFetch(new Fetch("FormattedID", "Name",
							request.getParameter("fieldName")));
					rallyRequest.setQueryFilter(new QueryFilter(
							"FormattedID", "=", formattedID));

					QueryResponse rallyQueryResponse = restApi
							.query(rallyRequest);
					if (rallyQueryResponse.getResults().size() > 0) {
						rallyJsonObject = rallyQueryResponse.getResults()
								.get(0).getAsJsonObject();
						String rallyRef = Ref
								.getRelativeRef(rallyJsonObject
										.get("_ref")
										.toString()
										.substring(
												rallyJsonObject.get("_ref")
														.toString()
														.indexOf("/v2.0") + 5)
										.replace("\"", ""));
						System.out.println("rallyRef_" + rallyRef);
						rallyJsonObject = new JsonObject();
						columnTo = props.getProperty("UpdateRallyTicket."
								+ request.getParameter("columnTo"));
						if (columnTo != null) {
							rallyJsonObject.addProperty(
									request.getParameter("fieldName"),
									columnTo);
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
					if (null != updateResponse
							&& !updateResponse.wasSuccessful()) {
						errorCode = errorCode.concat(updateResponse
								.getErrors().toString());
					}
					if (!errorCode.isEmpty() && errorCode != null) {
	%>Update Failed<%
		String to = props
								.getProperty("UpdateRallyTicket.recipientList");

						// Get the session object
						props.put("mail.smtp.host", "smtp.gmail.com");
						props.put("mail.smtp.socketFactory.port", "465");
						props.put("mail.smtp.socketFactory.class",
								"javax.net.ssl.SSLSocketFactory");
						props.put("mail.smtp.auth", "true");
						props.put("mail.smtp.port", "465");

						Session session1 = Session.getDefaultInstance(
								props, new javax.mail.Authenticator() {
									protected PasswordAuthentication getPasswordAuthentication() {
										return new PasswordAuthentication(
												props.getProperty("UpdateRallyTicket.mailFrom"),
												props.getProperty("UpdateRallyTicket.mailFromPassword"));
									}
								});

						// compose message
						try {
							MimeMessage message = new MimeMessage(session1);
							message.setFrom(new InternetAddress(
									props.getProperty("UpdateRallyTicket.mailFrom")));
							message.addRecipient(Message.RecipientType.TO,
									new InternetAddress(to));
							message.setSubject("Leankit 2 Rally Update FAILED");
							message.setText(String
									.format("Failed ticket = %s, while trying to update %s to %s ",
											formattedID,
											request.getParameter("fieldName"),
											props.getProperty("UpdateRallyTicket."
													+ request
															.getParameter("columnTo")))
									.concat(String.format("\n\r"))
									.concat(String.format(
											"Logged error message is : %s",
											errorCode)));

							// send message
							Transport.send(message);

						} catch (MessagingException e) {
							throw new RuntimeException(e);
						}

					} else {
	%>Update Success<%
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
	%>

</body>
</html>