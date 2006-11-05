/**
 * 
 */
package net.sourceforge.arbaro.export;

import net.sourceforge.arbaro.params.FloatFormat;
import net.sourceforge.arbaro.tree.*;

import java.io.PrintWriter;
import java.text.NumberFormat;


/**
 * @author wolfram
 *
 */
public class POVConeExporter extends Exporter {
	Tree tree;
	PrintWriter w;
	Progress progress;

	/**
	 * 
	 */
	public POVConeExporter(Tree tree, PrintWriter pw) {
		super(tree,pw,tree.getProgress());
	}
	
	public void write() throws ErrorOutput{
		try {
			// some declarations in the POV file
			NumberFormat frm = FloatFormat.getInstance();
			
			// tree scale
			w.println("#declare " + povrayDeclarationPrefix() + "height = " 
					+ frm.format(tree.getHeight()) + ";");
			
			// leaf declaration
			if (tree.params.Leaves!=0) writeLeafDeclaration();
	
			// stems
			progress.beginPhase("writing stem objects",tree.getStemCount());
			
			for (int level=0; level < tree.params.Levels; level++) {
				
				w.println("#declare " + povrayDeclarationPrefix() + "stems_"
						+ level + " = union {");
				
				POVConeStemExporter exporter = new POVConeStemExporter(w,level);
				tree.traverseTree(exporter);
				
				w.println("}");
				
			}
			
			// leaves
			if (tree.params.Leaves!=0) {
				
				progress.beginPhase("writing leaf objects",tree.getLeafCount());
				
				w.println("#declare " + povrayDeclarationPrefix() + "leaves = union {");
				
				POVConeLeafExporter lexporter = new POVConeLeafExporter(w);
				tree.traverseTree(lexporter);
				
				w.println("}");
				
			} else { // empty declaration
				w.println("#declare " + povrayDeclarationPrefix() + "leaves = sphere {<0,0,0>,0}"); 
			}
			
			progress.endPhase();
			
			// all stems together
			w.println("#declare " + povrayDeclarationPrefix() + "stems = union {"); 
			for (int level=0; level < tree.params.Levels; level++) {
				w.println("  object {" + povrayDeclarationPrefix() + "stems_" 
						+ level + "}");
			}
			w.println("}");
			
			w.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println(e);
			throw new ErrorOutput(e.getMessage());
		}	
	}

	/**
	 * Returns a prefix for the Povray objects names,
	 * it consists of the species name and the random seed
	 * 
	 * @return the prefix string
	 */
	private String povrayDeclarationPrefix() {
		return tree.params.Species + "_" + tree.params.Seed + "_";
	}

	/**
	 * Outputs the Povray code for a leaf object
	 * 
	 * @param w The output stream
	 */
	private void writeLeafDeclaration() {
		double length = tree.params.LeafScale/Math.sqrt(tree.params.LeafQuality);
		double width = tree.params.LeafScale*tree.params.LeafScaleX/Math.sqrt(tree.params.LeafQuality);
		w.println("#include \"arbaro.inc\"");
		w.println("#declare " + povrayDeclarationPrefix() + "leaf = " +
				"object { Arb_leaf_" + (tree.params.LeafShape.equals("0")? "disc" : tree.params.LeafShape)
				+ " translate " + (tree.params.LeafStemLen+0.5) + "*y scale <" 
				+ width + "," + length + "," + width + "> }");
	}	  	
	

}
