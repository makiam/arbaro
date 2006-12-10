package net.sourceforge.arbaro.export;

public class InvalidExportFormatError extends Exception {
    public InvalidExportFormatError(String msg) {
	super(msg);
    }
};