package com.example.esardini.infotainment;

public class GenericResponse {
	private boolean esito;
	private String message;

	public boolean isEsito() {
		return esito;
	}

	public void setEsito(boolean esito) {
		this.esito = esito;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


	@Override
	public String toString() {
		return "GenericResponse [esito=" + esito + ", message=" + message + "]";
	}
	
	
	
}
