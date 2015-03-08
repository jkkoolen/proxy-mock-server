package eu.ludimus.proxy.converter;

/**
 * Created by jkkoolen on 07/02/15.
 */
public interface Converter {
    public byte[] convertBeforeForward(byte[] value);
    public byte[] convertBeforeReturn(byte[] value);
}
