//  #**************************************************************************
//  #
//  #    $Id$  - the tree creation and render dialog
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
import javax.swing.border.BevelBorder;
//import javax.swing.event.*;

import net.sourceforge.arbaro.tree.Tree;
import net.sourceforge.arbaro.params.*;

public class PovDialog {
	final static int INTERVAL = 500; // 0.5 sec
	// static Tree tree;
	JFrame frame;
	Config config;

	JPanel mainPanel;
	Progressbar progressbar;
	
	Tree tree;

	File treefile = null;
	JFileChooser fileChooser;
	JFileChooser sceneFileChooser;
	JFileChooser renderFileChooser;
	JCheckBox sceneCheckbox;
	JCheckBox renderCheckbox;
	JTextField seedField = new JTextField(6);
	JTextField smoothField = new JTextField(6);
	JTextField fileField;
	JTextField sceneFileField;
	JTextField renderFileField;
	JButton selectSceneFile;
	JButton selectRenderFile;
	JRadioButton meshes;
	Timer timer;
//	ProgressMonitor progressMonitor;
	TreeCreationTask treeCreationTask;
	JButton startButton;
	JButton cancelButton;
	
	public PovDialog(JFrame parent, Tree tr, Config cfg) {
		
		tree = tr;
		config = cfg;
		
		frame = new JFrame("Create tree and write to POVRay file");
		frame.setIconImage(parent.getIconImage());
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// FIXME: make this filechoosers static?
		fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/pov"));
		sceneFileChooser = new JFileChooser();
		sceneFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/pov"));
		renderFileChooser = new JFileChooser();
		renderFileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")+"/pov"));
		
		timer = new Timer(INTERVAL, new TimerListener());
		treeCreationTask = new TreeCreationTask(config);
		
		createGUI();
		frame.setVisible(true);
	}
	
	void createGUI() {
		JPanel panel = new JPanel();
		
		// create GridBagLayout
		GridBagLayout grid = new GridBagLayout();
		panel.setLayout(grid);
		//panel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
		panel.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createRaisedBevelBorder(),
				BorderFactory.createEmptyBorder(30,30,30,30)));
		
		// Constraints for the left labels
		GridBagConstraints clabel = new GridBagConstraints();
		clabel.gridx = 0;
		clabel.anchor = GridBagConstraints.WEST;
		
		// Constraint for the input fields
		GridBagConstraints ctext = new GridBagConstraints();
		ctext.gridx = 1;
		ctext.ipady = 4;
		ctext.anchor = GridBagConstraints.WEST;
		ctext.insets = new Insets(1,5,1,5);
		
		// Constraint for the choose buttons
		GridBagConstraints cbutton = new GridBagConstraints();
		cbutton.gridx = 2;
		cbutton.anchor = GridBagConstraints.WEST;
		
		// INC file input 
		JLabel label = new JLabel("POV Include file:");
		clabel.gridy = 0;
		grid.setConstraints(label,clabel);
		panel.add(label);
		
		fileField = new JTextField(30);
		fileField.setText(fileChooser.getCurrentDirectory().getPath()
				+"/"+tree.getSpecies()+".inc");
		fileField.setMinimumSize(new Dimension(250,19));
		ctext.gridy = 0;
		grid.setConstraints(fileField,ctext);
		panel.add(fileField);
		
		JButton selectFile = new JButton("Choose...");
		selectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int returnVal = fileChooser.showSaveDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					fileField.setText(fileChooser.getSelectedFile().getPath());
				}
			}
		});
		cbutton.gridy = 0;
		grid.setConstraints(selectFile,cbutton);
		panel.add(selectFile);
		
		// POV file input 
		sceneCheckbox = new JCheckBox("POV Scene file:");
		sceneCheckbox.setSelected(false);
		sceneCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				sceneFileField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				selectSceneFile.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				renderCheckbox.setSelected(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		clabel.gridy = 1;
		grid.setConstraints(sceneCheckbox,clabel);
		panel.add(sceneCheckbox);
		
		sceneFileField = new JTextField(30);
		sceneFileField.setEnabled(false);
		sceneFileField.setText(sceneFileChooser.getCurrentDirectory().getPath()
				+"/"+tree.getSpecies()+".pov");
		sceneFileField.setMinimumSize(new Dimension(250,19));
		ctext.gridy = 1;
		grid.setConstraints(sceneFileField,ctext);
		panel.add(sceneFileField);
		
		selectSceneFile = new JButton("Choose...");
		selectSceneFile.setEnabled(false);
		selectSceneFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int returnVal = sceneFileChooser.showSaveDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					sceneFileField.setText(sceneFileChooser.getSelectedFile().getPath());
				}
			}
		});
		cbutton.gridy = 1;
		grid.setConstraints(selectSceneFile,cbutton);
		panel.add(selectSceneFile);
		
		// rendering with POVRay
		renderCheckbox = new JCheckBox("Render scene to:");
		renderCheckbox.setSelected(false);
		renderCheckbox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				renderFileField.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
				selectRenderFile.setEnabled(e.getStateChange() == ItemEvent.SELECTED);
			}
		});
		clabel.gridy = 2;
		grid.setConstraints(renderCheckbox,clabel);
		panel.add(renderCheckbox);
		
		renderFileField = new JTextField(30);
		renderFileField.setEnabled(false);
		renderFileField.setText(renderFileChooser.getCurrentDirectory().getPath()
				+"/"+tree.getSpecies()+".png");
		renderFileField.setMinimumSize(new Dimension(250,19));
		ctext.gridy = 2;
		grid.setConstraints(renderFileField,ctext);
		panel.add(renderFileField);
		
		selectRenderFile = new JButton("Choose...");
		selectRenderFile.setEnabled(false);
		selectRenderFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				int returnVal = renderFileChooser.showSaveDialog(frame);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					renderFileField.setText(renderFileChooser.getSelectedFile().getPath());
				}
			}
		});
		cbutton.gridy = 2;
		grid.setConstraints(selectRenderFile,cbutton);
		panel.add(selectRenderFile);
		
		// random seed
		label = new JLabel("Seed:");
		clabel.gridy = 3;
		grid.setConstraints(label,clabel);
		panel.add(label);
		
		seedField.setText(new Integer(tree.getSeed()).toString());
		seedField.setMinimumSize(new Dimension(80,19));
		ctext.gridy = 3;
		grid.setConstraints(seedField,ctext);
		panel.add(seedField);
		
		// output type
		label = new JLabel("Output:");
		clabel.gridy = 4;
		grid.setConstraints(label,clabel);
		panel.add(label);
		
		JRadioButton primitives = new JRadioButton("Primitives");
		primitives.setActionCommand("primitives");
		meshes = new JRadioButton("Meshes");
		meshes.setActionCommand("meshes");
		if (tree.params.output==Params.CONES) primitives.setSelected(true);
		else meshes.setSelected(true);
		ActionListener alisten = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("meshes")) {
					smoothField.setEnabled(true);
					tree.setOutput(Params.MESH);
				} else {
					smoothField.setEnabled(false);
					tree.setOutput(Params.CONES);
				}
			}
		};
		meshes.addActionListener(alisten);
		primitives.addActionListener(alisten);
		ButtonGroup group = new ButtonGroup();
		group.add(primitives);
		group.add(meshes);
		
		ctext.gridy = 4;
		grid.setConstraints(primitives,ctext);
		panel.add(primitives);
		ctext.gridy = 5;
		grid.setConstraints(meshes,ctext);
		panel.add(meshes);
		
		// smooth value
		label = new JLabel("Smooth value:");
		clabel.gridy = 6;
		grid.setConstraints(label,clabel);
		panel.add(label);
		
		smoothField.setText(new Double(tree.getSmooth()).toString());
		smoothField.setMinimumSize(new Dimension(80,19));
		ctext.gridy = 6;
		grid.setConstraints(smoothField,ctext);
		panel.add(smoothField);
		
		// buttons
		startButton = new JButton("Start");
		startButton.addActionListener(new StartButtonListener());
		
		cancelButton = new JButton("Close");
		cancelButton.addActionListener(new CancelButtonListener());
		
		JPanel buttons = new JPanel();
		buttons.add(startButton);
		buttons.add(cancelButton);
		
		cbutton.gridx = 1;
		cbutton.gridy = 7;
		cbutton.anchor = GridBagConstraints.CENTER;
		grid.setConstraints(buttons,cbutton);
		panel.add(buttons);
		
    	// add panel to content pane
    	Container contentPane = frame.getContentPane(); 
    	contentPane.add(panel,BorderLayout.CENTER);
    	
    	// add status line
    	progressbar = new Progressbar();
    	progressbar.setVisible(false);
    	contentPane.add(progressbar,BorderLayout.PAGE_END);

		frame.pack();
	}
	
	class StartButtonListener implements ActionListener {
		// creates the tree and writes to file when button pressed
		public void actionPerformed(ActionEvent e) {
			// System.err.println("seed "+seedField.getText());
			// System.err.println("smooth "+smoothField.getText());
			// System.err.println("0Branches "+tree.params.getParam("0Branches").getValue());
			
			// get seed, output parameters
			tree.setSeed(Integer.parseInt(seedField.getText()));
			tree.setSmooth(Double.parseDouble(smoothField.getText()));
			
			// setup progress dialog
//			progressMonitor = new ProgressMonitor(frame,"","",0,100);
//			progressMonitor.setProgress(0);
//			progressMonitor.setMillisToDecideToPopup(1*INTERVAL);
			
			progressbar.setVisible(true);
			
			startButton.setEnabled(false);
			cancelButton.setText("Cancel");
			
			// start tree creation
			System.err.println("start creating tree and writing to "+fileField.getText());
			
			File incfile = new File(fileField.getText());
			File povfile = null;
			if (sceneCheckbox.isSelected()) povfile = new File(sceneFileField.getText());
			String imgFilename = null;
			if (renderCheckbox.isSelected()) imgFilename = renderFileField.getText();
			treeCreationTask.start(tree,incfile,povfile,imgFilename); //fileChooser.getSelectedFile());
			timer.start();
		}
	}
	
	class CancelButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e){
			if (treeCreationTask.notActive())
				frame.dispose();
			else {
				treeCreationTask.stop();
//				progressMonitor.close();
				Toolkit.getDefaultToolkit().beep();
				timer.stop();
				startButton.setEnabled(true);
				startButton.setText("Restart");
				cancelButton.setText("Close");
				progressbar.setVisible(false);
			}
		}
	}
	
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			// System.err.println("timer event "+createTreeTask.getProgress());
			if (/*progressMonitor.isCanceled() ||*/ treeCreationTask.notActive()) 
			{
				treeCreationTask.stop();
//				progressMonitor.close();
				progressbar.setProgress(100);
				progressbar.setNote("Ready");
				Toolkit.getDefaultToolkit().beep();
				timer.stop();
				startButton.setEnabled(true);
				startButton.setText("Restart");
				cancelButton.setText("Close");
			} else {
//				progressMonitor.setProgress((int)(100*createTreeTask.getProgress()));
//				progressMonitor.setNote(createTreeTask.getProgressMsg());
				
				progressbar.setProgress(treeCreationTask.getProgress());
				progressbar.setNote(treeCreationTask.getProgressMsg());
			}
		}
	}
}

class Progressbar extends JPanel {
	JLabel label;
	JProgressBar progressbar;
	
	public Progressbar() {
		super(new BorderLayout());
		((BorderLayout)getLayout()).setHgap(20);
		
		setBorder(BorderFactory.createEmptyBorder(10,5,5,5));
		
		label = new JLabel("Creating tree structure");
    	Font font = label.getFont().deriveFont(Font.PLAIN,12);
    	label.setFont(font);
		add(label,BorderLayout.WEST);
    	
		progressbar = new JProgressBar(0,100);
		progressbar.setValue(-1);
		progressbar.setStringPainted(true);
		progressbar.setString("");
		progressbar.setIndeterminate(true);
		add(progressbar,BorderLayout.CENTER);
	}
	
	public void setProgress(int val) {
//		System.err.println("setProg "+val);
		if (val<=0) {
			progressbar.setIndeterminate(true);
		} else {
			progressbar.setIndeterminate(false);
			progressbar.setValue(val);
			progressbar.setString(""+val+"%");
		}
	}
	
	public void setNote(String note) {
		label.setText(note);
	}
}

/* this class actually creates the tree and saves it to a POV file
 */
class TreeCreationTask {
	Tree tmptree;
	PrintWriter writer;
	File scene_file = null;
	PrintWriter scenewriter = null;
	String renderFilename = null;
	boolean isNotActive;
	String povrayexe;
	
	final class TreeWorker extends SwingWorker {
		public Object construct() {
			return new DoTask();
		}
		
		public void finished() {
			// may be this should be done in DoTask instead?
			isNotActive = true;
		}
	};
	
	TreeWorker worker;
	
	public TreeCreationTask(Config config) {
		povrayexe = config.getProperty("povray.executable");
		if (povrayexe == null) {
			System.err.println("Warning: Povray executable not set up, trying "+
			"\"povray\" without a directory");
			povrayexe = "povray";
		}
		isNotActive=true;
	};
	
	public void start(Tree tree, File outFile, File sceneFile, String imgFilename) {
		// create new Tree copying the parameters of tree
		try {
			tmptree = new Tree(tree);
			tmptree.params.verbose=false;
			
			writer = new PrintWriter(new FileWriter(outFile)); 
			if (sceneFile != null) {
				scene_file = sceneFile;
				scenewriter = new PrintWriter(new FileWriter(sceneFile)); 
			}
			renderFilename = imgFilename;
			
			isNotActive = false;
			
			worker = new TreeWorker();
			worker.start();
			
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace(System.err);
		}
	}
	
	int getProgress() {
		return tmptree.getProgress().getPercent();
	}
	
	String getProgressMsg() {
		return tmptree.getProgress().getPhase();
	}
	
	void stop() {
		if (worker != null) {
			System.err.println("stop tree creation...");
			worker.interrupt();
			isNotActive = true;
			worker = null;
		}
	}
	
	boolean notActive() {
		return isNotActive;
	}
	
	class DoTask {
		void render() {
			try {
				
				String [] povcmd = { povrayexe,
						"+L"+scene_file.getParent(),
						"+w400","+h600",
						"+o"+renderFilename,
						scene_file.getPath()};
				System.err.println("rendering with \""+povrayexe+"\"...");
				Process povProc = Runtime.getRuntime().exec(povcmd);
				BufferedReader pov_in = new BufferedReader(
						new InputStreamReader(povProc.getErrorStream()));
				
				String str;
				while ((str = pov_in.readLine()) != null) {
					System.err.println(str);
				}
				
			} catch (Exception e) {
				System.err.println(e);
				e.printStackTrace(System.err);
			}
		}
		DoTask() {
			try {
				tmptree.make();
				tmptree.output(writer);
				if (scenewriter != null) tmptree.outputScene(scenewriter);
				if (renderFilename != null && renderFilename.length()>0) {
					tmptree.getProgress().beginPhase("Rendering tree",-1);
					render();
				}
			} catch (Exception err) {
				System.err.println(err);
				err.printStackTrace(System.err);
			}
		}
	};
}









