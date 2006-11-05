/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.text.NumberFormat;
import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.*;

/**
 * @author wolfram
 *
 */
public class DXFFaceExporter extends DefaultTreeTraversal {
	LeafMesh leafMesh;
	VFace vFace;
	String layer;
	PrintWriter w;
	Progress progress;
	Tree tree;
	long leavesProgressCount=0;
	
	NumberFormat frm = FloatFormat.getInstance();

	/**
	 * 
	 */
	public DXFFaceExporter(PrintWriter pw, String layer) {
		super();
		this.layer = layer;
		this.w = pw;
		vFace = new VFace(new Vector(),new Vector(),new Vector());
	}
	
	public boolean enterTree(Tree tree) {
		this.tree = tree;
		this.leafMesh = tree.createLeafMesh(false);
		this.progress = tree.getProgress();
		return true;
	}

	public boolean visitLeaf(Leaf l) {
		for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {

			Face face = leafMesh.shapeFaceAt(i);
			for (int k=0; k<3; k++) {
				vFace.points[k] = l.transf.apply(
						leafMesh.shapeVertexAt((int)face.points[k]).point);
			}
			
			writeFace(vFace,layer);
		}
		
		incLeavesProgressCount();
		
		return true;
	}
	
	private void writeFace(VFace face,String layer) {
		// FIXME: maybe could be faster when putting
		// all into one string and then send to stream?
		wg(0,"3DFACE");
		wg(8,layer);
		wg(62,layer); // different colors for layer 1 and 2
		writePoint(face.points[0],0);
		writePoint(face.points[1],1);
		writePoint(face.points[2],2);
		writePoint(face.points[2],3); // repeat last point
	}

	private void wg(int code, String val) {
		w.println(""+code);
		w.println(val);
	}

	private void writePoint(Vector v, int n) {
		wg(10+n,frm.format(v.getX()));
		wg(20+n,frm.format(v.getZ()));
		wg(30+n,frm.format(v.getY()));
	}
	
	private void incLeavesProgressCount() {
		if (leavesProgressCount++ % 500 == 0) {
			progress.incProgress(500);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
}
