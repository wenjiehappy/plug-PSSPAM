package com.tju.passd.wizard;

import org.eclipse.swt.widgets.Button;

import org.eclipse.jdt.core.IClasspathEntry;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;


public class PassdProjectPage extends WizardNewProjectCreationPage{
	
	private Button decision;
	private Button auction;
	
	protected PassdProjectPage(String pageName) {
		super(pageName);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createControl(Composite parent) {
		// TODO Auto-generated method stub
		super.createControl(parent);
		Composite container =(Composite) getControl();
	      
	    decision = new Button(container, SWT.CHECK);
	    decision.setText("Customize your own decision method");
	    auction = new Button(container, SWT.CHECK);
	    auction.setText("Customize your own auction method");
	}

	public Button getDecision() {
		return decision;
	}

	public void setDecision(Button decision) {
		this.decision = decision;
	}

	public Button getAuction() {
		return auction;
	}

	public void setAuction(Button auction) {
		this.auction = auction;
	}

}
