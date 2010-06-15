/**
 * Copyright Linebee. www.linebee.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linebee.solrmeter.controller;

import java.awt.Dimension;
import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import com.linebee.solrmeter.SolrMeterMain;
import com.linebee.solrmeter.model.SolrMeterConfiguration;
import com.linebee.solrmeter.view.I18n;
import com.linebee.solrmeter.view.Model;
import com.linebee.solrmeter.view.SettingsPanelContainer;
import com.linebee.solrmeter.view.SwingUtils;

public class SolrMeterMenuController {
	
	public void onExitMenu() {
		SolrMeterMain.mainFrame.dispose();
		System.exit(0);
	}

	public void onImportMenu() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(this.createConfigurationFileFilter());
		int returnValue = fileChooser.showOpenDialog(SolrMeterMain.mainFrame);
		if(returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			if(!selectedFile.exists()) {
				Logger.getLogger(this.getClass()).error("Can't find file with name " + selectedFile.getName());
				//TODO show error
			}else {
				try {
					SolrMeterConfiguration.importConfiguration(selectedFile);
					SolrMeterMain.restartApplication();
				} catch (IOException e) {
					Logger.getLogger(this.getClass()).error("Error importing configuration", e);
					//TODO show error
				}
			}
		}
	}
	
	public void onExportMenu() {
		try {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fileChooser.setFileFilter(this.createConfigurationFileFilter());
			int returnValue = fileChooser.showSaveDialog(SolrMeterMain.mainFrame);
			if(returnValue == JFileChooser.APPROVE_OPTION) {
				File selectedFile = this.addExtension(fileChooser.getSelectedFile());
				if(selectedFile.exists()) {
					int optionResultPane = JOptionPane.showConfirmDialog(SolrMeterMain.mainFrame, 
								I18n.get("menu.file.export.fileExists.message"), 
								I18n.get("menu.file.export.fileExists.title"),
								JOptionPane.WARNING_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
					if(optionResultPane == JOptionPane.OK_OPTION) {
						this.doExport(selectedFile);
					}
				}else {
					this.doExport(selectedFile);
				}
			}
		} catch (IOException e) {
			Logger.getLogger(this.getClass()).error("Error exporting configuration", e);
			//TODO show error
		}
		
	}

	private File addExtension(File selectedFile) {
		if(!selectedFile.getName().endsWith(".smc.xml")) {
			return new File(selectedFile.getParent(), selectedFile.getName() + ".smc.xml");
		}
		return selectedFile;
	}

	private void doExport(File selectedFile) throws IOException {
		SolrMeterConfiguration.exportConfiguration(selectedFile);
	}

	private FileFilter createConfigurationFileFilter() {
		FileFilter fileFilter = new FileFilter() {

			@Override
			public boolean accept(File file) {
				if(file.isDirectory()) {
					return true;
				}
				return file.getName().endsWith(".smc.xml");
			}

			@Override
			public String getDescription() {
				return "*.smc.xml";
			}
			
		};
		return fileFilter;
	}

	public void onSettingsMenu() {
		JDialog dialog = new JDialog(SolrMeterMain.mainFrame, I18n.get("menu.edit.settings"), ModalityType.APPLICATION_MODAL);
		dialog.setContentPane(new SettingsPanelContainer(dialog, this.isSettingsEditable()));
		dialog.setSize(new Dimension(400, 400));
		SwingUtils.centerWindow(dialog);
		dialog.setVisible(true);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
	}

	private boolean isSettingsEditable() {
		if(Model.getInstance().getCurrentQueryExecutor().isRunning()) {
			return false;
		}
		if(Model.getInstance().getCurrentUpdateExecutor().isRunning()) {
			return false;
		}
		if(Model.getInstance().getCurrentOptimizeExecutor().isOptimizing()) {
			return false;
		}
		return true;
	}

}
