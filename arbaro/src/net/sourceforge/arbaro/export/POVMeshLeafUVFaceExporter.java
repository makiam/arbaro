/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;

import net.sourceforge.arbaro.mesh.Face;
import net.sourceforge.arbaro.mesh.LeafMesh;
import net.sourceforge.arbaro.tree.Leaf;

/**
 * @author wolfram
 *
 */
public class POVMeshLeafUVFaceExporter extends POVMeshLeafExporter {

	/**
	 * @param pw
	 * @param leafMesh
	 * @param leafVertexOffset
	 */
	public POVMeshLeafUVFaceExporter(PrintWriter pw, LeafMesh leafMesh,
			long leafVertexOffset) {
		super(pw, leafMesh, leafVertexOffset);
		// TODO Auto-generated constructor stub
	}

	public boolean visitLeaf(Leaf l) {
		String indent = "    ";
		
		for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
			Face face = leafMesh.shapeFaceAt(i);
			w.print("<" + (/*leafFaceOffset+*/face.points[0]) + "," 
					+ (/*leafFaceOffset+*/face.points[1]) + "," 
					+ (/*leafFaceOffset+*/face.points[2]) + ">");
			if (i<leafMesh.getShapeFaceCount()-1) {
				w.print(",");
			}
			if (i % 6 == 4) {
				// new line
				w.println();
				w.print(indent + "          ");
			}
		}
		w.println();
			
		// increment face offset
		//leafFaceOffset += leafMesh.getShapeVertexCount();
		
		incLeavesProgressCount();
		
		return true;
	}

}
