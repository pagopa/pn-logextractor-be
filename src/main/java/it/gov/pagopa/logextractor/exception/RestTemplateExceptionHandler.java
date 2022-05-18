package it.gov.pagopa.logextractor.exception;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus.Series;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import it.gov.pagopa.logextractor.dto.response.ApiError;

public class RestTemplateExceptionHandler implements ResponseErrorHandler {

	@Override
    public boolean hasError(ClientHttpResponse httpResponse) throws IOException {
		return (httpResponse.getStatusCode().series() == Series.CLIENT_ERROR 
				|| httpResponse.getStatusCode().series() == Series.SERVER_ERROR);
    }

    @Override
    public void handleError(ClientHttpResponse httpResponse) throws IOException {
        if (httpResponse.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
        	System.out.println("Errore 500");
        	throw new IOException("Errore 500");
        } else if (httpResponse.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
        	System.out.println("Errore 400");
        	throw new IOException("Errore 400");
        }
    }
}
