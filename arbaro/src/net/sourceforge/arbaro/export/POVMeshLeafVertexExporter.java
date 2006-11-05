/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.tree.Leaf;

/**
 * @author wolfram
 *
 */
public class POVMeshLeafVertexExporter extends POVMeshLeafExporter {

	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public POVMeshLeafVertexExporter(PrintWriter pw, LeafMesh leafMesh,
			long leafVertexOffset) {
		super(pw, leafMesh, leafVertexOffset);
		// TODO Auto-generated constructor stub
	}
	
	public boolean visitLeaf(Leaf l) {
		String indent = "    ";
	
		for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
			writeVector(l.transf.apply(leafMesh.shapeVertexAt(i).point));
			
			if (i<leafMesh.getShapeVertexCount()-1) {
				w.print(",");
			}
			if (i % 3 == 2) {
				// new line
				w.println();
				w.print(indent+"          ");
			} 
		}
		
		incLeavesProgressCount();
		
		return true;
	}


}
