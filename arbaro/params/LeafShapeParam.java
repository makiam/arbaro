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
public final class LeafShapeParam extends StringParam {
	
	final static String[] items = {"disc","disc1","disc2","disc3","disc4","disc5","disc6","disc7","disc8","disc9","sphere"}; 

	/**
	 * @param nam
	 * @param def
	 * @param grp
	 * @param lev
	 * @param sh
	 * @param lng
	 */
	public LeafShapeParam(String nam, String def, String grp, int lev,
			int ord, String sh, String lng) {
		super(nam, def, grp, lev, ord, sh, lng);
	}
	
	public static String[] values() {
		return items;
	}

}
