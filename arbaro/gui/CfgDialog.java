//  #**************************************************************************
//  #
//  #    $Id$  - the config dialog
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

//import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;    
//import javax.swing.event.*;

public class CfgDialog {
    JFrame frame;
    JPanel mainPanel;
    JFileChooser fileChooser;
    JTextField fileField;
    Config config;

    public CfgDialog(JFrame parent, Config cfg) {

	config = cfg;
	frame = new JFrame("Arbaro setup");
	frame.setIconImage(parent.getIconImage());

	fileChooser = new JFileChooser();
	// fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")"));

	createGUI();
	frame.setVisible(true);
    }

    void createGUI() {
	mainPanel = new JPanel();
	//panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
	GridBagLayout grid = new GridBagLayout();
	mainPanel.setLayout(grid);
	mainPanel.setBorder(BorderFactory.createEmptyBorder(30,30,30,30));
	
        GridBagConstraints clabel = new GridBagConstraints();
	clabel.gridx = 0;
	//	clabel.ipadx = 5;
	clabel.anchor = GridBagConstraints.WEST;
	GridBagConstraints ctext = new GridBagConstraints();
	ctext.gridx = 1;
	// ctext.ipadx = 5;
	ctext.ipady = 4;
	ctext.anchor = GridBagConstraints.WEST;
	ctext.insets = new Insets(1,5,1,5);
	GridBagConstraints cbutton = new GridBagConstraints();
	cbutton.gridx = 2;
	cbutton.anchor = GridBagConstraints.WEST;

	// INC file input 
	JLabel label = new JLabel("POVRay executable:");
	clabel.gridy = 0;
	grid.setConstraints(label,clabel);
	mainPanel.add(label);

	fileField = new JTextField(30);
	fileField.setText(config.getProperty("povray.executable","povray"));

	ctext.gridy = 0;
	grid.setConstraints(fileField,ctext);
	mainPanel.add(fileField);

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
	mainPanel.add(selectFile);


	// buttons
	JButton okButton = new JButton("OK");
	okButton.addActionListener(new OKButtonListener());

	JButton cancelButton = new JButton("Cancel");
	cancelButton.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    frame.dispose();
		}
	    });

	JPanel buttons = new JPanel();
	buttons.add(okButton);
	buttons.add(cancelButton);

	cbutton.gridx = 1;
	cbutton.gridy = 7;
	cbutton.anchor = GridBagConstraints.CENTER;
	grid.setConstraints(buttons,cbutton);
	mainPanel.add(buttons);

	frame.getContentPane().add(mainPanel);
        frame.pack();
    }

    class OKButtonListener implements ActionListener {
	// creates the tree and writes to file when button pressed
	public void actionPerformed(ActionEvent e) {
	    config.setProperty("povray.executable",fileField.getText());
	    frame.dispose();
	    try {
		config.store();
	    } catch (Exception err) {
		JOptionPane.showMessageDialog(frame,err.getMessage(),
					  "Setup Error",
					  JOptionPane.ERROR_MESSAGE);
	    }
	}
    }
};





