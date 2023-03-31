package it.gov.pagopa.logextractor.util;


import org.springframework.lang.NonNull;

@FunctionalInterface
public interface IAddComponent {

    boolean addComponent( @NonNull ILogExtractionComponent component );
}
