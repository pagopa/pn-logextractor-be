package it.gov.pagopa.logextractor.util.constant;

/**
 * Utility class to list the backend response constants
 */
public class ResponseConstants {
  private ResponseConstants() {
  }

  public static final String GENERIC_INTERNAL_SERVER_ERROR_MESSAGE =
      "Errore nell'elaborazione della richiesta";
  public static final String GENERIC_INTERNAL_SERVER_ERROR_ENGLISH_MESSAGE =
      "An error occurred during request elaboration";
  public static final String GENERIC_BAD_REQUEST_ERROR_MESSAGE = "Informazioni non valide";

  public static final String GENERIC_BAD_REQUEST_ERROR_ENGLISH_MESSAGE = "Informations not valid";
  public static final String OPERATION_CANNOT_BE_COMPLETED_MESSAGE =
      "L'operazione non puo' essere ancora completata, ritentare tra ";
  public static final String SUCCESS_RESPONSE_MESSAGE = "Operazione completata con successo";
  public static final String NO_NOTIFICATION_FOUND_MESSAGE =
      "Nessuna notifica trovata per i dati inseriti";
  public static final String NO_DOCUMENT_FOUND_MESSAGE =
      "Nessun documento trovato per i dati inseriti";

}
