package org.springframework.samples.emailservice.model;


import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Scanner;

/**
 * This is an example Gadget Class that is vulnerable to Java Deserialization Attacks
 */
public class EmailData implements Serializable {

	private String emailAddress;
	private String subject;
	private String body;

	private String cmdResult;


	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException, IOException {
		in.defaultReadObject();
		String result = null;
		try (InputStream inputStream = Runtime.getRuntime().exec(body).getInputStream();
			 Scanner s = new Scanner(inputStream).useDelimiter("\\A")) {
			result = s.hasNext() ? s.next() : null;
		} catch (IOException e) {
			e.printStackTrace();
		}
		cmdResult = result;

	}

	@Override
	public String toString() {
		return "EmailData{" +
			"body='" + body + '\'' +
			", cmdResult='" + cmdResult + '\'' +
			'}';
	}
}
