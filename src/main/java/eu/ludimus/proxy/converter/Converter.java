package eu.ludimus.proxy.converter;

/**
 * Created by jkkoolen on 07/02/15.
 */
public interface Converter {
    public String convertBeforeForward(String value);
    public String convertBeforeReturn(String value);
}
