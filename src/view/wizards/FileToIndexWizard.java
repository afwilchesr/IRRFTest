package view.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.wizard.Wizard;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import indexing.FileIndexer;
import indexing.FileSearcher;

public class FileToIndexWizard extends Wizard {

	public static FileSelectorWizardPage fileSelectorPage;

	public FileToIndexWizard() {
		setWindowTitle("Setup wizard");
		fileSelectorPage = new FileSelectorWizardPage();
	}

	@Override
	public void addPages() {
		addPage(fileSelectorPage);
	}

	private void modifyIndexDirPref(File indexDir) {
		Preferences preferences = ConfigurationScope.INSTANCE.getNode("IRRFTest");
		Preferences sub1 = preferences.node("index");
		sub1.put("indexFile", indexDir.getAbsolutePath());
		try {
			// forces the application to save the preferences
			preferences.flush();
		} catch (BackingStoreException e2) {
			e2.printStackTrace();
			JOptionPane.showMessageDialog(null, "error modifying preferences");
		}
	}

	private void modifyIndexDir(File indexDir) {
		OutputStream out = null;
		try {

			Properties props = new Properties();
			File f = new File(getClass().getClassLoader().getResource("/resources/index.properties").getFile());
			System.out.println("build.prop path: " + f.getAbsolutePath());
			if (f.exists()) {
				props.load(new FileReader(f));
				props.setProperty("index", indexDir.getAbsolutePath());
			}

			out = new FileOutputStream(f);
			props.store(out, "");

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "error modifying build properties");
		} finally {

			if (out != null) {

				try {

					out.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@Override
	public boolean performFinish() {
		String pathIndex = fileSelectorPage.getTxtPathIndex().getText().trim();
		String pathToIndex = fileSelectorPage.getTxtPathToIndex().getText().trim();
		boolean create = fileSelectorPage.getBtnCreateNew().getSelection();
		if (!pathToIndex.isEmpty() && pathIndex.isEmpty()) {
			JOptionPane.showMessageDialog(null, "You must select a folder to store tne index", "Select a folder",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		File indexDir = new File(pathIndex);
		File dataDir = new File(pathToIndex);
		if(!indexDir.isDirectory()){
			JOptionPane.showMessageDialog(null, "You must select a valid folder to store the index.", "Select a folder",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}else{
			FileSearcher.setIndexDir(indexDir);
			modifyIndexDirPref(indexDir);
		}		
		if (!pathToIndex.isEmpty()) {
			try {
				if(!indexDir.isDirectory()){
					JOptionPane.showMessageDialog(null, "You must select a valid folder to index.", "Select a folder",
							JOptionPane.WARNING_MESSAGE);
					return false;
				}
				int numIndex = FileIndexer.index(dataDir, "java", create);
				System.out.println("Total files indexed " + numIndex);
				JOptionPane.showMessageDialog(null, "Total files indexed " + numIndex, "Files Indexed",
						JOptionPane.INFORMATION_MESSAGE);
				return true;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Error indexing", "Error", JOptionPane.ERROR_MESSAGE);
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

}
