//  #**************************************************************************
//  #
//  #    $Id$  
//  #            - Params class - it holds the tree parameters and related methods
//  #              (the params for the levels are in LevelParams, not here!)
//  #            - classes CfgTreeParser, XMLTreeParser to read in the params
//  #          
//  #
//  #    Copyright (C) 2003  Wolfram Diestel
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

package net.sourceforge.arbaro.params;

import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileReader;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.swing.event.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

class CfgTreeParser {
  
    public void parse(String fileName, Params params) throws Exception {
	File inputFile = new File(fileName);
	LineNumberReader r = 
	    new LineNumberReader(new FileReader(inputFile));
	parse(r,params);
    }

    public void parse(InputStream is, Params params) throws Exception {
	LineNumberReader r = new LineNumberReader(new InputStreamReader(is));
	parse(r,params);
    }
    
    public void parse(LineNumberReader r, Params params) throws Exception {
	String line = r.readLine().trim();
	String param;
	String value;
	while (line != null) {
	    if (line != "" && line.charAt(0) != '#') {
		int equ = line.indexOf('=');
		param = line.substring(0,equ).trim();
		value = line.substring(equ+1).trim();
		if (param.equals("species")) {
		    params.setSpecies(value);
		} else {
		    params.setParam(param,value);
		}
		line = r.readLine();
	    }
	}
    }
}

class XMLTreeFileHandler extends DefaultHandler {

    Params params;
    String errors = "";

    public XMLTreeFileHandler(Params par) {
	params = par;
    }

    public void startElement(String namespaceURI,String localName,
            String qName,Attributes atts) throws SAXException {
	
	if (qName.equals("species")) {
	    params.setSpecies(atts.getValue("name"));
	} else if (qName.equals("param")) {

	    try {
		params.setParam(atts.getValue("name"),atts.getValue("value"));
	    } catch (ErrorParam e) {
		errors += e.getMessage()+"\n";
		// throw new SAXException(e.getMessage());
	    }
	}
    }

    /*
    public void endElement(String namespaceURI,String localName,
            String qName) {
        System.out.println("</" + qName + ">");
    }
    */
}


class XMLTreeParser {
    SAXParser parser;

    public XMLTreeParser() 
	throws ParserConfigurationException, SAXException
    {
        // get a parser factory 
        SAXParserFactory spf = SAXParserFactory.newInstance();
        // get a XMLReader 
	parser = spf.newSAXParser();
    }

    public void parse(InputSource is, Params params) throws SAXException, IOException, ErrorParam {
        // parse an XML tree file
	//InputSource is = new InputSource(sourceURI);
	XMLTreeFileHandler xml_handler = new XMLTreeFileHandler(params);
	parser.parse(is,xml_handler);
	if (xml_handler.errors != "") {
	    throw new ErrorParam(xml_handler.errors);
	}
    }
}


public class Params {
    
    // Outputformats
    public final static int MESH = 0;
    public final static int CONES = 1;
    public final static int BLOBS = 2;

    // Tree Shapes 
    public final static int CONICAL = 0;
    public final static int SPHERICAL = 1;
    public final static int HEMISPHERICAL = 2;
    public final static int CYLINDRICAL = 3;
    public final static int TAPERED_CYLINDRICAL = 4;
    public final static int FLAME = 5;
    public final static int INVERSE_CONICAL = 6;
    public final static int TEND_FLAME = 7;
    public final static int ENVELOPE = 8;

    public LevelParams [] levelParams;
    public Random random;
    Hashtable paramDB;

    // debugging etc.
    public boolean debug;
    public boolean verbose;
    public boolean ignoreVParams;
    public int stopLevel;

    // general params
    String species;
      
    public double LeafQuality;
      
    // this mesh parameters are influenced by Smooth, 
    // this are only defaults here
    public double Smooth;
    public double mesh_quality;  // 0..1 - factor for mesh point number 
                          // (1+mesh_quality)
    public int smooth_mesh_level; // -1..Levels - add average normals 
                          // to mesh points of all levels below

      
    public int output; // mesh/cones/blobs

    // the seed
    public int Seed;

    // defauls values for tree params
    public int Levels;

    // trunk&radius parameters
    public double Ratio;
    public double RatioPower;
    public int Shape;
    public double BaseSize;
    public double Flare;
    
    public int Lobes;
    public double LobeDepth;
    
    // leave parameters
    public int Leaves;
    public String LeafShape;
    public double LeafScale;
    public double LeafScaleX;

    // new introduced - not in the paper
    public double LeafStemLen;
    public double LeafBend;
    public int LeafDistrib;
       
    // tree scale
    public double Scale;
    public double ScaleV;
    
    // additional trunk scaling
    public double _0Scale; // only 0SCale used
    public double _0ScaleV; // only 0ScaleV used

    // attraction and pruning/envelope
    public double AttractionUp;
    public double PruneRatio;
    public double PrunePowerLow;
    public double PrunePowerHigh;
    public double PruneWidth;
    public double PruneWidthPeak;
    
    // base splits
    public int _0BaseSplits;
      
    // variables need for stem creation
    public double scale_tree;

    // change events
    protected ChangeEvent changeEvent = null;
    protected EventListenerList listenerList = new EventListenerList();


    public Params() {

	debug = false;
	verbose = true;
	ignoreVParams = false;

	stopLevel = -1;
	
	species = "default";
    
	LeafQuality = 1;
      
	Smooth = 0.5;
	output = MESH; // mesh/cones/blobs

	// the default seed
	Seed = 13;

	// create paramDB
	paramDB = new Hashtable();
	levelParams = new LevelParams[4];
	for (int l=0; l<4; l++) {
	    levelParams[l] = new LevelParams(l,paramDB);
	}
	register_params();
    };

    public Params(Params other) {
	

	// copy values from other
	debug = other.debug;
	verbose = other.verbose;
	ignoreVParams = other.ignoreVParams;
	stopLevel = other.stopLevel;
	species = other.species;
	output = other.output;
	Seed = other.Seed;
	Smooth = other.Smooth;

	// create paramDB
	paramDB = new Hashtable();
	levelParams = new LevelParams[4];
	for (int l=0; l<4; l++) {
	    levelParams[l] = new LevelParams(l,paramDB);
	}
	register_params();

	// copy param values
	for (Enumeration e = paramDB.elements(); e.hasMoreElements();) {
	    AbstractParam p = ((AbstractParam)e.nextElement());
	    try {
		AbstractParam otherParam = other.getParam(p.name);
		if (! otherParam.empty()) {
		    p.setValue(otherParam.getValue());
		} // else use default value
	    } catch (ErrorParam err) {
		System.err.println("Error copying params: "+err.getMessage());
	    }
	}
    }

    public void setSpecies(String sp) {
	species = sp;
	fireStateChanged();
    }

    public String getSpecies() {
	return species;
    }

    // help methods for output of params
    private void xml_param(PrintWriter w, String name, int value) {
	w.println("    <param name='" + name + "' value='"+value+"'/>");
    }
    
    private void xml_param(PrintWriter w, String name, double value) {
	w.println("    <param name='" + name + "' value='"+value+"'/>");
    }

    private void xml_param(PrintWriter w, String name, String value) {
	w.println("    <param name='" + name + "' value='"+value+"'/>");
    }

    public void toXML(PrintWriter w) throws ErrorParam {
	prepare(); // read parameters from paramDB
	w.println("<?xml version='1.0' ?>");
	w.println();
	w.println("<arbaro>");
	w.println("  <species name='" + species + "'>");
	w.println("    <!-- general params -->");
	// FIXME: maybe use paramDB to print out params
	// thus no one yould be forgotten
	xml_param(w,"Shape",Shape);
	xml_param(w,"Levels",Levels);
	xml_param(w,"Scale",Scale);
	xml_param(w,"ScaleV",ScaleV);
	xml_param(w,"BaseSize",BaseSize);
	xml_param(w,"Ratio",Ratio);
	xml_param(w,"RatioPower",RatioPower);
	xml_param(w,"Flare",Flare);
	xml_param(w,"Lobes",Lobes);
	xml_param(w,"LobeDepth",LobeDepth);
	xml_param(w,"Smooth",Smooth);
	xml_param(w,"Leaves",Leaves);
	xml_param(w,"LeafShape",LeafShape);
	xml_param(w,"LeafScale",LeafScale);
	xml_param(w,"LeafScaleX",LeafScaleX);
	xml_param(w,"LeafQuality",LeafQuality);
	xml_param(w,"LeafStemLen",LeafStemLen);
	xml_param(w,"LeafDistrib",LeafDistrib);
	xml_param(w,"LeafBend",LeafBend);
	xml_param(w,"AttractionUp",AttractionUp);
	xml_param(w,"PruneRatio",PruneRatio);
	xml_param(w,"PrunePowerLow",PrunePowerLow);
	xml_param(w,"PrunePowerHigh",PrunePowerHigh);
	xml_param(w,"PruneWidth",PruneWidth);
	xml_param(w,"PruneWidthPeak",PruneWidthPeak);
	xml_param(w,"0Scale",_0Scale); 
	xml_param(w,"0ScaleV",_0ScaleV);
	xml_param(w,"0BaseSplits",_0BaseSplits);

	for (int i=0; i <= Math.min(Levels,3); i++) {
	    levelParams[i].toXML(w,i==Levels); // i==Levels => leaf level only
	}
	w.println("  </species>");
	w.println("</arbaro>");
	w.flush();
    }

    public void clearParams() {
	for (Enumeration e = paramDB.elements(); e.hasMoreElements();) {
	    ((AbstractParam)e.nextElement()).clear();
	}
    }

    // help method for loading params
    private int int_param(String name) throws ErrorParam {
	IntParam par = (IntParam)paramDB.get(name);
	if (par != null) {
	    return par.intValue();
	} else {
	    throw new ErrorParam("bug: param "+name+" not found!");
	}
    }

    private double dbl_param(String name) throws ErrorParam {
	FloatParam par = (FloatParam)paramDB.get(name);
	if (par != null) {
	    return par.doubleValue();
	} else {
	    throw new ErrorParam("bug: param "+name+" not found!");
	}   
    }

    private String str_param(String name) throws ErrorParam {
	StringParam par = (StringParam)paramDB.get(name);
	if (par != null) {
	    return par.getValue();
	} else {
	    throw new ErrorParam("bug: param "+name+" not found!");
	}    
    }
 
    void fromDB() throws ErrorParam {
	LeafQuality = dbl_param("LeafQuality");
	Smooth = dbl_param("Smooth");
	Levels = int_param("Levels");
	Ratio = dbl_param("Ratio");
	RatioPower = dbl_param("RatioPower");
	Shape = int_param("Shape");
	BaseSize = dbl_param("BaseSize");
	Flare = dbl_param("Flare");
	Lobes = int_param("Lobes");
	LobeDepth = dbl_param("LobeDepth");
	Leaves = int_param("Leaves");
	LeafShape = str_param("LeafShape");
	LeafScale = dbl_param("LeafScale");
	LeafScaleX = dbl_param("LeafScaleX");
	LeafStemLen = dbl_param("LeafStemLen");
	LeafDistrib = int_param("LeafDistrib");
	LeafBend = dbl_param("LeafBend");
	Scale = dbl_param("Scale");
	ScaleV = dbl_param("ScaleV");
	_0Scale = dbl_param("0Scale"); 
	_0ScaleV = dbl_param("0ScaleV");
	AttractionUp = dbl_param("AttractionUp");
	PruneRatio = dbl_param("PruneRatio");
	PrunePowerLow = dbl_param("PrunePowerLow");
	PrunePowerHigh = dbl_param("PrunePowerHigh");
	PruneWidth = dbl_param("PruneWidth");
	PruneWidthPeak = dbl_param("PruneWidthPeak");
	_0BaseSplits = int_param("0BaseSplits");

	for (int i=0; i<=Math.min(Levels,3); i++) {
	    levelParams[i].fromDB(i==Levels); // i==Levels => leaf level only
	}
    }

    public void prepare() throws ErrorParam {
	if (debug) { verbose=false; }

	// read in parameter values from ParamDB
	fromDB();
   
	if (ignoreVParams) {
	    ScaleV=0;
	    for (int i=1; i<4; i++) {
		LevelParams lp = levelParams[i];
		lp.nCurveV = 0;
		lp.nLengthV = 0;
		lp.nSplitAngleV = 0;
		lp.nRotateV = 0;
		// lp.nBranchDistV = 0;
		if (lp.nDownAngle>0) { lp.nDownAngle=0; }
	    }
	}
	
	// additional params checks
	for (int l=0; l < Math.min(Levels,4); l++) {
	    LevelParams lp = levelParams[l];
	    if (lp.nSegSplits>0 && lp.nSplitAngle==0) {
		throw new ErrorParam("nSplitAngle may not be 0.");
	    }
	}
	         
	// create one random generator for every level
	// so you can develop a tree level by level without
	// influences between the levels
	long l = levelParams[0].initRandom(Seed);
	for (int i=1; i<4; i++) {
	    l = levelParams[i].initRandom(l);
	}

	// create a random generator for myself (used in stem_radius)
	random = new Random(Seed);
    
	// mesh settings
	if (Smooth <= 0.2) {
	    smooth_mesh_level = -1;
	} else {
	    smooth_mesh_level = (int)(Levels*Smooth);
	}
	mesh_quality = Smooth;
    
	// mesh points per cross-section for the levels
	// minima
	levelParams[0].mesh_points = 4;
	levelParams[1].mesh_points = 3;
	levelParams[2].mesh_points = 2;
	levelParams[3].mesh_points = 1;
	// set meshpoints with respect to mesh_quality and Lobes
	if (Lobes>0) {
	    levelParams[0].mesh_points = (int)(Lobes*(Math.pow(2,(int)(1+2.5*mesh_quality))));
	    levelParams[0].mesh_points = 
		Math.max(levelParams[0].mesh_points,(int)(4*(1+2*mesh_quality)));
	}
	for (int i=1; i<4; i++) {
	    levelParams[i].mesh_points = 
		Math.max(3,(int)(levelParams[i].mesh_points*(1+1.5*mesh_quality)));
	}

	// stop generation at some level?
	if (stopLevel>=0 && stopLevel<=Levels) {
	    Levels = stopLevel;
	    Leaves = 0;
	}

	scale_tree = Scale + levelParams[0].random.uniform(-ScaleV,ScaleV);
    }
    
    public double shape_ratio(double ratio) {
	return shape_ratio(ratio,Shape);
    }

    public double shape_ratio(double ratio, int shape) {

	switch (shape) { 
	    //case CONICAL: return 0.2+0.8*ratio;
	    // need real conical shape for lark, fir, etc.
	case CONICAL: return ratio;
	case SPHERICAL: return 0.2+0.8*Math.sin(Math.PI*ratio);
	case HEMISPHERICAL: return 0.2+0.8*Math.sin(0.5*Math.PI*ratio);
	case CYLINDRICAL: return 1.0;
	case TAPERED_CYLINDRICAL: return 0.5+0.5*ratio;
	case FLAME: 
	    return ratio<=0.7? 
		ratio/0.7 : 
		    (1-ratio)/0.3;
	case INVERSE_CONICAL: return 1-0.8*ratio;
	case TEND_FLAME: 
	    return ratio<=0.7? 
		0.5+0.5*ratio/0.7 :
		    0.5+0.5*(1-ratio)/0.3;
	case ENVELOPE:
	    if (ratio<0 || ratio>1) {
		return 0;
	    } else if (ratio<(1-PruneWidthPeak)) {
		return Math.pow(ratio/(1-PruneWidthPeak),PrunePowerHigh);
	    } else {
		return Math.pow((1-ratio)/(1-PruneWidthPeak),PrunePowerLow);
	    }
	    // tested in prepare() default: throw new ErrorParam("Shape must be between 0 and 8");
	}
	return 0; // shouldn't reach here
    }

    public void setParam(String name, String value) throws ErrorParam {
	AbstractParam p = (AbstractParam)paramDB.get(name);
	if (p!=null) {
	    p.setValue(value);
	    if (debug) {
		System.err.println("Params.setParam(): set "+name+" to "+value);
	    }

	} else {
	    throw new ErrorParam("Unknown parameter "+name+"!");
	}
    }

    /*
void Tree::setParams(Paramset &paramset) {
    for (Params::const_iterator pi = params.begin();
	 pi != params.end(); pi++) {

      AbstractParam *param = pi->second;

      // FIXME: warn and set default value if parameter is not in paramset
      // don't warn for additional parameters not in the original model
      if (! param->has_levels) {
	param->set_value(paramset[param->name]);
      } else {
	for (int l=0; l<4; l++) {
	  param.set_value(paramset[toString(l)+param.name.copy(0,param.name.length()-1)]);
	}
      }
    }
}
    */

    public Hashtable getParamGroup(int level, String group) {
	Hashtable result = new Hashtable();
	for (Enumeration e = paramDB.elements(); e.hasMoreElements();) {
	    AbstractParam p = (AbstractParam)e.nextElement();
	    if (p.getLevel() == level && p.getGroup().equals(group)) {
		result.put(p.getName(),p);
	    }
	}
	return result;
    }

    // help methods for createing param-db
    private void int_par(String name, int min, int max, int deflt,
			 String group, String short_desc, String long_desc) {
	paramDB.put(name,new IntParam(name,min,max,deflt,group,AbstractParam.GENERAL,
				      short_desc,long_desc));
    }

    private void int4_par(String name, int min, int max, 
			  int deflt0,int deflt1, int deflt2, int deflt3,
			  String group, String short_desc, String long_desc) {
	int [] deflt = {deflt0,deflt1,deflt2,deflt3};
	for (int i=0; i<4; i++) {
	    name = "" + i + name.substring(1);
	    paramDB.put(name,new IntParam(name,min,max,deflt[i],group,i,short_desc,long_desc));
	}
    }

    private void flt_par(String name, double min, double max, double deflt,
			 String group, String short_desc, String long_desc) {
	paramDB.put(name,new FloatParam(name,min,max,deflt,group,AbstractParam.GENERAL,
					short_desc,long_desc));
    }

    private void flt4_par(String name, double min, double max, 
			  double deflt0, double deflt1, double deflt2, double deflt3,
			  String group, String short_desc, String long_desc) {
	double [] deflt = {deflt0,deflt1,deflt2,deflt3};
	for (int i=0; i<4; i++) {
	    name = "" + i + name.substring(1);
	    paramDB.put(name,new FloatParam(name,min,max,deflt[i],group,i,short_desc,long_desc));
	}
    }

    private void str_par(String name, String deflt,
			 String group, String short_desc, String long_desc) {
	paramDB.put(name,new StringParam(name,deflt,group,AbstractParam.GENERAL,
					 short_desc,long_desc));
    }


    private void register_params() {
	int_par ("Shape",0,8,0,"SHAPE","general tree shape id",
		 "The Shape can be one of: \n"+
		 "0 - conical\n"+
		 "1 - spherical\n"+
		 "2 - hemispherical\n"+
		 "3 - cylindrical\n"+
		 "4 - tapered cylindrical\n"+
		 "5 - flame\n"+
		 "6 - inverse conical\n"+
		 "7 - tend flame\n"+
		 "8 - envelope - use pruning envelope\n"+
		 "(see PruneWidth, PruneWidtPeak, PrunePowerLow, PrunePowerHigh)\n");

	flt_par ("BaseSize",0.0,1.0,0.25,"SHAPE","fractional branchless area at tree base",
		 "BaseSize is the fractional branchless part of the trunk. E.g.\n"+
		 "BaseSize=0   means branches begin on the bottom of the tree,\n"+
		 "BaseSize=0.5 means half of the trunk is branchless,\n"+
		 "BaseSize=1.0 branches grow only from the peak of the trunk.\n");

	flt_par("Scale",0.000001,Double.POSITIVE_INFINITY,10.0,"SHAPE","average tree size in meters",
		"Scale gives the average tree size in meters.\n"+
		"Scale = 10.0, ScaleV = 2.0 means, trees of this species\n"+
		"reach from 8.0 to 12.0 meters.\n"+
		"Note, that the trunk length can be different from the tree size.\n"+
		"(See 0Length and 0LengthV)\n");

	flt_par("ScaleV",0.0,Double.POSITIVE_INFINITY,0.0,"SHAPE","variation of tree size in meters",
		"ScaleV gives the variation range of the tree size in meters.\n"+
		"Scale = 10.0, ScaleV = 2.0 means, trees of this species\n"+
		"reach from 8.0 to 12.0 meters.\n"+
		"(See Scale)\n");

	flt_par("ZScale",0.000001,Double.POSITIVE_INFINITY,1.0,"SHAPE",
		"additional Z-scaling (not used)",
		"ZScale and ZScaleV are not described in the Weber/Penn paper.\n"+
		"so theire meaning is unclear and they aren't used at the moment\n");

	flt_par("ZScaleV",0.0,Double.POSITIVE_INFINITY,0.0,"SHAPE",
		"additional Z-scaling variation (not used)",
		"ZScale and ZScaleV are not described in the Weber/Penn paper.\n"+
		"so theire meaning is unclear and they aren't used at the moment\n");

	int_par("Levels",0,9,3,"SHAPE","levels of recursion",
		"Levels are the levels of recursion when creating the\n"+
		"stems of the tree.\n" +
		"Levels=1 means the tree consist only of the (may be splitting) trunk\n"+
		"Levels=2 the tree consist of the trunk with one level of branches\n"+
		"Levels>4 seldom necesarry, the parameters of the forth level are used\n"+
		"Leaves are considered to be one level above the last stem level.\n");

	flt_par("Ratio",0.000001,Double.POSITIVE_INFINITY,0.05,"TRUNK",
		"trunk radius/length ratio",
		"Ratio gives the radius/length ratio of the trunk.\n"+
		"Ratio=0.05 means the trunk is 1/20 as thick as it is long,\n"+
		"t.e. a 10m long trunk has a base radius of 50cm.\n"+
		"Note, that the real base radius could be greater, when Flare\n"+
		"and/or Lobes are used. (See Flare, Lobes, LobesDepth, RatioPower)\n");

	flt_par("RatioPower",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,1.0,
		"MISC","radius reduction",
		"RatioPower gives a reduction value for the radius of the\n"+
		"substems.\n"+
		"RatioPower=1.0  means the radius decreases linearly with\n"+
		"decreasing stem length\n"+
		"RatioPower=2.0  means it decreases with the second power\n"+
		"RatioPower=0.0  means radius is the same as parent radius\n"+
		"(t.e. it doesn't depend of the length)\n"+
		"RatioPower=-1.0 means the shorter the stem the thicker it is\n"+
		"(radius = parent radius * 1 / length)\n"+
		"Note, that the radius of a stem cannot be greater then\n"+
		"the parent radius at the stem offset. So with negative RatioPower\n"+
		"you cannot create stems thicker than it's parent. Instead you\n"+
		"can use it to make stems thinner, which are longer than it's parent.\n"+
		"(See Ratio)\n");
	
	flt_par("Flare",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0.5,
		"TRUNK","exponential expansion at base of tree",
		"Flare makes the trunk base thicker.\n"+
		"Flare = 0.0 means base radius is used at trunk base\n"+
		"Flare = 1.0 means trunk base is twice as thick as it's base radius\n"+
		"(See Ratio)\n"+
		"Note, that using Lobes make the trunk base thicker too.\n"+
		"(See Lobes, LobeDepth)\n");

	int_par("Lobes",0,Integer.MAX_VALUE,0,"TRUNK",
		"sinusoidal cross-section variation",
		"With Lobes you define how much lobes (this are variations in it's\n"+
		"cross-section) the trunk will have. This isn't supported for \n"+
		"cones output, but for mesh only.\n"+
		"(See LobeDepth too)\n");
	
	flt_par("LobeDepth",0,Double.POSITIVE_INFINITY,0,
		"TRUNK","amplitude of cross-section variation",
		"LobeDepth defines, how deep the lobes of the trunk will be.\n"+
		"This is the amplitude of the sinusoidal cross-section variations.\n"+
		"(See Lobes)\n");
		
	int_par("Leaves",Integer.MIN_VALUE,Integer.MAX_VALUE,0,
		"LEAVES","number of leaves per stem",
		"Leaves gives the maximal number of leaves per stem.\n"+
		"Leaves grow only from stems of the last level. The\n"+
		"actual number of leaves on a stem depending on the\n"+
		"stem offset and length, can be smaller than Leaves.\n"+
		"When Leaves is negativ, the leaves grow in a fan at\n"+
		"the end of the stem.\n");
	
	str_par("LeafShape","0","LEAVES","leaf shape id",
		"LeafShape gives the shape of the leaf. \"0\"\n" +
		"means oval shape. The length and width of the \n"+
		"leaf are given by LeafScale and LeafScaleX.\n"+
		"Other possible values of LeafShape references\n"+
		"to the declarations in arbaro.inc. At the moment\n"+
		"there are:\n"+
		"\"disc\" the standard oval form of a leaf, defined\n"+
		"as a unit circle of radius 0.5m. The real\n"+
		"length and with ar given by the LeafScale \n"+
		"parameters.\n"+
		"\"sphere\" a spherical form, you can use this to \n"+
		"simulate seed on herbs or knots on branches\n"+
		"like in the desert bush. You can use the\n"+
		"sphere shape for needles to, because they\n"+
		"are visible from all sides\n"+
		"\"palm\" a palm leaf, this are two disc halfs put together\n"+
		"with an angle between them. So they are visible\n"+
		"also from the side and the light effects are\n"+
		"more typically, especialy for fan palms seen\n"+
		"from small distances.\n");
	
	flt_par("LeafScale",0.000001,Double.POSITIVE_INFINITY,0.2,
		"LEAVES","leaf length",
		"LeafScale gives the length of the leaf in meters. \n"+
		"The unit leaf is scaled in z-direction (y-direction in Povray)\n"+
		"by this factor. (See LeafShape, LeafScaleX)\n");
	
	flt_par("LeafScaleX",0.000001,Double.POSITIVE_INFINITY,0.5,"LEAVES",
		"fractional leaf width",
		"LeafScaleX gives the fractional width of the leaf\n"+
		"relativly to it's length. So \n"+
		"LeafScaleX=0.5 means the leaf is half as wide as long\n"+
		"LeafScaleX=1.0 means the leaf is a circle\n"+
		"The unit leaf is scaled by LeafScale*LeafScaleX in x- and\n"+
		"y-direction (x- and z-direction in Povray). So the spherical\n"+
		"leaf is transformed to a needle 5cm long and 1mm wide \n"+
		"by LeafScale=0.05 and LeafScaleX=0.02.\n");

	flt_par("LeafBend",0,1,0.3,"LEAVESADD","leaf orientation toward light",
		"With LeafBend you can influence, how much leaves are oriented\n"+
		"outside and upwards. Values near 0.5 are good. For low values the leaves\n"+
		"are oriented to the stem, for high value to the light.\n"+
		"For trees with long leaves like palms you should use lower values.\n");
	
	flt_par("LeafStemLen",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0.5,
		"LEAVESADD","fractional leaf stem length",
		"The length of the leaf stem. For normal trees with many nearly circular\n"+
		"leaves the default value of 0.5 (meaning the stem has half of the length\n"+
		"of the leaf) is quite good. For other trees like palms with long leaves\n"+
		"or some herbs you need a LeafStemLen near 0. Negative stem length is "+
		"allowed for special cases.");

	int_par ("LeafDistrib",0,8,4,"LEAVESADD","leaf distribution",
		 "LeafDistrib determines how leaves are distributed over\n"+
		 "the branches of the last but one stem level. It takes the same\n"+
		 "values like Shape, meaning 3 = even distribution, 0 = most leaves\n"+
		 "outside. Default is 4 (some inside, more outside).");
		
	flt_par("LeafQuality",0.000001,1.0,1.0,"LEAVESADD","leaf quality/leaf count reduction",
		"With a LeafQuality less then 1.0 you can reduce the number of leaves\n"+
		"to improve rendering speed and memory usage. The leaves are scaled\n"+
		"with the same amount to get the same coverage.\n"+
		"For trees in the background of the scene you will use a reduced\n"+
		"LeafQuality around 0.9. Very small values would cause strange results.\n"+
		"(See LeafScale)" );

	flt_par("Smooth",0.0,1.0,0.5,"MISC","smooth value for mesh creation",
		"Higher Smooth values creates meshes with more vertices and\n"+
		"adds normal vectors to them for some or all branching levels.\n"+
		"normally you would specify this value on the command line, but for\n"+
		"some species a special default smooth value could be best\n"+
		"E.g. for shave-grass a low smooth value is best, because this herb\n"+
		"has angular stems.");
		
	flt_par("AttractionUp",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0.0,
		"MISC","upward/downward growth tendency",
		"AttractionUp gives the tendency of stems with level>=2 to grow\n"+
		"upwards (downwards for negative values). A value of 1.0 means\n"+
		"the last segment should point upward. Greater values means \n"+
		"earlier reaching of upward direction. Values of 10 and greater\n"+
		"could cause overcorrection resulting in a snaking oscillation.\n"+
		"The weeping willow has an AttractionUp value of -3.\n");
	
	flt_par("PruneRatio",0.0,1.0,0.0,"PRUNING",
		"fractional effect of pruning",
		"Pruning ratio. A Ratio of 1.0 means all branches are inside\n"+
		"the envelope. 0.0 means no pruning.\n");

	flt_par("PruneWidth",0.0,1.0,0.5,"PRUNING","width of envelope peak",
		"This is the fractional width of the pruning envelope at the\n"+
		"peak. 0.5 means the tree is half as wide as high.\n");

	flt_par("PruneWidthPeak",0.0,1.0,0.5,"PRUNING","position of envelope peak",
		"Fractional height of the envelope peak. 0.5 means upper part\n"+
		"en lower part of the envelope have the same height.\n");

	flt_par("PrunePowerLow",0.0,Double.POSITIVE_INFINITY,0.5,"PRUNING",
		"curvature of envelope",
		"Describes the envelope curve below the peak. A value\n"+
		"of 1 means linear decreasing Higher values means concave,\n"+
		"lower values convex curve.\n");
	
	flt_par("PrunePowerHigh",0.0,Double.POSITIVE_INFINITY,0.5,"PRUNING",
		"curvature of envelope",
		"Describes the envelope curve above the peak. A value\n"+
		"of 1 means linear decreasing Higher values means concave,\n"+
		"lower values convex curve.\n");

	flt_par("0Scale",0.000001,Double.POSITIVE_INFINITY,1.0,
		"TRUNK","extra trunk scaling",
		"0Scale and 0ScaleV makes the trunk thicker. This parameters\n"+
		"exists for the level 0 only. From the Weber/Penn paper it is\n"+
		"not clear, why there are two trunk scaling parameters \n"+
		"0Scale and Ratio. See Ratio, 0ScaleV, Scale, ScaleV.\n"+
		"In this implementation 0Scale does not influence the trunk base radius\n"+
		"but is applied finally to the stem_radius formular. Thus the\n"+
		"trunk radius could be influenced independently from the\n"+
		"Ratio/RatioPower parameters and the periodic tapering\n"+
		"could be scaled, that the sections are elongated spheres.\n");
	
	flt_par("0ScaleV",0.0,Double.POSITIVE_INFINITY,0.0,"TRUNK",
		"variation for extra trunk scaling",
		"0Scale and 0ScaleV makes the trunk thicker. This parameters\n"+
		"exists for the level 0 only. From the Weber/Penn paper it is\n"+
		"not clear, why there are two trunk scaling parameters\n"+
		"0Scale and Ratio. See Ratio, 0ScaleV, Scale, ScaleV.\n"+
		"In this implementation 0ScaleV is used to perturb the\n"+
		"mesh of the trunk. But use with care, because the mesh\n"+
		"could get fissures for too big values.\n");
	
	int_par("0BaseSplits",0,Integer.MAX_VALUE,0,"MISC",
		"stem splits at base of trunk",
		"BaseSplit determines how many clones are created at\n"+
		"the top of the first trunk segment. So with BaseSplits=2\n"+
		"you get a trunk splitting into three parts. Other then\n"+
		"with 0SegSplits the clones are evenly distributed over\n"+
		"the 360°. So, if you want to use splitting, you should\n"+
		"use BaseSplits for the first splitting to get a circular\n"+
		"stem distribution (seen from top).\n");
	
	flt4_par("nLength",0.0000001,Double.POSITIVE_INFINITY,1.0,0.5,0.5,0.5,
		 "LENTAPER","fractional trunk scaling",
		 "0Length and 0LengthV give the fractional length of the\n"+
		 "trunk. So with Scale=10 and 0Length=0.8 the length of the\n"+
		 "trunk will be 8m. Dont' confuse the height of the tree with\n"+
		 "the length of the trunk here.\n"+
		 "nLength and nLengthV give the fractional length of a stem\n"+
		 "relating to the length of it's parent length\n");
	
	flt4_par("nLengthV",0.0,Double.POSITIVE_INFINITY,0.0,0.0,0.0,0.0,
		 "LENTAPER","variation of fractional trunk scaling",
		 "nLengthV gives the variation for nLength.\n");
	
	flt4_par("nTaper",0.0,2.99999999,1.0,1.0,1.0,1.0,
		 "LENTAPER","cross-section scaling",
		 "nTaper gives the tapering of the stem along its length.\n"+
		 "0 - non-tapering cylinder\n"+
		 "1 - taper to a point (cone)\n"+
		 "2 - taper to a spherical end\n"+
		 "3 - periodic tapering (concatenated spheres)\n"+
		 "You can use fractional values, to get intermediate results.\n");
	
	flt4_par("nSegSplits",0,Double.POSITIVE_INFINITY,0,0,0,0,
		 "SPLITTING","stem splits per segment",
		 "nSegSplits determines how much splits per segment occures.\n"+
		 "Normally you would use a value between 0.0 and 1.0. A value of\n"+
		 "0.5 means a split at every second segment. If you use splitting\n"+
		 "for the trunk you should use 0BaseSplits for the first split, \n"+
		 "otherwise the tree will tend to one side.");
	
	flt4_par("nSplitAngle",0,180,0,0,0,0,"SPLITTING",
		 "splitting angle",
		 "nSplitAngle is the vertical splitting angle. A horizontal diverging\n"+
		 "angle will be added too, but this one you cannot influence with parameters.\n"+
		 "The declination of the splitting branches won't exceed the splitting angle.\n");

	flt4_par("nSplitAngleV",0,180,0,0,0,0,"SPLITTING",
		 "splitting angle variation",
		 "This is the variation of the splitting angle. See nSplitAngle.\n");
	
	int4_par("nCurveRes",1,Integer.MAX_VALUE,3,3,1,1,
		 "CURVATURE","curvature resolution",
		 "nCurveRes determines how many segments the branches consist of.\n"+
		 "Normally you will use higher values for the first levels, and low\n"+
		 "values for the higher levels.\n");
	
	flt4_par("nCurve",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0,0,0,0,
		 "CURVATURE","curving angle",
		 "This is the angle the branches are declined over theire whole length.\n"+
		 "If nCurveBack is used, the curving angle is distributed only over the\n"+
		 "first half of the stem.\n");
	
	flt4_par("nCurveV",-90,Double.POSITIVE_INFINITY,0,0,0,0,
		 "CURVATURE","curving angle variation",
		 "This is the variation of the curving angle. See nCurve, nCurveBack.\n");
	
	flt4_par("nCurveBack",Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,0,0,0,0,
		 "CURVATURE","curving angle upper stem half",
		 "Using nCurveBack you can give the stem an S-like shape.\n"+
		 "The first half of the stem the nCurve value is apllied.\n"+
		 "The second half the nCurveBack value.\n"+
		 "It's also possible to give both parameter the same sign to\n"+
		 "get different curving over the stem length, instead of a S-shape\n");

	flt4_par("nDownAngle",-179.9999999,179.999999,0,30,30,30,
		 "BRANCHING","angle from parent",
		 "nDownAngle is the angle between a stem and it's parent.\n");
	
	flt4_par("nDownAngleV",-179.9999999,179.9999999,0,0,0,0,
		 "BRANCHING","down angle variation",
		 "This is the variation of the downangle. See nDownAngle.\n"+
		 "Using a negative value, the nDownAngleV is variated over the\n"+
		 "length of the stem, so that the lower branches have a bigger\n"+
		 "downangle then the higher branches.\n");

	flt4_par("nRotate",-360,360,0,120,120,120,
		 "BRANCHING","spirangling angle",
		 "This is the angle, the branches are rotating around the parent\n"+
		 "If nRotate is negative the branches are located on alternating\n"+
		 "sides of the parent.\n");

	flt4_par("nRotateV",-360,360,0,0,0,0,
		 "BRANCHING","spiraling angle variation",
		 "This is the variation of nRotate.\n");

	int4_par("nBranches",0,Integer.MAX_VALUE,1,10,5,5,
		 "BRANCHING","number of branches",
		 "Maximal number of branches on a parent stem.\n"+
		 "The number of branches are reduced proportional to the\n"+
		 "length of theire parent.\n");

	flt4_par("nBranchDist",0,1,0,1,1,1,
		 "BRANCHING","branch distribution along the segment",
		 "This is an additional parameter of Arbaro, it influences the\n"+
		 "distribution of branches over a segment of the parent stem.\n"+
		 "With 1.0 you get evenly distribution of branches like in the\n"+
		 "original model. With 0.0 all branches grow from the segments\n"+
		 "base like for conifers.\n");

	/*	flt4_par("nBranchDistV",0,1,0,0.5,0.5,0.5,
		 "ADDBRANCH","branch distribution variation",
		 "This is the variation of branch distribution.\n"+
		 "A value of 0.0 means evenly distribution of branches over\n"+
		 "the parent segment. With a value of 1.0 a branch can grow\n"+
		 "from any point between the preceeding and the following branch.\n"); */
    }

    public void readFromCfg(InputStream is) throws Exception {
	CfgTreeParser parser = new CfgTreeParser();
	parser.parse(is,this);
    }

    public void readFromXML(InputStream is) throws ErrorParam {
	try {
	    XMLTreeParser parser = new XMLTreeParser();
	    parser.parse(new InputSource(is),this);
	} catch (Exception e) {
	    throw new ErrorParam(e.getMessage());
	}
    }

    public AbstractParam getParam(String parname) {
	return (AbstractParam)paramDB.get(parname);
    }

    public void addChangeListener(ChangeListener l) {
	listenerList.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
	listenerList.remove(ChangeListener.class, l);
    }

    protected void fireStateChanged() {
	Object [] listeners = listenerList.getListenerList();
	for (int i = listeners.length -2; i>=0; i-=2) {
	    if (listeners[i] == ChangeListener.class) {
		if (changeEvent == null) {
		    changeEvent = new ChangeEvent(this);
		}
		((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
	    }
	}
    }
};
























