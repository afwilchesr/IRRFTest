package view.views;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.jar.Attributes.Name;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import indexing.FileSearcher;
import indexing.Result;
import view.dialogs.FeedbackParametersDialog;
import view.wizards.FileToIndexWizard;

public class IRSearcher extends ViewPart {
	private Text txtSearch;
	private Button btnSearch;
	private Button btnFeedback;
	private Button btnSetup;
	private Table tblResults;
	private FileSearcher searcher;
	private Query query;
	private ArrayList<Result> results;
	private ArrayList<ScoreDoc> relevantDocuments;
	private ArrayList<ScoreDoc> noRelevantDocuments;

	public IRSearcher() throws CorruptIndexException, IOException {
		searcher = new FileSearcher();
		results = new ArrayList<>();
	}

	/**
	 * Create contents of the view part.
	 */
	@PostConstruct
	public void createControls(Composite parent) {
	}

	@Override
	@PreDestroy
	public void dispose() {
	}

	@Override
	public void createPartControl(Composite parent) {
		// indexing();
		parent.setLayout(null);

		txtSearch = new Text(parent, SWT.BORDER);
		txtSearch.setBounds(22, 50, 273, 21);

		btnSearch = new Button(parent, SWT.NONE);
		tblResults = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);

		tblResults.setHeaderVisible(false);
		tblResults.setBounds(22, 79, 365, 250);

		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!txtSearch.getText().trim().isEmpty())
					search(parent);
			}

		});
		txtSearch.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (!txtSearch.getText().trim().isEmpty())
					search(parent);

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				if (!txtSearch.getText().trim().isEmpty())
					search(parent);
			}
		});
		txtSearch.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				// search(parent);

			}
		});
		btnSearch.setBounds(313, 48, 75, 25);
		btnSearch.setText("Search");

		tblResults.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int tableIndex = tblResults.getSelectionIndex();
				if (tableIndex >= 0 && tableIndex < tblResults.getItemCount()) {
					TableItem res = tblResults.getItem(tableIndex);
					Result result = (Result) res.getData();
					String file = result.getPath();
					String method = result.getMethodName();
					int parameters = result.getParameters();
					File fileToOpen = new File(file);
					if (fileToOpen.exists() && fileToOpen.isFile()) {
						IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
						IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
						IJavaElement element;
						IEditorPart editor;
						try {
							editor = IDE.openEditorOnFileStore(page, fileStore);
							ICompilationUnit root = (ICompilationUnit) EditorUtility.getEditorInputJavaElement(editor,
									false);
							element = findElement(method, root, parameters);
							if (element != null) {
								JavaUI.revealInEditor(editor, element);
							}
						} catch (PartInitException | JavaModelException e1) {
							e1.printStackTrace();
						}

					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub

			}

		});
		// tableResults.setVisible(false);
		btnFeedback = new Button(parent, SWT.NONE);
		btnFeedback.setText("Feedback");
		btnFeedback.setBounds(150, 349, 100, 25);
		btnFeedback.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getRelevantDocuments();
				if (tblResults.getItemCount() == 0 || relevantDocuments.size() == 0) {
					MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_WARNING);
					msg.setMessage("You must select at least a result.");
					msg.setText("Select results");
					msg.open();
				} else {

					FeedbackParametersDialog dialog = new FeedbackParametersDialog(parent.getShell());
					dialog.create();
					float alpha = 1;
					float beta = 1;
					float gama = 1;
					if (dialog.open() == Window.OK) {
						alpha = dialog.getAlpha();
						beta = dialog.getBeta();
						gama = dialog.getGama();
						int decay = 0;
						try {
							query = searcher.expand(query, alpha, beta, gama, decay, relevantDocuments,
									noRelevantDocuments);
							// System.out.println("Rocchio query = " +
							// query.toString());
							fillResultsTable(searcher.searchIndex(query, 100));
							System.out.println("relevant: " + relevantDocuments.size());
							System.out.println("irelevant: " + noRelevantDocuments.size());
						} catch (Exception e1) {
							e1.printStackTrace();
							MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_ERROR);
							msg.setMessage("Error making feedback");
							msg.setText("Error");
							msg.open();
						}
					}

				}

			}
		});

		btnSetup = new Button(parent, SWT.NONE);
		btnSetup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// FileToIndexSelector fileToIndex = new FileToIndexSelector();
				WizardDialog wizardDialog = new WizardDialog(parent.getShell(), new FileToIndexWizard());
				if (wizardDialog.open() == Window.OK) {
					System.out.println("Ok pressed");
				} else {
					System.out.println("Cancel pressed");
				}
			}
		});
		btnSetup.setBounds(22, 10, 75, 34);
		btnSetup.setText("Setup");
		// Image image =
		// PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_ELCL_REMOVE);
		// btnSetup.setImage(image);
	}

	private void getRelevantDocuments() {
		relevantDocuments = new ArrayList<>();
		noRelevantDocuments = new ArrayList<>();
		for (int i = 0; i < tblResults.getItemCount(); i++) {
			Result res = (Result) tblResults.getItem(i).getData();
			if (tblResults.getItem(i).getChecked()) {
				relevantDocuments.add(res.getHit());
			} else {
				noRelevantDocuments.add(res.getHit());
			}
		}
	}

	private void search(Composite parent) {
		String queryStr = txtSearch.getText().trim();
		query = null;
		try {
			if (!queryStr.isEmpty()) {
				query = searcher.buildQuery(queryStr);
			}
		} catch (ParseException ex) {
			ex.printStackTrace();
			MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_ERROR);
			msg.setMessage("Query format is not valid.");
			msg.setText("Error");
			msg.open();
		}
		try {
			results = performSearch(query);
			if (results.isEmpty()) {
				MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_INFORMATION);
				msg.setMessage("There are not results.");
				msg.setText("No results");
				msg.open();
			}
			fillResultsTable(results);
		} catch (IOException ex) {
			MessageBox msg = new MessageBox(parent.getShell(), SWT.OK | SWT.ICON_ERROR);
			msg.setMessage("The index directory is not valid. go to setup.");
			msg.setText("Error");
			msg.open();
		}
	}

	private ArrayList<Result> performSearch(Query query) throws IOException, FileNotFoundException {
		// File indexDir = new File("c:/index/");
		int hits = 100;
		return searcher.searchIndex(query, hits);

	}

	private void fillResultsTable(ArrayList<Result> results) {
		tblResults.removeAll();
		for (Result result : results) {
			TableItem tableItem = new TableItem(tblResults, SWT.NONE);
			tableItem.setData(result);
			Image image = JavaUI.getSharedImages().getImage(org.eclipse.jdt.ui.ISharedImages.IMG_OBJS_PUBLIC);
			tableItem.setImage(image);
			tableItem.setText(result.toString());

		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	private IJavaElement findElement(String method, ICompilationUnit root, int parameters) throws JavaModelException {
		IJavaElement element = null;
		IType[] types = root.getAllTypes();
		// System.out.println(Arrays.toString(types));
		for (int i = 0; i < types.length; i++) {
			IMethod[] methods;
			methods = types[i].getMethods();
			for (int j = 0; j < methods.length; j++) {
				// System.out.println();
				System.out.println("element " + methods[j].toString());
				// String aux = methods[j].toString().b);
				// aux = aux.replace("element", "").trim();
				// System.out.println(aux);
				if (method.equalsIgnoreCase(methods[j].getElementName())
						&& methods[j].getParameters().length == parameters) {
					// System.out.println("element " + methods[j].);
					element = methods[j];
					return element;
				}
			}
		}
		return element;
	}

	static class ResultListProvider extends LabelProvider {

		@Override
		public Image getImage(Object element) {
			// TODO Auto-generated method stub
			return super.getImage(element);
		}

		@Override
		public String getText(Object element) {
			Result result = (Result) element;
			return result.toString();
		}

	}
}
