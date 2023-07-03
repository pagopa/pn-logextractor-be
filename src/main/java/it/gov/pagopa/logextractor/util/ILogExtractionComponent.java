package it.gov.pagopa.logextractor.util;

import java.util.Iterator;
import java.util.List;

public interface ILogExtractionComponent {

    String getArchiveEntryName();

    String getDeduplicationCode();

    Iterator<List<String>> retrieveLogExtractionComponent( IAddComponent recipeView );
}
