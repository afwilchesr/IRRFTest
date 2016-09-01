package view.wizards;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.eclipse.jface.wizard.Wizard;

import indexing.FileIndexer;
import indexing.FileSearcher;
import irrftest.Activator;

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

	/*private void modifyIndexDir(File indexDir) {
		OutputStream out = null;
		try {

			Properties props = new Properties();
			//URL url = new URL("platform:/plugin/IRRFTest/build.properties");
			for (File file : File.listRoots()) {
				System.out.println(file.getAbsolutePath());
			}
			File f = new File(getClass().getResource("./META-INF/index.properties").getFile());
			System.out.println("build.prop path: " + f.getAbsolutePath());
			if (f.exists()) {
				props.load(new FileReader(f));
				props.setProperty("index", indexDir.getAbsolutePath());
			}

			out = new FileOutputStream(f);
			props.store(out, "This is an optional header comment string");

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
	}*/

	@Override
	public boolean performFinish() {
		//String pathIndex = fileSelectorPage.getTxtPathIndex().getText().trim();
		String pathToIndex = fileSelectorPage.getTxtPathToIndex().getText().trim();
		boolean create = fileSelectorPage.getBtnCreateNew().getSelection();
		if (pathToIndex.isEmpty()) {
			JOptionPane.showMessageDialog(null, "You must select a folder to index", "Select a folder",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		//File indexDir = new File(pathIndex);
		File dataDir = new File(pathToIndex);
		//FileSearcher.setIndexDir(indexDir);		
		if (!pathToIndex.isEmpty()) {
			try {
				int numIndex = FileIndexer.index( dataDir, "java", create);
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
