package com.Tool.Templateconflict;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import toolui.ToolUInew;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollBar;

public class Toolui extends JFrame {

	private JPanel contentPane;
	JTextArea textField = new JTextArea();


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ToolUInew frame = new ToolUInew();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Toolui() {
		setTitle("Template Conflict Eliminator Tool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 707, 404);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(211, 211, 211));
		contentPane.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		JFileChooser fileChooser = new JFileChooser();
	    

		JButton btnFromGitButton = new JButton("From Git Folder");
		btnFromGitButton.setFont(new Font("Verdana", Font.PLAIN, 16));
		
		JButton btnListOfIncluded = new JButton("List of included Stylesheets");
		btnListOfIncluded.setFont(new Font("Verdana", Font.PLAIN, 16));
		btnListOfIncluded.setBounds(27, 204, 263, 39);
		
		JButton btnEliminateConflicts = new JButton("Eliminate Conflicts");
		btnEliminateConflicts.setFont(new Font("Verdana", Font.PLAIN, 16));
		btnEliminateConflicts.setBounds(27, 274, 263, 39);
		
		btnFromGitButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    //Handle open button action.
			    if (e.getSource() == btnFromGitButton) {
			    	File gitFolder = new File("C:\\raja\\AnuTool\\Template conflict\\anu\\Apple.xsl").getParentFile();
			    	try {
						textField.setText(gitFolder.getPath().toString());
						btnEliminateConflicts.setEnabled(true);
						btnListOfIncluded.setEnabled(true);
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
//				    TemplateConflictEliminator.eliminateTempalateConflicts(gitFolder);
			   }
			}
		});

		btnFromGitButton.setBounds(27, 63, 263, 39);
		
		 
		contentPane.add(btnFromGitButton);
		
		JButton btnFromLocalFolder = new JButton("From Local Folder");
		btnFromLocalFolder.setFont(new Font("Verdana", Font.PLAIN, 15));
		btnFromLocalFolder.setBounds(27, 133, 263, 39);
		
		btnFromLocalFolder.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    //Handle open button action.
			    if (e.getSource() == btnFromLocalFolder) {
			        int returnVal = fileChooser.showOpenDialog(Toolui.this);

			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			            File file = fileChooser.getSelectedFile();
			            //This is where a real application would open the file.
			            textField.setText(file.getAbsolutePath());
						btnEliminateConflicts.setEnabled(true);
						btnListOfIncluded.setEnabled(true);
						final JComponent[] inputs = new JComponent[] {
						        new JLabel("Would you like to process all the Stylesheets in the folder of the File you Chose??? Saying No will proess the selected File alone")
						};
						int result = JOptionPane.showConfirmDialog(null, inputs, "My custom dialog", JOptionPane.OK_OPTION);
						if (result == JOptionPane.OK_OPTION) {
						    try{
//						    	Desktop.getDesktop().open(new File(new File(jTextField1.getText()).getParent()));
//						    	TemplateConflictEliminator.eliminateTempalateConflicts(file.getParentFile());
						    	
						    	textField.setText((file.getParentFile()).getAbsolutePath());
						    	
						    }catch(Exception ex){
						    	System.err.println("Cant open the stylesheet folder");
						    }
						} else {
							textField.setText(file.getAbsolutePath());
//							TemplateConflictEliminator.eliminateTempalateConflicts(file);
						}
			        }    
			   }
			}
		});

		contentPane.add(btnFromLocalFolder);
		

		
		btnListOfIncluded.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    //Handle open button action.
			    if (e.getSource() == btnListOfIncluded) {
//						    	Desktop.getDesktop().open(new File(new File(jTextField1.getText()).getParent()));
			    	textField.setText(TemplateConflictEliminator.eliminateTempalateConflicts(new File(textField.getText()), true));
			    	btnEliminateConflicts.setEnabled(false);
			    	btnListOfIncluded.setEnabled(false);
						    	
			   }
			}
		});

		contentPane.add(btnListOfIncluded);
		

		btnEliminateConflicts.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
			    //Handle open button action.
			    if (e.getSource() == btnEliminateConflicts) {
//						    	Desktop.getDesktop().open(new File(new File(jTextField1.getText()).getParent()));
						    	textField.setText(TemplateConflictEliminator.eliminateTempalateConflicts(new File(textField.getText()), false));
						    	btnEliminateConflicts.setEnabled(false);
						    	btnListOfIncluded.setEnabled(false);
						    	
			   }
			}
		});

		contentPane.add(btnEliminateConflicts);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(391, 215, 107, -143);
		contentPane.add(scrollPane);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		scrollPane_1.setBounds(313, 63, 305, 248);
		contentPane.add(scrollPane_1);
		
		scrollPane_1.setViewportView(textField);

	}
}
