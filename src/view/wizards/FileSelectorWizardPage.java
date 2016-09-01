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
	private Button btnAddToExistent;
	private Button btnCreateNew;
	private Text txtLog;
	private  StringBuilder log;

	public Text getTxtLog() {
		return txtLog;
	}

	public void setTxtLog(Text txtLog) {
		this.txtLog = txtLog;
	}

	public StringBuilder getLog() {
		return log;
	}

	public void setLog(String log) {		
		txtLog.setText(this.log.append(log).toString());
	}

	public Button getBtnAddToExistent() {
		return btnAddToExistent;
	}

	public void setBtnAddToExistent(Button btnAddToExistent) {
		this.btnAddToExistent = btnAddToExistent;
	}

	public Button getBtnCreateNew() {
		return btnCreateNew;
	}

	public void setBtnCreateNew(Button btnCreateNew) {
		this.btnCreateNew = btnCreateNew;
	}

	public Text getTxtPathToIndex() {
		return txtPathToIndex;
	}

	public void setTxtPathToIndex(Text txtPathToIndex) {
		this.txtPathToIndex = txtPathToIndex;
	}

	
	/**
	 * Create the wizard.
	 */
	public FileSelectorWizardPage() {
		super("wizardPage");
		setTitle("Setup wizzard");
		setDescription("Choose the folder to index.");
		log = new StringBuilder("Progress:\n");
	}

	/**
	 * Create contents of the wizard.
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite cointainer = new Composite(parent, SWT.BORDER);

		setControl(cointainer);
		cointainer.setLayout(new FormLayout());
		
		txtPathToIndex = new Text(cointainer, SWT.BORDER);
		FormData fd_txtPathToIndex = new FormData();
		fd_txtPathToIndex.left = new FormAttachment(0, 10);
		txtPathToIndex.setLayoutData(fd_txtPathToIndex);
		txtPathToIndex.setEditable(false);
		
		Button btnPathToIndex = new Button(cointainer, SWT.NONE);
		fd_txtPathToIndex.right = new FormAttachment(btnPathToIndex, -18);
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
		fd_lblFolderToIndex.left = new FormAttachment(0, 10);
		fd_lblFolderToIndex.bottom = new FormAttachment(100, -192);
		lblFolderToIndex.setLayoutData(fd_lblFolderToIndex);
		lblFolderToIndex.setBounds(50, 20, 500, 30);
		
		lblFolderToIndex.setText("Folder to index");
		
		btnAddToExistent = new Button(cointainer, SWT.RADIO);
		FormData fd_btnAddToExistent = new FormData();
		fd_btnAddToExistent.top = new FormAttachment(txtPathToIndex, 6);
		fd_btnAddToExistent.right = new FormAttachment(0, 155);
		fd_btnAddToExistent.left = new FormAttachment(0, 10);
		btnAddToExistent.setLayoutData(fd_btnAddToExistent);
		btnAddToExistent.setText("Add to existent Index");
		btnAddToExistent.setVisible(true);
		
		btnCreateNew = new Button(cointainer, SWT.RADIO);
		FormData fd_btnCreateNew = new FormData();
		fd_btnCreateNew.top = new FormAttachment(txtPathToIndex, 6);
		fd_btnCreateNew.left = new FormAttachment(btnAddToExistent, 6);
		btnCreateNew.setLayoutData(fd_btnCreateNew);
		btnCreateNew.setText("Create a new Index");
		btnCreateNew.setVisible(true);
		
		txtLog = new Text(cointainer, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.MULTI);
		FormData fd_txtLog = new FormData();
		fd_txtLog.bottom = new FormAttachment(btnAddToExistent, 133, SWT.BOTTOM);
		fd_txtLog.top = new FormAttachment(btnAddToExistent, 15);
		fd_txtLog.left = new FormAttachment(0, 12);
		fd_txtLog.right = new FormAttachment(0, 626);
		txtLog.setLayoutData(fd_txtLog);
	}
}
