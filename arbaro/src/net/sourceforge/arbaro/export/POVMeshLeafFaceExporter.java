/**
 * 
 */
package net.sourceforge.arbaro.export;

import java.io.PrintWriter;
import net.sourceforge.arbaro.mesh.*;
import net.sourceforge.arbaro.tree.*;

/**
 * @author wolfram
 *
 */
public class POVMeshLeafFaceExporter extends POVMeshLeafExporter {

	public POVMeshLeafFaceExporter(PrintWriter pw, LeafMesh leafMesh,
			long leafVertexOffset) {
		super(pw,leafMesh,leafVertexOffset);
	}
	
	public boolean visitLeaf(Leaf l) {
		String indent = "    ";
		
		for (int i=0; i<leafMesh.getShapeFaceCount(); i++) {
			Face face = leafMesh.shapeFaceAt(i);
			w.print("<" + (leafVertexOffset+face.points[0]) + "," 
					+ (leafVertexOffset+face.points[1]) + "," 
					+ (leafVertexOffset+face.points[2]) + ">");
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
		leafVertexOffset += leafMesh.getShapeVertexCount();
		
		incLeavesProgressCount();
		
		return true;
	}

	

	
}
