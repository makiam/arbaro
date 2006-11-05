/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.tree.*;

/**
 * @author wolfram
 *
 */
public class POVMeshLeafNormalExporter extends POVMeshLeafExporter {

	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public POVMeshLeafNormalExporter(PrintWriter pw, LeafMesh leafMesh,
			long leafVertexOffset) {
		super(pw, leafMesh, leafVertexOffset);
		// TODO Auto-generated constructor stub
	}

	public boolean visitLeaf(Leaf l) throws TraversalException {
		String indent = "    ";
		
		try {
			for (int i=0; i<leafMesh.getShapeVertexCount(); i++) {
				writeVector(l.transf.apply(leafMesh.shapeVertexAt(i).normal));
				
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

			throw new Exception("Not implemented: if using normals for leaves use factor "+
			"3 instead of 2 in progress.beginPhase");
			
		} catch (Exception e) {
			throw new TraversalException(e.toString());
		}

		//return true;

	}

}
