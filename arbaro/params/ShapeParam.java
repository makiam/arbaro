/*
 * Created on 24.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.params;

/**
 * @author wdiestel
 *
 */
public final class ShapeParam extends IntParam {

	//Integer [] values;
	final static String[] items = { "conical", "spherical", "hemispherical", "cylindrical", 
			"tapered cylindrical","flame","inverse conical","tend flame","envelope" };
	
	/**
	 * @param nam
	 * @param mn
	 * @param mx
	 * @param def
	 * @param grp
	 * @param lev
	 * @param sh
	 * @param lng
	 */
	public ShapeParam(String nam, int mn, int mx, int def, String grp, int lev,
			int ord, String sh, String lng) {
		super(nam, mn, mx, def, grp, lev, ord, sh, lng);
	}

	public String toString() {
		return items[intValue()];
	}
	
	public static String[] values() {
		return items;
	}
	
}
