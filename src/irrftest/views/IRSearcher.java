package irrftest.views;

import java.io.File;
import java.util.ArrayList;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.swing.JOptionPane;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Query;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import indexing.Result;
import indexing.Searcher;

public class IRSearcher extends ViewPart {
	private Text txtSearch;
	private Button btnSearch;
	private Button btnFeedback;
	private Table tblResults;
	private Searcher searcher;
	private Query query;
	private ArrayList<Result> results;
	private ArrayList<Document> relevantDocuments;
	private ArrayList<Document> iRelevantDocuments;

	public IRSearcher() {
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
		searcher = new Searcher();
		txtSearch = new Text(parent, SWT.BORDER);
		txtSearch.setBounds(22, 37, 350, 21);
		results = new ArrayList<>();
		btnSearch = new Button(parent, SWT.NONE);
		tblResults = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		tblResults.setHeaderVisible(false);
		tblResults.setBounds(22, 79, 400, 250);

		btnSearch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String queryStr = txtSearch.getText().trim();
				query = null;
				try {
					query = searcher.buildQuery(queryStr);
				} catch (ParseException ex) {
					ex.printStackTrace();
				}
				results = performSearch(query);
				fillResultsTable(results);
			}

		});
		btnSearch.setBounds(380, 35, 75, 25);
		btnSearch.setText("Search");

		tblResults.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				TableItem res = tblResults.getSelection()[0];
				Result result = (Result) res.getData();
				String file = result.getPath();
				File fileToOpen = new File(file);
				if (fileToOpen.exists() && fileToOpen.isFile()) {
					IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException exf) {
						// Put your exception handler here if you wish to
					}
				} else {
					// Do something if the file does not exist
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
		btnFeedback.setBounds(180, 350, 100, 25);
		btnFeedback.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getRelevantDocuments();
				if (tblResults.getItemCount() == 0 || relevantDocuments.size() == 0) {
					JOptionPane.showMessageDialog(null, "You must select at least a result.", "Error",
							JOptionPane.WARNING_MESSAGE);
				} else {
					float alpha = 1;
					float beta = 1;
					float gama = 1;
					try {
						query = searcher.expand(query, alpha, beta, gama, relevantDocuments);
						System.out.println("Rocchio query = " + query.toString());
						fillResultsTable(searcher.searchIndex(query, 100));
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						JOptionPane.showMessageDialog(null, "Error making feedback","Error",JOptionPane.ERROR_MESSAGE);
											
					}
				}

				System.out.println("relevant: " + relevantDocuments.size());
				System.out.println("irelevant: " + iRelevantDocuments.size());
			}
		});
	}

	private void getRelevantDocuments() {
		relevantDocuments = new ArrayList<>();
		iRelevantDocuments = new ArrayList<>();
		for (int i = 0; i < tblResults.getItemCount(); i++) {
			Result res = (Result) tblResults.getItem(i).getData();
			if (tblResults.getItem(i).getChecked()) {
				relevantDocuments.add(res.getDocument());
			} else {
				iRelevantDocuments.add(res.getDocument());
			}
		}
	}

	private ArrayList<Result> performSearch(Query query) {
		//File indexDir = new File("c:/index/");
		int hits = 100;
		try {
			return searcher.searchIndex(query, hits);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<>();
		}
	}

	private void fillResultsTable(ArrayList<Result> results) {
		tblResults.removeAll();
		for (Result result : results) {
			TableItem tableItem = new TableItem(tblResults, SWT.NONE);
			tableItem.setData(result);
			tableItem.setText(result.getFileName() + String.format("   %.3f", result.getScore()));
			
		}
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

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
			return result.getFileName() + String.format("%.3f", result.getScore());
		}

	}
}
