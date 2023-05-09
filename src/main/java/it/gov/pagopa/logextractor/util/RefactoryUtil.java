package it.gov.pagopa.logextractor.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefactoryUtil {

	//TODO: remove me!
	public static List<String> streamToList(ByteArrayOutputStream baos){
		ArrayList<String> values = new ArrayList<>();
		BufferedReader bufferedReader = new BufferedReader(new StringReader(new String(baos.toByteArray())));
		String line;
        try {
			while ((line = bufferedReader.readLine()) != null){
			    values.add(line);
			}
			bufferedReader.close();
			baos.close();
		} catch (IOException e) {
			log.error("Error reading OpenSearch Response to List<String>", e);
		}
        return values;
	}
}
