package com.lowagie.text.pdf;
/**
 * Fallback stub to prevent JasperReports 6.21.x NoClassDefFoundError 
 * when running against standard OpenPDF / unpatched iText.
 */

public class FopGlyphProcessor {
	public FopGlyphProcessor() {
    }
	public static boolean isFopSupported() {
        return false;
    }

    public static Object[] getGlyphPositions(String text) {
        return null;
    }

}
