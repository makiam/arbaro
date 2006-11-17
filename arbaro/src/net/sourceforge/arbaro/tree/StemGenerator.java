//  #**************************************************************************
//  #
//  #    Copyright (C) 2003-2006  Wolfram Diestel
//  #
//  #    This program is free software; you can redistribute it and/or modify
//  #    it under the terms of the GNU General Public License as published by
//  #    the Free Software Foundation; either version 2 of the License, or
//  #    (at your option) any later version.
//  #
//  #    This program is distributed in the hope that it will be useful,
//  #    but WITHOUT ANY WARRANTY; without even the implied warranty of
//  #    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  #    GNU General Public License for more details.
//  #
//  #    You should have received a copy of the GNU General Public License
//  #    along with this program; if not, write to the Free Software
//  #    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
//  #
//  #    Send comments and bug fixes to diestel@steloj.de
//  #
//  #**************************************************************************/

package net.sourceforge.arbaro.tree;

import net.sourceforge.arbaro.params.*;
import net.sourceforge.arbaro.transformation.*;

/**
 * @author wolfram
 *
 */
class StemGenerator extends GeneratorBase {
	TreeImpl tree;
	Params par;
	LevelParams lpar;
	TreeGenerator treeFactory;
	
	// stems shouldn't be shorter than 1/2 mm,
	// and taller than 0.05 mm - otherwise 
	// the mesh will be corrupted
	// if you like smaller plants you should
	// design them using cm or mm instead of m
	final static double MIN_STEM_LEN=0.0005; 
	final static double MIN_STEM_RADIUS=MIN_STEM_LEN/10; 

	public StemGenerator(TreeImpl tree, TreeGenerator treeFactory) {
		super(treeFactory);
		this.tree = tree;
		this.treeFactory = treeFactory;
		this.par = treeFactory.par;
	}
	/**
	 * Make a clone of the stem at this position
	 * 
	 * @param trf The base transformation for the clone
	 * @param start_segm Start segment number, i.e. the height, where
	 *        the clone spreads out
	 * @return The clone stem object
	 */
	public StemImpl makeClone(StemImpl clonedFrom, Transformation trf, int start_segm) {
		// creates a clone stem with same atributes as this stem
		StemImpl clone = new StemImpl(tree,clonedFrom,clonedFrom.stemlevel,trf,clonedFrom.offset);
		clone.segmentLength = clonedFrom.segmentLength;
		clone.segmentCount = clonedFrom.segmentCount;
		clone.length = clonedFrom.length;
		clone.baseRadius = clonedFrom.baseRadius;
		clone.splitCorrection = clonedFrom.splitCorrection; 
		clone.pruneTest = clonedFrom.pruneTest;
		clone.index = clonedFrom.index;
		
		//DBG("Stem.clone(): clone_index "+clone_index);
		clone.cloneIndex.addAll(clonedFrom.cloneIndex);
		
		//DBG("Stem.clone(): level: "+stemlevel+" clones "+clones);
		clone.cloneIndex.addElement(new Integer(clonedFrom.clones.size()));
		if (! clonedFrom.pruneTest) {
			clone.lengthChildMax = clonedFrom.lengthChildMax;
			//clone.substem_cnt = substem_cnt;
			clone.substemsPerSegment = clonedFrom.substemsPerSegment;
			//clone.substemdist = substemdist;
			//clone.substemdistv = substemdistv;
			//clone.seg_splits = self.seg_splits
			// FIXME: for more then one clone this angle should somehow
			// correspond to the rotation angle of the clone
			clone.substemRotangle=clonedFrom.substemRotangle+180;
			clone.leavesPerSegment=clonedFrom.leavesPerSegment;
		}
		return clone;
	}
	
	/**
	 * Makes the stem, i.e. calculates all its segments, clones and substems
	 * a.s.o. recursively
	 */
	public StemImpl makeStem(StemImpl growsOutOf, int stemlevel, 
			Transformation trf, double offs) {
		
		StemImpl stem = new StemImpl(tree,growsOutOf,stemlevel,trf,offs);
		
		lpar = par.getLevelParams(stem.getLevel());

		// initialize lists
		stem.segments = new java.util.Vector(lpar.nCurveRes);
		
		if (lpar.nSegSplits > 0 || par._0BaseSplits>0) {
			stem.clones = new java.util.Vector(); // lpar.nSegSplits*lpar.nCurveRes+1);
		}
		
		if (stem.stemlevel < par.Levels-1) {
			// next levels parameters
			LevelParams lpar_1 = par.getLevelParams(stem.getLevel()+1);
			stem.substems = new java.util.Vector(lpar_1.nBranches);
		}
		
		if (stem.stemlevel == par.Levels-1 && par.Leaves != 0) {
			stem.leaves = new java.util.Vector(Math.abs(par.Leaves));
		}
		
		// inialize other variables
		stem.leavesPerSegment = 0;
		stem.splitCorrection = 0;
		stem.index=0; // substem number
		stem.cloneIndex = new java.util.Vector();
		stem.pruneTest = false; // flag used for pruning
		
		// makes the stem with all its segments, substems, clones and leaves
		stem.segmentCount = lpar.nCurveRes;
		stem.length = stemLength(stem);
		stem.segmentLength = stem.length/lpar.nCurveRes;
		stem.baseRadius = stemBaseRadius(stem);
		if (stem.stemlevel==0) {
			double baseWidth = Math.max(stem.baseRadius,stemRadius(stem,0));
			stem.minMaxTest(new Vector(baseWidth,baseWidth,0));
		}
		
		treeFactory.DBG(stem,"Stem.make(): len: "+stem.length+" sgm_cnt: "+ stem.segmentCount+" base_rad: "+stem.baseRadius);
		
		// FIXME: should pruning occur for the trunk too?
		if (stem.stemlevel>0 && par.PruneRatio > 0) {
			pruning(stem);
		}
		
		// FIXME: if length<=MIN_STEM_LEN the stem object persists here but without any segments
		// alternatively make could return an error value, the invoking function
		// then had to delete this stem
		if (stem.length > MIN_STEM_LEN && stem.baseRadius > MIN_STEM_RADIUS)
		{
			prepareSubstemParams(stem);
			makeSegments(stem,0,stem.segmentCount);
		} else {
			treeFactory.DBG(stem,"length "+stem.length+" (after pruning?) to small - stem not created");
		}
		
//			tree.minMaxTest(maxPoint);
//			tree.minMaxTest(minPoint);
		
		return stem;
	}
	
	/**
	 * Apply pruning to the stem. If it grows out of the 
	 * pruning envelope, it is shortened.
	 */
	void pruning(StemImpl stem) {
		
		// save random state, split and len values
		lpar.saveState();
		double splitcorr = stem.splitCorrection;
		double origlen = stem.length;
		//double seglen = segmentLength;
		
		// start pruning
		stem.pruneTest = true;
		
		// test length
		int segm = makeSegments(stem,0,stem.segmentCount);
		
		while (segm >= 0 && stem.length > 0.001*par.scale_tree) {
			
			// restore random state and split values
			lpar.restoreState();
			stem.splitCorrection = splitcorr;
			
			// delete segments and clones
			if (stem.clones != null) stem.clones.clear();
			stem.segments.clear();
			
			// get new length
			double minlen = stem.length/2; // shorten max. half of length
			double maxlen = stem.length-origlen/15; // shorten min of 1/15 of orig. len
			stem.length = Math.min(Math.max(stem.segmentLength*segm,minlen),maxlen);
			
			// calc new values dependent from length
			stem.segmentLength = stem.length/lpar.nCurveRes;
			stem.baseRadius = stemBaseRadius(stem);
			
			if (stem.length>MIN_STEM_LEN && stem.baseRadius < MIN_STEM_RADIUS)
				System.err.println("WARNING: stem radius ("+stem.baseRadius+") too small for stem "+stem.getTreePosition());
			
			// test once more
			if (stem.length > MIN_STEM_LEN) segm = makeSegments(stem,0,stem.segmentCount);
		}
		// this length fits the envelope, 
		// diminish the effect corresp. to PruneRatio
		stem.length = origlen - (origlen-stem.length)*par.PruneRatio;
		
		// restore random state and split values
		lpar.restoreState();
		stem.splitCorrection = splitcorr;
		// delete segments and clones
		if (stem.clones != null) stem.clones.clear();
		stem.segments.clear();
		stem.pruneTest = false;
		//self.DBG("PRUNE-ok: len: %f, segm: %d/%d\n"%(self.length,segm,self.segment_cnt))
	}
	
	/**
	 * Calcs stem length from parameters and parent length
	 * 
	 * @return the stem length
	 */
	double stemLength(StemImpl stem) {
		if (stem.stemlevel == 0) { // trunk
			return (lpar.nLength + lpar.var(lpar.nLengthV)) * par.scale_tree;
		} else if (stem.stemlevel == 1) {
			double parlen = stem.parent.length;
			double baselen = par.BaseSize*par.scale_tree;
			double ratio  = (parlen-stem.offset)/(parlen-baselen);
			treeFactory.DBG(stem,"Stem.stem_length(): parlen: "+parlen+" offset: "+stem.offset+" baselen: "+baselen+" ratio: "+ratio);
			return parlen * stem.parent.lengthChildMax * par.getShapeRatio(ratio);
		} else { // higher levels
			return stem.parent.lengthChildMax*(stem.parent.length-0.6*stem.offset);
		}
	}
	
	// makes the segments of the stem
	int makeSegments(StemImpl stem, int start_seg,int end_seg) {
		
		if (stem.stemlevel==1) treeFactory.updateGenProgress();
		
		if (treeFactory.verbose) {
			if (! stem.pruneTest) {
				if (stem.stemlevel==0) System.err.print("=");
				else if (stem.stemlevel==1 && start_seg==0) System.err.print("/");
				else if (stem.stemlevel==2 && start_seg==0) System.err.print(".");
			}
		}
		
		Transformation trf = stem.transf;
		SegmentGenerator segmentFactory = new SegmentGenerator(stem,this);
		
		for (int s=start_seg; s<end_seg; s++) {
			if (stem.stemlevel==0) treeFactory.updateGenProgress();
			
			if (! stem.pruneTest && treeFactory.verbose) {
				if (stem.stemlevel==0) System.err.print("|");
			}
			
			// curving
			trf=newDirection(stem,trf,s);
			treeFactory.TRF(stem,"Stem.make_segments(): after new_direction ",trf);
			
			// segment radius
			double rad1 = stemRadius(stem,s*stem.segmentLength);
			double rad2 = stemRadius(stem,(s+1)*stem.segmentLength);
			
			// create new segment
			//Segment segment = new Segment(stem,s,trf,rad1,rad2);
			//makeSegment(stem,segment);
			
			SegmentImpl segment = segmentFactory.makeSegment(s,trf,rad1,rad2);
			stem.segments.addElement(segment);
			
			// create substems
			// self.DBG("SS-makingsubst? pt: %d, lev: %d\n"%(self.prunetest,self.level))
			if (! stem.pruneTest && lpar.level<par.Levels-1) {
				// self.DBG("SS-making substems\n")
				makeSubstems(stem,segment);
			}
			
			if (! stem.pruneTest && lpar.level==par.Levels-1 && par.Leaves!=0) {
				makeLeaves(stem,segment);
			}
			
			// shift to next position
			trf = trf.translate(trf.getZ().mul(stem.segmentLength));
			//self.DBG("transf: %s\n"%(transf))
			//self.DBG("pos: %s\n"%(transf.vector))
			
			// test if too long
			if (stem.pruneTest && ! isInsideEnvelope(trf.getT())) {
				// DBG("PRUNE: not inside - return %d\n"%(s))
				return s;
			}
			
			// splitting (create clones)
			if (s<end_seg-1) {
				int segm = makeClones(stem,trf,s);
				// trf is changed by make_clones
				// prune test - clone not inside envelope 
				if (segm>=0) {
					//DBG("PRUNE: clone not inside - return %d\n"%(segm))
					return segm;
				}
			}
		}
		
		return -1;
	}
	
	/**
	 * Tests if a point is inside the pruning envelope
	 * 
	 * @param vector the point to test
	 * @return true if the point is inside the pruning envelope
	 */
	boolean isInsideEnvelope(Vector vector) {
		double r = Math.sqrt(vector.getX()*vector.getX() + vector.getY()*vector.getY());
		double ratio = (par.scale_tree - vector.getZ())/(par.scale_tree*(1-par.BaseSize));
		return (r/par.scale_tree) < (par.PruneWidth * par.getShapeRatio(ratio,8));
	}
	
	
	/**
	 * Calcs a new direction for the current segment
	 * 
	 * @param trf The transformation of the previous segment
	 * @param nsegm The number of the segment ( for testing, if it's the
	 *              first stem segment
	 * @return The new transformation of the current segment
	 */
	Transformation newDirection(StemImpl stem, Transformation trf, int nsegm) {
		// next segments direction
		
		// The first segment shouldn't get another direction 
		// down and rotation angle shouldn't be falsified 
		if (nsegm == 0) return trf;
		
		treeFactory.TRF(stem,"Stem.new_direction() before curving",trf);
		
		// get curving angle
		double delta;
		if (lpar.nCurveBack==0) {
			delta = lpar.nCurve / lpar.nCurveRes;
			
		} else {
			if (nsegm < (lpar.nCurveRes+1)/2) {
				delta = lpar.nCurve*2 / lpar.nCurveRes;
			} else {
				delta = lpar.nCurveBack*2 / lpar.nCurveRes;
			}
		}
		delta += stem.splitCorrection;
		DBG(stem,"Stem.new_direction(): delta: "+delta);
		trf = trf.rotx(delta);
		
		
		// With Weber/Penn the orientation of the x- and y-axis 
		// shouldn't be disturbed (maybe, because proper curving relies on this)
		// so may be such random rotations shouldn't be used, instead nCurveV should
		// add random rotation to rotx, and rotate nCurveV about the tree's z-axis too?
		
		// add random rotation about z-axis
		if (lpar.nCurveV > 0) {
			//    		if (nsegm==0 && stemlevel==0) { // first_trunk_segment
			//    			// random rotation more moderate
			//    			delta = (Math.abs(lpar.var(lpar.nCurveV)) - 
			//    					Math.abs(lpar.var(lpar.nCurveV)))
			//						/ lpar.nCurveRes;
			//    		}	else {
			// full random rotation
			delta = lpar.var(lpar.nCurveV)/lpar.nCurveRes;
			//    		}
			// self.DBG("curvV (delta): %s\n" % str(delta))
			double rho = 180+lpar.var(180);
			trf = trf.rotaxisz(delta,rho);
		}  
		TRF(stem,"Stem.new_direction() after curving",trf);
		
		// attraction up/down
		if (par.AttractionUp != 0 && stem.stemlevel>=2) {
			
			double declination = Math.acos(trf.getZ().getZ());
			
			// 			I don't see, why we need orientation here, may be this avoids
			//          attraction of branches with the x-Axis up and thus avoids
			//			twisting (see below), but why branches in one direction should
			//			be attracted, those with another direction not, this is unnaturally:
			//    		double orient = Math.acos(trf.getY().getZ());
			//    		double curve_up_orig = par.AttractionUp * declination * Math.cos(orient)/lpar.nCurveRes; 
			
			// FIXME: devide by (lpar.nCurveRes-nsegm) if last segment
			// should actually be vertical 
			double curve_up = par.AttractionUp * 
			Math.abs(declination * Math.sin(declination)) / lpar.nCurveRes;
			
			Vector z = trf.getZ();
			// FIXME: the mesh is twisted for high values of AttractionUp
			trf = trf.rotaxis(-curve_up*180/Math.PI,new Vector(-z.getY(),z.getX(),0));
			// trf = trf.rotx(curve_up*180/Math.PI);
		}
		return trf;
	}
	
	/**
	 * Calcs the base radius of the stem
	 * 
	 * @return
	 */
	double stemBaseRadius(StemImpl stem) {
		if (stem.stemlevel == 0) { // trunk
			// radius at the base of the stem
			// I think nScale+-nScaleV should applied to the stem radius but not to base radius(?)
			return stem.length * par.Ratio; // * par._0Scale; 
			//+ var(par._0ScaleV))
		} else {
			// max radius is the radius of the parent at offset
			double max_radius = stemRadius(stem.parent,stem.offset);
			
			// FIXME: RatioPower=0 seems not to work here
			double radius = stem.parent.baseRadius * Math.pow(stem.length/stem.parent.length,par.RatioPower);
			return Math.min(radius,max_radius);
		}
	}
	
	/**
	 * Calcs the stem radius at a given offset
	 * 
	 * @param h the offset at which the radius is calculated
	 * @return the stem radius at this position
	 */
	public double stemRadius(StemImpl stem, double h) {
		DBG(stem,"Stem.stem_radius("+h+") base_rad:"+stem.baseRadius);
		
		double angle = 0; //FIXME: add an argument "angle" for Lobes, 
		// but at the moment Lobes are calculated later in mesh creation
		
		// gets the stem width at a given position within the stem
		double Z = Math.min(h/stem.length,1.0); // min, to avoid rounding errors
		double taper = lpar.nTaper;
		
		double unit_taper=0;
		if (taper <= 1) {
			unit_taper = taper;
		} else if (taper <=2) {
			unit_taper = 2 - taper;
		}
		
		double radius = stem.baseRadius * (1 - unit_taper * Z);
		
		double depth;
		if (taper>1) {
			double Z2 = (1-Z)*stem.length;
			if (taper<2 || Z2<radius) {
				depth = 1;
			} else {
				depth=taper-2;
			}
			double Z3;
			if (taper<2) {
				Z3=Z2;
			} else {
				Z3=Math.abs(Z2-2*radius*(int)(Z2/2/radius+0.5));
			}
			if (taper>2 || Z3<radius) {
				radius=(1-depth)*radius+depth*Math.sqrt(radius*radius-(Z3-radius)*(Z3-radius));
				//  self.DBG("TAPER>2: Z2: %f, Z3: %f, depth: %f, radius %f\n"%(Z2,Z3,depth,radius))
			}	  
		}	    
		if (stem.stemlevel==0) { 
			// add flaring (thicker stem base)
			if (par.Flare != 0) {
				double y = Math.max(0,1-8*Z);
				double flare = 1 + par.Flare * (Math.pow(100,y) - 1) / 100.0;
				DBG(stem,"Stem.stem_radius(): Flare: "+flare+" h: "+h+" Z: "+Z);
				radius = radius*flare;
			}
			// add lobes - this is done in mesh creation not here at the moment
			if (par.Lobes>0 && angle!=0) {
				// FIXME: use the formular from Segment.create_mesh_section() instead
				radius = radius*(1.0+par.LobeDepth*Math.sin(par.Lobes*angle*Math.PI/180));
			}
			
			// multiply with 0Scale;
			// 0ScaleV is applied only in mesh creation (Segment.create_section_meshpoints)
			radius = radius*par._0Scale;
		}
		
		DBG(stem,"Stem.stem_radius("+h+") = "+radius);
		
		return radius;
	}
	
	
	/**
	 * Precalcs some stem parameters used later during when generating
	 * the current stem
	 */
	void prepareSubstemParams(StemImpl stem) {
		//int level = min(stemlevel+1,3);
		LevelParams lpar_1 = par.getLevelParams(stem.getLevel()+1);
		
		// maximum length of a substem
		stem.lengthChildMax = lpar_1.nLength+lpar_1.var(lpar_1.nLengthV);
		
		// maximum number of substems
		double stems_max = lpar_1.nBranches;
		
		// actual number of substems and substems per segment
		double substem_cnt;
		if (stem.stemlevel==0) {
			substem_cnt = stems_max;
			stem.substemsPerSegment = substem_cnt / (float)stem.segmentCount / (1-par.BaseSize);
			DBG(stem,"Stem.prepare_substem_params(): stems_max: "+ substem_cnt 
					+ " substems_per_segment: " + stem.substemsPerSegment);
		} else if (par.preview) {
			substem_cnt = stems_max;
			stem.substemsPerSegment = substem_cnt / (float)stem.segmentCount;
		} else if (stem.stemlevel==1) {
			substem_cnt = (int)(stems_max * 
					(0.2 + 0.8*stem.length/stem.parent.length/stem.parent.lengthChildMax));
			stem.substemsPerSegment = substem_cnt / (float)stem.segmentCount;
			DBG(stem,"Stem.prepare_substem_params(): substem_cnt: "+ substem_cnt 
					+ " substems_per_segment: " + stem.substemsPerSegment);
		} else {
			substem_cnt = (int)(stems_max * (1.0 - 0.5 * stem.offset/stem.parent.length));
			stem.substemsPerSegment = substem_cnt / (float)stem.segmentCount;
		}
		stem.substemRotangle = 0;
		
		// how much leaves for this stem - not really a substem parameter
		if (lpar.level == par.Levels-1) {
			stem.leavesPerSegment = leavesPerBranch(stem) / stem.segmentCount;
		}
	}
	
	/**
	 * Number of leaves of the stem
	 * 
	 * @return
	 */
	double leavesPerBranch(StemImpl stem) {
		// calcs the number of leaves for a stem
		if (par.Leaves==0) return 0;
		if (stem.stemlevel == 0) {
			// FIXME: maybe set Leaves=0 when Levels=1 in Params.prepare()
			System.err.println("WARNING: trunk cannot have leaves, no leaves are created");
			return 0;
		}
		
		return (Math.abs(par.Leaves) 
				* par.getShapeRatio(stem.offset/stem.parent.length,par.LeafDistrib) 
				* par.LeafQuality);
	}
	
	/**
	 * Make substems of the current stem
	 * 
	 * @param segment
	 */
	void makeSubstems(StemImpl stem, SegmentImpl segment) {
		// creates substems for the current segment
		LevelParams lpar_1 = par.getLevelParams(stem.getLevel()+1);
		
		DBG(stem,"Stem.make_substems(): substems_per_segment "+stem.substemsPerSegment);
		
		double subst_per_segm;
		double offs;
		
		if (stem.stemlevel>0) {
			// full length of stem can have substems
			subst_per_segm = stem.substemsPerSegment;
			
			if (segment.index==0) {
				offs = stemRadius(stem.parent,stem.offset)/stem.segmentLength;
			} else { offs = 0; }
			
		} else if (segment.index*stem.segmentLength > par.BaseSize*stem.length) {
			// segment is fully out of the bare trunk region => normal nb of substems
			subst_per_segm = stem.substemsPerSegment;
			offs = 0;
		} else if ((segment.index+1)*stem.segmentLength <= par.BaseSize*stem.length) {
			// segment is fully part of the bare trunk region => no substems
			return;
		} else {
			// segment has substems in the upper part only
			offs = (par.BaseSize*stem.length - segment.index*stem.segmentLength)/stem.segmentLength;
			subst_per_segm = stem.substemsPerSegment*(1-offs);
		}	
		
		// how many substems in this segment
		int substems_eff = (int)(subst_per_segm + lpar.substemErrorValue+0.5);
		
		// adapt error value
		lpar.substemErrorValue -= (substems_eff - subst_per_segm);
		
		if (substems_eff <= 0) return;
		
		DBG(stem,"Stem.make_substems(): substems_eff: "+substems_eff);
		
		// what distance between the segements substems
		double dist = (1.0-offs)/substems_eff*lpar_1.nBranchDist;
		double distv = dist*0.25; // lpar_1.nBranchDistV/2;
		
		DBG(stem,"Stem.make_substems(): offs: "+offs+" dist: "+dist+" distv: "+distv);
		
		for (int s=0; s<substems_eff; s++) {
			// where on the segment add the substem
			double where = offs+dist/2+s*dist+lpar_1.var(distv);
			
			//offset from stembase
			double offset = (segment.index + where) * stem.segmentLength;
			
			DBG(stem,"Stem.make_substems(): offset: "+ offset+" segminx: "+segment.index
					+" where: "+where+ " seglen: "+stem.segmentLength);
			
			Transformation trf = substemDirection(stem,segment.transf,offset);
			trf = substemPosition(segment,trf,where);
			
			// create new substem
			StemImpl substem = makeStem(stem,stem.stemlevel+1,trf,offset);
			substem.index=stem.substems.size();
			DBG(stem,"Stem.make_substems(): make new substem");
			stem.substems.addElement(substem);
		}
	}
	
	/**
	 * Calcs the position of a substem in the segment given 
	 * a relativ position where in 0..1 - needed esp. for helical stems,
	 * because the substems doesn't grow from the axis of the segement
	 *
	 * @param trf the transformation of the substem
	 * @param where the offset, where the substem spreads out
	 * @return the new transformation of the substem (shifted from
	 *        the axis of the segment to the axis of the subsegment)
	 */
	public Transformation substemPosition(SegmentImpl segm, Transformation trf, double where) {
		if (lpar.nCurveV>=0) { // normal segment 
			return trf.translate(segm.transf.getZ().mul(where*segm.length));
		} else { // helix
			// get index of the subsegment
			int i = (int)(where*(segm.subsegments.size()-1));
			// interpolate position
			Vector p1 = ((SubsegmentImpl)segm.subsegments.elementAt(i)).pos;
			Vector p2 = ((SubsegmentImpl)segm.subsegments.elementAt(i+1)).pos;
			Vector pos = p1.add(p2.sub(p1).mul(where - i/(segm.subsegments.size()-1)));
			return trf.translate(pos.sub(segm.getLowerPosition()));
		}
	}
	/**
	 * Calcs the direction of a substem from the parameters
	 * 
	 * @param trf The transformation of the current stem segment
	 * @param offset The offset of the substem from the base of the currents stem
	 * @return The direction of the substem
	 */
	Transformation substemDirection(StemImpl stem, Transformation trf, double offset) {
		LevelParams lpar_1 = par.getLevelParams(stem.getLevel()+1);
		//lev = min(level+1,3);
		
		// get rotation angle
		double rotangle;
		if (lpar_1.nRotate>=0) { // rotating substems
			stem.substemRotangle = (stem.substemRotangle + lpar_1.nRotate+lpar_1.var(lpar_1.nRotateV)+360) % 360;
			rotangle = stem.substemRotangle;
		} else { // alternating substems
			if (Math.abs(stem.substemRotangle) != 1) stem.substemRotangle = 1;
			stem.substemRotangle = -stem.substemRotangle;
			rotangle = stem.substemRotangle * (180+lpar_1.nRotate+lpar_1.var(lpar_1.nRotateV));
		}
		
		// get downangle
		double downangle;
		if (lpar_1.nDownAngleV>=0) {
			downangle = lpar_1.nDownAngle+lpar_1.var(lpar_1.nDownAngleV);
		} else {
			double len = (stem.stemlevel==0)? stem.length*(1-par.BaseSize) : stem.length;
			downangle = lpar_1.nDownAngle +
			lpar_1.nDownAngleV*(1 - 2 * par.getShapeRatio((stem.length-offset)/len,0));
		}  
		DBG(stem,"Stem.substem_direction(): down: "+downangle+" rot: "+rotangle);
		
		return trf.rotxz(downangle,rotangle);
	}
	
	/**
	 * Creates the leaves for the current stem segment
	 * 
	 * @param segment
	 */
	void makeLeaves(StemImpl stem, SegmentImpl segment) {
		// creates leaves for the current segment
		
		if (par.Leaves > 0) { // ### NORMAL MODE, leaves along the stem
			// how many leaves in this segment
			double leaves_eff = (int)(stem.leavesPerSegment + par.leavesErrorValue+0.5);
			
			// adapt error value
			par.leavesErrorValue -= (leaves_eff - stem.leavesPerSegment);
			
			if (leaves_eff <= 0) return;
			
			double offs;
			if (segment.index==0) {
				offs = stemRadius(stem.parent,stem.offset)/stem.segmentLength;
			} else {
				offs = 0;
			}
			
			// what distance between the leaves
			double dist = (1.0-offs)/leaves_eff;
			
			for (int s=0; s<leaves_eff; s++) {
				// where on the segment add the leaf
				
				// FIXME: may be use the same distribution method (BranchDist) as for substems?
				double where = offs+dist/2+s*dist+lpar.var(dist/2);
				
				// offset from stembase
				double loffs = (segment.index+where)*stem.segmentLength;
				// get a new direction for the leaf
				Transformation trf = substemDirection(stem,segment.transf,loffs);
				// translate it to its position on the stem
				trf = trf.translate(segment.transf.getZ().mul(where*stem.segmentLength));
				
				// create new leaf
				LeafImpl leaf = new LeafImpl(par,trf/*,loffs*/);
				leaf.make();
				stem.leaves.addElement(leaf);
				
			}
		}
		
		// ##### FAN MOD, leaves placed in a fan at stem end
		else if (par.Leaves<0 && segment.index == stem.segmentCount-1) {
			
			LevelParams lpar_1 = par.getLevelParams(stem.getLevel()+1);
			int cnt = (int)(leavesPerBranch(stem)+0.5);
			
			Transformation trf = segment.transf.translate(segment.transf.getZ().mul(stem.segmentLength));
			double distangle = lpar_1.nRotate/cnt;
			double varangle = lpar_1.nRotateV/cnt;
			double downangle = lpar_1.nDownAngle;
			double vardown = lpar_1.nDownAngleV;
			double offsetangle = 0;
			// use different method for odd and even number
			if (cnt%2 == 1) {
				// create one leaf in the middle
				LeafImpl leaf = new LeafImpl(par,trf/*,segmentCount*segmentLength*/);
				leaf.make();
				stem.leaves.addElement(leaf);
				offsetangle = distangle;
			} else {
				offsetangle = distangle/2;
			}
			// create leaves left and right of the middle
			for (int s=0; s<cnt/2; s++) {
				for (int rot=1; rot >=-1; rot-=2) {
					Transformation transf1 = trf.roty(rot*(offsetangle+s*distangle
							+lpar_1.var(varangle)));
					transf1 = transf1.rotx(downangle+lpar_1.var(vardown));
					LeafImpl leaf = new LeafImpl(par,transf1/*,segmentCount*segmentLength*/);
					leaf.make();
					stem.leaves.addElement(leaf);
				}
			}
		}
	}
	
	
	/**
	 * Make clones of the current stem at the current segment
	 * 
	 * @param trf The current segments's direction
	 * @param nseg The number of the current segment
	 * @return Segments outside the pruning envelope, -1
	 *         if stem clone is completely inside the envelope
	 */
	int makeClones(StemImpl stem, Transformation trf,int nseg) {
		// splitting
		// FIXME: maybe move this calculation to LevelParams
		// but pay attention to saving errorValues and restoring when making prune tests
		int seg_splits_eff;
		if (stem.stemlevel==0 && nseg==0 && par._0BaseSplits>0) {
			seg_splits_eff = par._0BaseSplits;
		}
		else {
			// how many clones?
			double seg_splits = lpar.nSegSplits;
			seg_splits_eff = (int)(seg_splits+lpar.splitErrorValue+0.5);
			
			// adapt error value
			lpar.splitErrorValue -= (seg_splits_eff - seg_splits);
		}
		
		if (seg_splits_eff<1) return -1;
		
		double s_angle = 360/(seg_splits_eff+1);
		
		// make clones
		// if seg_splits_eff > 0:
		for (int i=0; i<seg_splits_eff; i++) { 
			
			// copy params
			StemImpl clone = makeClone(stem,trf,nseg+1);
			
			// NOTE: its a little bit problematic here
			// when the clone is given as a parent to
			// the substems, it should have the same
			// params for length and segment_cnt like
			// the original stem, but this could be
			// somewhat confusing(?)
			// clone.segment_cnt = remaining_segs;
			// clone.length = remaining_segs * self.segment_len
			
			// change the direction for the clone
			//if self.debug: sys.stderr.write("-SPLIT_CORE_BEFOR: %s, dir: %s\n" % \
			//	(str(clone.split_corr),str(clone.direction)))
			
			clone.transf = split(clone,trf,s_angle*(1+i),nseg,seg_splits_eff);
			
			//if self.debug: sys.stderr.write("-SPLIT_CORE_AFTER: %s, dir: %s\n" % 
			//	(str(clone.split_corr),str(clone.direction)))
			
			// make segments etc. for the clone
			int segm = makeSegments(clone,nseg+1,clone.segmentCount);
			if (segm>=0) { // prune test - clone not inside envelope
				return segm;
			}
			// add clone to the list
			stem.clones.addElement(clone);
		}
		// get another direction for the original stem too   
		trf = split(stem,trf,0,nseg,seg_splits_eff);
		return -1;
	}
	
	
	/**
	 * Gives a clone a new direction (splitting)
	 * 
	 * @param trf The base transformation of the clone 
	 * @param s_angle The splitting angle
	 * @param nseg The segment number, where the clone begins
	 * @param nsplits The number of clones
	 * @return The new direction for the clone
	 */
	Transformation split(StemImpl stem,Transformation trf,
			double s_angle, int nseg, int nsplits) {
		// applies a split angle to the stem - the Weber/Penn method
		int remaining_seg = stem.segmentCount-nseg-1;
		
		// the splitangle
		// FIXME: don't know if it should be nSplitAngle or nSplitAngle/2
		double declination = Math.acos(trf.getZ().getZ())*180/Math.PI;
		double split_angle = Math.max(0,(lpar.nSplitAngle
				+ lpar.var(lpar.nSplitAngleV) - declination));
		
		// FIXME: first works better for level 0, second for further levels
		// transf = transf.rotxz(split_angle,s_angle)
		trf = trf.rotx(split_angle);
		
		// adapt split correction
		stem.splitCorrection -=  split_angle/remaining_seg;
		//t_corr = Transformation().rotx(-split_angle/remaining_seg)
		
		double split_diverge;
		if (s_angle>0) { // original stem has s_angle==0    
			if (par._0BaseSplits>0 && stem.stemlevel==0 && nseg==0) {
				split_diverge = s_angle + lpar.var(lpar.nSplitAngleV);
			}	else {
				split_diverge = 20 + 0.75 * (30 + Math.abs(declination-90)) 
				* Math.pow((lpar.var(1)+1)/2.0,2);
				if (lpar.var(1) >= 0) split_diverge = - split_diverge;
			}
			
			trf = trf.rotaxis(split_diverge,Vector.Z_AXIS);
			
		} else split_diverge = 0; // for debugging only
		
		// adjust some parameters	
		//split_cnt = split_cnt+1;
		
		// lower substem prospensity
		if (! stem.pruneTest) {
			stem.substemsPerSegment /= (float)(nsplits+1);
			// FIXME: same reduction for leaves_per_segment?
		}
		return trf;
	}
	
	/*
	 # The Weber/Penn splitting method is problematic for big splitting angles, or
	 # may be i misunderstood it, but it seems, that evenly splitting like for
	 # an umbrella formed acacia (don't know the english name of that trees) isn't
	 */

}
