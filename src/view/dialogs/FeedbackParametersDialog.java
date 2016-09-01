package view.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;

public class FeedbackParametersDialog extends Dialog {
	private float alpha;
	private float beta;
	private float gama;
	private Spinner spAlpha;
	private Spinner spBeta;
	private Spinner spGama;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public FeedbackParametersDialog(Shell parentShell) {
		super(parentShell);		
	}

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.setBounds(0, 0, 220, 120);
		container.setLayout(new GridLayout(2, false));
		Label lblAlpha = new Label(container, SWT.NONE);
		lblAlpha.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		lblAlpha.setText("Alpha");

		spAlpha = new Spinner(container, SWT.BORDER);
		spAlpha.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		spAlpha.setMaximum(10000);
		spAlpha.setSelection(100);
		spAlpha.setDigits(2);

		Label lblBeta = new Label(container, SWT.NONE);
		lblBeta.setText("Beta");

		spBeta = new Spinner(container, SWT.BORDER);
		spBeta.setDigits(2);
		spBeta.setMaximum(10000);
		spBeta.setSelection(100);

		Label lblGama = new Label(container, SWT.NONE);
		lblGama.setText("Gama");

		spGama = new Spinner(container, SWT.BORDER);
		spGama.setMaximum(10000);
		spGama.setSelection(100);
		spGama.setDigits(2);

		return container;
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button button = createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	public float getAlpha() {
		return alpha;
	}

	public void setAlpha(float alpha) {
		this.alpha = alpha;
	}

	public float getBeta() {
		return beta;
	}

	public void setBeta(float beta) {
		this.beta = beta;
	}

	public float getGama() {
		return gama;
	}

	public void setGama(float gama) {
		this.gama = gama;
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void okPressed() {		
		alpha = (float) (spAlpha.getSelection()/Math.pow(10, 2));
		beta = (float) (spBeta.getSelection()/Math.pow(10, 2));
		gama = (float) (spGama.getSelection()/Math.pow(10, 2));
		super.okPressed();
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(230, 178);
	}

}
