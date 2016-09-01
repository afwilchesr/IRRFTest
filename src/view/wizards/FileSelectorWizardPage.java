package view.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class FileSelectorWizardPage extends WizardPage {
	private Text txtPathToIndex;
	private Text txtPathIndex;
	private Button btnAddToExistent;
	private Button btnCreateNew;

	public Text getTxtPathToIndex() {
		return txtPathToIndex;
	}

	public void setTxtPathToIndex(Text txtPathToIndex) {
		this.txtPathToIndex = txtPathToIndex;
	}

	public Text getTxtPathIndex() {
		return txtPathIndex;
	}

	public void setTxtPathIndex(Text txtPathIndex) {
		this.txtPathIndex = txtPathIndex;
	}

	/**
	 * Create the wizard.
	 */
	public FileSelectorWizardPage() {
		super("wizardPage");
		setTitle("Setup wizzard");
		setDescription("Choose the folder to index and where store the index.");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite cointainer = new Composite(parent, SWT.NULL);

		setControl(cointainer);
		cointainer.setLayout(new FormLayout());
		
		txtPathToIndex = new Text(cointainer, SWT.BORDER);
		FormData fd_txtPathToIndex = new FormData();
		txtPathToIndex.setLayoutData(fd_txtPathToIndex);
		txtPathToIndex.setEditable(false);
		
		Button btnPathToIndex = new Button(cointainer, SWT.NONE);
		FormData fd_btnPathToIndex = new FormData();
		fd_btnPathToIndex.right = new FormAttachment(0, 432);
		fd_btnPathToIndex.top = new FormAttachment(0, 25);
		fd_btnPathToIndex.left = new FormAttachment(0, 362);
		btnPathToIndex.setLayoutData(fd_btnPathToIndex);
		btnPathToIndex.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog fileDialog = new DirectoryDialog(parent.getShell());
				fileDialog.setText("Directory to index");
				fileDialog.setMessage("Select the directory you want to index.");
				String result = fileDialog.open();
				txtPathToIndex.setText(result);
				System.out.println(result);
				
			}
		});
		btnPathToIndex.setText("Browse...");
		
		Label lblFolderToIndex = new Label(cointainer, SWT.NONE);
		fd_txtPathToIndex.top = new FormAttachment(lblFolderToIndex, 6);
		FormData fd_lblFolderToIndex = new FormData();
		fd_lblFolderToIndex.bottom = new FormAttachment(100, -192);
		lblFolderToIndex.setLayoutData(fd_lblFolderToIndex);
		lblFolderToIndex.setBounds(50, 20, 500, 30);
		
		lblFolderToIndex.setText("Folder to index");
		
		Label lblIndexDirectory = new Label(cointainer, SWT.NONE);
		fd_lblFolderToIndex.right = new FormAttachment(lblIndexDirectory, 0, SWT.RIGHT);
		FormData fd_lblIndexDirectory = new FormData();
		fd_lblIndexDirectory.left = new FormAttachment(0, 10);
		lblIndexDirectory.setLayoutData(fd_lblIndexDirectory);
		lblIndexDirectory.setText("Index directory");
		
		btnAddToExistent = new Button(cointainer, SWT.NONE);
		fd_txtPathToIndex.left = new FormAttachment(btnAddToExistent, 0, SWT.LEFT);
		FormData fd_btnAddToExistent = new FormData();
		fd_btnAddToExistent.top = new FormAttachment(txtPathToIndex, 6);
		fd_btnAddToExistent.right = new FormAttachment(0, 155);
		fd_btnAddToExistent.left = new FormAttachment(0, 10);
		btnAddToExistent.setLayoutData(fd_btnAddToExistent);
		btnAddToExistent.setText("Add to existent Index");
		btnAddToExistent.setVisible(false);
		
		btnCreateNew = new Button(cointainer, SWT.RADIO);
		FormData fd_btnCreateNew = new FormData();
		fd_btnCreateNew.top = new FormAttachment(txtPathToIndex, 6);
		fd_btnCreateNew.left = new FormAttachment(btnAddToExistent, 6);
		btnCreateNew.setLayoutData(fd_btnCreateNew);
		btnCreateNew.setText("Create a new Index");
		btnCreateNew.setVisible(false);
		
		txtPathIndex = new Text(cointainer, SWT.BORDER);
		fd_txtPathToIndex.right = new FormAttachment(txtPathIndex, 0, SWT.RIGHT);
		fd_lblIndexDirectory.bottom = new FormAttachment(txtPathIndex, -6);
		FormData fd_txtPathIndex = new FormData();
		fd_txtPathIndex.left = new FormAttachment(0, 10);
		fd_txtPathIndex.top = new FormAttachment(0, 97);
		txtPathIndex.setLayoutData(fd_txtPathIndex);
		txtPathIndex.setEditable(false);
		
		Button btnPathIndex = new Button(cointainer, SWT.NONE);
		fd_txtPathIndex.right = new FormAttachment(btnPathIndex, -18);
		btnPathIndex.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(parent.getShell());
				dirDialog.setText("Folder to store the index");
				dirDialog.setMessage("Select the folder where you want to store the index.");
				String result = dirDialog.open();
				txtPathIndex.setText(result);
				System.out.println(result);
			}
		});
		btnPathIndex.setText("Browse...");
		FormData fd_btnPathIndex = new FormData();
		fd_btnPathIndex.right = new FormAttachment(btnPathToIndex, 0, SWT.RIGHT);
		fd_btnPathIndex.top = new FormAttachment(btnPathToIndex, 45);
		fd_btnPathIndex.left = new FormAttachment(0, 362);
		btnPathIndex.setLayoutData(fd_btnPathIndex);
	}

	
}
