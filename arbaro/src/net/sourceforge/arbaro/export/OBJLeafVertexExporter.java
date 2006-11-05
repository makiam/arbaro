/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.transformation.Vector;
import net.sourceforge.arbaro.tree.Leaf;

/**
 * @author wolfram
 *
 */
public class OBJLeafVertexExporter extends OBJLeafExporter {
	String type;
	long vertexProgressCount=0;
	
	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public OBJLeafVertexExporter(PrintWriter pw, LeafMesh leafMesh,
			int leafVertexOffset, String type) {
		super(pw, leafMesh, leafVertexOffset);
		this.type=type;
	}

	public boolean visitLeaf(Leaf l) {
		for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
			
			if (type=="v") {
				writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).point),type);
			} else {
				writeVertex(l.transf.apply(leafMesh.shapeVertexAt(i).normal),type);
			}
		}
		
		incVertexProgressCount();
		
		return true;
	}
	
	private void incVertexProgressCount() {
		if (vertexProgressCount++ % 100 == 0) {
			progress.incProgress(100);
			if (tree.params.verbose) System.err.print(".");
		}
	}
	
	private void writeVertex(Vector v, String type) {
		w.println(type+" "
				+fmt.format(v.getX())+" "
				+fmt.format(v.getZ())+" "
				+fmt.format(v.getY()));
	}
	
}
