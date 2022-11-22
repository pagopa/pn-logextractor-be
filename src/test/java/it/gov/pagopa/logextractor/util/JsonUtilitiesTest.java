package it.gov.pagopa.logextractor.util;

import it.gov.pagopa.logextractor.util.JsonUtilities;
import it.gov.pagopa.logextractor.util.external.pnservices.NotificationDownloadFileData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class JsonUtilitiesTest {

    JsonUtilities jsonUtilities;

    @BeforeEach
    void init() {
        jsonUtilities = new JsonUtilities();
    }

    @Test
    @DisplayName("Convert JSON array of objects to string")
    void testToString_whenProvidedJsonArray_returnsJsonString() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("test", "test");
        JSONArray inputArray = new JSONArray();
        inputArray.put(object);

        String convertedJson = jsonUtilities.toString(inputArray);

        Assertions.assertNotNull(convertedJson, () -> "Converted string shouldn't be null");
    }

    @Test
    @DisplayName("Convert NotificationDownloadFileData object into JSON object")
    void testToJson_whenProvidedNotificationDownloadFileData_returnsJsonObject() throws JSONException {
        NotificationDownloadFileData fileData = new NotificationDownloadFileData("test", "test", "test");

        JSONObject convertedJson = jsonUtilities.toJson(fileData);

        Assertions.assertNotNull(convertedJson, () -> "Converted object shouldn't be null");
    }

    @Test
    @DisplayName("Convert NotificationDownloadFileData object list into JSON object")
    void testToJson_whenProvidedNotificationDownloadFileDataList_returnsJsonObject() throws JSONException {
        List<NotificationDownloadFileData> downloadFileData = List.of(
                new NotificationDownloadFileData("test", "test", "test"),
                new NotificationDownloadFileData("test1", "test1", "test1")
        );

        JSONArray convertedJson = jsonUtilities.toJson(downloadFileData);

        Assertions.assertNotNull(convertedJson, () -> "Converted object shouldn't be null");
    }
}
