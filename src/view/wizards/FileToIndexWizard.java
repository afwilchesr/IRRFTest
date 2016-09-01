package view.wizards;

import java.io.File;

import javax.swing.JOptionPane;

import org.eclipse.jface.wizard.Wizard;

import indexing.FileIndexer;

public class FileToIndexWizard extends Wizard {

	private FileSelectorWizardPage fileSelectorPage;

	public FileToIndexWizard() {
		setWindowTitle("Setup wizard");
		fileSelectorPage = new FileSelectorWizardPage();
	}

	@Override
	public void addPages() {
		addPage(fileSelectorPage);
	}

	@Override
	public boolean performFinish() {
		String pathIndex = fileSelectorPage.getTxtPathIndex().getText().trim();
		String pathToIndex = fileSelectorPage.getTxtPathToIndex().getText().trim();
		if (pathIndex.isEmpty() || pathToIndex.isEmpty()) {
			return false;
		}
		File indexDir = new File(pathIndex);
		File dataDir = new File(pathToIndex);
		try {
			int numIndex = FileIndexer.index(indexDir, dataDir, "java");
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

}
