//  #**************************************************************************
//  #
//  #    $Id$  - the Main window of the GUI
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

package net.sourceforge.arbaro.gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;    
import javax.swing.event.*;

import java.util.TreeMap;
import java.util.Iterator;
import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;    

public class ParamFrame {
    // static Tree tree;
    JFrame frame;
    JPanel mainPanel;
    JTabbedPane tabs;
    Tree tree;
    File treefile = null;
    JFileChooser fileChooser;
    SpeciesField species;
    Component lastFocused = null;
    boolean modified = false;

    public ParamFrame() {
	// create tree with paramDB
	tree = new Tree();

	frame = new JFrame("Arbaro");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.addWindowListener(new WindowAdapter() {
		public void windowClosing(WindowEvent e) {
		     if (! shouldSave()) return;
		     frame.dispose();
		}
	    });

	fileChooser = new JFileChooser();
	fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/trees"));
	

	createGUI();
	frame.setVisible(true);
    }

    void createGUI() {
	createMenuBar();

	// create Tabset
	tabs = new JTabbedPane();

	// add tab for general params
	JPanel panel1 = new JPanel();
	panel1.setLayout(new GridLayout(2,3,20,20));
	panel1.setBorder(BorderFactory.createEmptyBorder(10,30,30,30));
	addParamGroupWidget("SHAPE","Tree shape",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("TRUNK","Trunk radius",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("LEAVES","Leaves",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("LEAVESADD","Additional leaf parameters",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("PRUNING","Pruning",AbstractParam.GENERAL,panel1);
	addParamGroupWidget("MISC","Miscellaneous parameters",AbstractParam.GENERAL,panel1);

	JPanel speciesPanel = new JPanel();
	speciesPanel.setBorder(BorderFactory.createEmptyBorder(20,30,10,10));
	speciesPanel.setLayout(new BoxLayout(speciesPanel,BoxLayout.X_AXIS));
	speciesPanel.add(new JLabel("Species:"));
	speciesPanel.add(Box.createRigidArea(new Dimension(5,0)));
	species = new SpeciesField(20,tree);
	//species.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
	Dimension dim = species.getPreferredSize();
	species.setMaximumSize(new Dimension((int)dim.getWidth()+5,(int)dim.getHeight()+5));
	speciesPanel.add(species);

	JPanel panel2 = new JPanel();
	panel2.setLayout(new BorderLayout());

	panel2.add(speciesPanel,BorderLayout.NORTH);
	panel2.add(panel1,BorderLayout.CENTER);

	tabs.addTab("General", null, panel2, "General parameters");
	tabs.setSelectedIndex(0);

	// add tabs for level params
	for (int i=0; i<4; i++) {
	    JPanel panel = new JPanel();
	    panel.setLayout(new GridLayout(2,3,20,20));
	    panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
	    tabs.addTab("Level "+i, null, panel, "Parameters for branching level "+i);
	
	    addParamGroupWidget("LENTAPER","Length and tapering",i,panel);
	    addParamGroupWidget("CURVATURE","Curvature",i,panel);
	    addParamGroupWidget("SPLITTING","Splitting",i,panel);
	    addParamGroupWidget("BRANCHING","Branching",i,panel);
	    addParamGroupWidget("ADDBRANCH","Branch distribution",i,panel);
	}

	// add more
	frame.getContentPane().add(tabs);

        frame.pack();
    }

    void createMenuBar() {
	JMenuBar menubar;
	JMenu menu;
	JMenuItem item;
	
	/**** file menu ***/

	menubar=new JMenuBar();
	menu = new JMenu("File");
	menu.setMnemonic('F');

	// File new
	item = new JMenuItem("New");
	item.setMnemonic('N');
	item.addActionListener(new FileNewListener());
	menu.add(item);

	// File open
	item = new JMenuItem("Open...");
	item.setMnemonic('O');
	item.addActionListener(new FileOpenListener());
	menu.add(item);

	// File save
	item = new JMenuItem("Save");
	item.setMnemonic('S');
	item.addActionListener(new FileSaveListener());
	menu.add(item);

	// File save as
	item = new JMenuItem("Save as...");
	item.setMnemonic('A');
	item.addActionListener(new FileSaveAsListener());
	menu.add(item);
	
	menu.add(new JSeparator());

	// Save POV file
	item = new JMenuItem("Create tree...");
	item.setMnemonic('C');
	item.addActionListener(new CreateTreeListener());
	menu.add(item);

	menu.add(new JSeparator());

	// Quit
	item = new JMenuItem("Quit");
	item.setMnemonic('Q');
	item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // ask if values should be saved
		    if (! shouldSave()) return;
		    frame.dispose();
		}
	    });
	menu.add(item);
	
	menubar.add(menu);

	/**** help menu ****/
	menu = new JMenu("Help");
	menu.setMnemonic('H');

	// help paramter
	item = new JMenuItem("Parameter");
	item.setMnemonic('P');
	item.addActionListener(new HelpParameterListener());
	menu.add(item);	

	// help about
	item = new JMenuItem("About Arbaro");
	item.setMnemonic('A');
	item.addActionListener(new HelpAboutListener());
	menu.add(item);

	menubar.add(menu);

	frame.setJMenuBar(menubar);
    }

    void addParamGroupWidget(String group, String groupName, int level, JPanel parent) {
	TreeMap params = new TreeMap(tree.getParamGroup(level,group));
	
	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(params.size(),2,10,2));

	for (Iterator e=params.values().iterator();e.hasNext();) {
	    AbstractParam p = (AbstractParam)e.next();
	    panel.add(new JLabel(p.getName()));
	    panel.add(new ParamField(this,6,p));
	}

	JPanel panel1 = new JPanel();
	panel1.setBorder(BorderFactory.createCompoundBorder(
                      BorderFactory.createTitledBorder(groupName),
                      BorderFactory.createEmptyBorder(5,10,5,10)));
	panel1.add(panel);
	parent.add(panel1);
	//panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    void setModified(boolean mod) {
	modified = mod;
    }

    class FileNewListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // ask if should save when modified...
	    if (! shouldSave()) return;

	    tree.clearParams();
	    tree.setSpecies("default");
	    modified = false;
	}
    };

    class FileOpenListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // ask if should saved
	    if (! shouldSave()) return;

	    int returnVal = fileChooser.showOpenDialog(frame);
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		System.err.println("opening file: " +
				   fileChooser.getSelectedFile().getName());
		try {
		    tree.clearParams();
		    treefile = fileChooser.getSelectedFile();
		    // read parameters
		    tree.readFromXML(new FileInputStream(treefile));
		    modified = false;
		} catch (ErrorParam err) {
		    JOptionPane.showMessageDialog(frame,err.getMessage(),
						  "Parameter Error",
						  JOptionPane.ERROR_MESSAGE);
		} catch (FileNotFoundException err) {
		    JOptionPane.showMessageDialog(frame,err.getMessage(),
						  "File not found",
						  JOptionPane.ERROR_MESSAGE);
		}
	    }	
	}
    };

    boolean shouldSave() {
	if (modified) {
	    int result = JOptionPane.showConfirmDialog(frame,
		   "Some parameters are modified. Should the tree definition be saved?",
						       "Tree definition modified",
						       JOptionPane.YES_NO_CANCEL_OPTION,
						       JOptionPane.QUESTION_MESSAGE);  
	    if (result == JOptionPane.YES_OPTION) {
		if (treefile != null) {
		    return fileSave();
		} else {
		    return fileSaveAs();
		}
	    }
	    return (result != JOptionPane.CANCEL_OPTION);
	} else
	    return true;
    }


    boolean fileSaveAs() {
	int returnVal = fileChooser.showSaveDialog(frame);
	if(returnVal == JFileChooser.APPROVE_OPTION) {
	    treefile = fileChooser.getSelectedFile();
	    return fileSave();
	}
	return false;
    }

    boolean fileSave() {
	System.err.println("saving to file: " +
			       fileChooser.getSelectedFile().getName());
	try {
	    PrintWriter out = new PrintWriter(new FileWriter(treefile));
	    tree.toXML(out);
	    modified = false;
	    return true;
	} catch (ErrorParam err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "Parameter Error",
					  JOptionPane.ERROR_MESSAGE);
	} catch (FileNotFoundException err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "File not found",
					      JOptionPane.ERROR_MESSAGE);
	}
	catch (IOException err) {
	    JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "Output error",
					  JOptionPane.ERROR_MESSAGE);
	}
	return false;
    }	

    class FileSaveListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (treefile != null) {
		fileSave();
	    } else {
		fileSaveAs();
	    }
	}
    };

    class FileSaveAsListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    fileSaveAs();
	}
    }

    class CreateTreeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    new PovDialog(tree);
	}
    }

    class HelpParameterListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    // Component c = frame.getMostRecentFocusOwner();
	    // System.err.println(c.getClass());
	    if (lastFocused.getClass() == ParamField.class) {
		JOptionPane.showMessageDialog(frame, 
					      ((ParamField)lastFocused).param.getLongDesc());
	    }
	}
    }

    class HelpAboutListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    JOptionPane.showMessageDialog(frame, net.sourceforge.arbaro.arbaro.progname);
	}
    }
}

class SpeciesField extends JTextField {

    Tree tree;

    public SpeciesField(int width, Tree tr) {
	super(width);

	tree = tr;
	setText(tree.getSpecies());
	setToolTipText("tree species name, used for object names in the POVRay file");

	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tree.setSpecies(getText());
		}
	    });
	addFocusListener(new FocusAdapter() {
		public void focusLost(FocusEvent e) {
		    tree.setSpecies(getText());
		}
	    });
	// add ChangeListener to set species name if changed in the tree
	tree.params.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    setText(tree.getSpecies());
		}
	    });
    }
}

class ParamField extends JTextField {

    ParamFrame parent;
    AbstractParam param;

    public ParamField(ParamFrame pnt, int width, AbstractParam par) {
	super(width);
	parent = pnt;
	param = par;

	setText(param.getDefaultValue());
	// ? param.setValue(getText());
	setToolTipText(param.getShortDesc());

	if (param.getClass() != StringParam.class) {
	    setHorizontalAlignment(JTextField.RIGHT);
	}
	addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    setParamValue();
		}
	    });
	addFocusListener(new FocusAdapter() {
		public void focusLost(FocusEvent e) {
		    setParamValue();
		}
		public void focusGained(FocusEvent e) {
		    parent.lastFocused = (Component)e.getSource();
		}
	    });
	param.addChangeListener(new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
		    if (!param.empty()) {
			setText(param.getValue());
		    } else {
			setText(param.getDefaultValue());
		    }
		}
	    });
				
    }


    void setParamValue() {
	try {
	    param.setValue(getText());
	    setText(param.getValue());
	    parent.setModified(true);
	} catch (ErrorParam err) {
	    JOptionPane.showMessageDialog(getParent(),err.getMessage(),"Parameter Error",
					  JOptionPane.ERROR_MESSAGE);
	    // set focus to this field
	    requestFocusInWindow();
	}
    }
}






