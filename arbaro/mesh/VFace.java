/*
 * Created on 28.09.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sourceforge.arbaro.mesh;

import net.sourceforge.arbaro.transformation.Vector;

/**
 * @author wdiestel
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public final class VFace {
	public Vector [] points;
    public VFace(Vector u, Vector v, Vector w) {
	points = new Vector[3];
	points[0]=u;
	points[1]=v;
	points[2]=w;
    }
}
