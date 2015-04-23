package com.tju.passd.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;


public class PassdProjectWizard extends BasicNewResourceWizard{


	private static final String TITLE = "PASSD Project";
	private static final String DESCRIPTION = "Create an PASSD project";

	protected PassdProjectPage mainPage;

	/**
	 * Creates an wizard for creating a new PASSD project in the workspace.
	 */
	public PassdProjectWizard() {
		//setDialogSettings(PABPlugin.getDefault().getDialogSettings());
		setNeedsProgressMonitor(true);
		setForcePreviousAndNextButtons(true);
		setWindowTitle(TITLE);
	}

	@Override
	public void addPages() {
		mainPage = new PassdProjectPage("PASSD Wizard Page");
		mainPage.setTitle(TITLE);
		mainPage.setDescription(DESCRIPTION);
		addPage(mainPage);
	}

	@Override
	public boolean performFinish() {
		IProject project = mainPage.getProjectHandle();
		try {
			createProject(project);
		} catch (CoreException e) {
			//PABPlugin.log(e);
			return false;
		}
		selectAndReveal(project);
		return true;
	}

	/**
	 * Create a new PASSD project.
	 * 
	 * @param project the new project
	 */
	private void createProject(final IProject project) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		String name = mainPage.getProjectName();
		IPath location = project.getLocation();
		final IProjectDescription description = workspace.newProjectDescription(name);
		if (location != null) {
			description.setLocation(location);
		}
		
		String[] javaNature = description.getNatureIds();
		String[] newJavaNature = new String[javaNature.length + 1];
		System.arraycopy(javaNature, 0, newJavaNature, 0, javaNature.length);
		newJavaNature[javaNature.length] = "org.eclipse.jdt.core.javanature"; // 这个标记证明本工程是Java工程
		description.setNatureIds(newJavaNature);
		
		IWorkspaceRunnable create = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					if (monitor == null) {
						monitor = new NullProgressMonitor();
					}
					monitor.beginTask("Creating PASSD Project...", 3);
					if (!project.exists()) {
						project.create(description, new SubProgressMonitor(
								monitor, 1));
					}
					if (monitor.isCanceled()) {
						throw new OperationCanceledException();
					}
					// Open first.
					project.open(IResource.BACKGROUND_REFRESH,
							new SubProgressMonitor(monitor, 1));
					
					IJavaProject javaProject = JavaCore.create(project);
					// //////////////////////////////////添加JRE库////////////////////////////
					try {
						// 获取默认的JRE库
						IClasspathEntry[] jreLibrary = PreferenceConstants.getDefaultJRELibrary();
						// 获取原来的build path
						IClasspathEntry[] oldClasspathEntries = javaProject.getRawClasspath();
						List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
						list.addAll(Arrays.asList(jreLibrary));
						list.addAll(Arrays.asList(oldClasspathEntries));
				
						javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// //////////////////////////////////创建输出路径/////////////////////////////
					IFolder binFolder = javaProject.getProject().getFolder("bin");
					try {
						binFolder.create(true, true, null);
						javaProject.setOutputLocation(binFolder.getFullPath(), null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					// /////////////////////////设置Java生成器///////////////////////
					try {
						IProjectDescription description2 = javaProject.getProject().getDescription();
						ICommand command = description2.newCommand();
						command.setBuilderName("org.eclipse.jdt.core.javabuilder");
						description2.setBuildSpec(new ICommand[] { command });
						description2.setNatureIds(new String[] { "org.eclipse.jdt.core.javanature" });
						javaProject.getProject().setDescription(description2, null);
					} catch (CoreException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
					// /////////////////////////////创建源代码文件夹//////////////////////////
					// ///////////源文件夹和文件夹相似,只是使用PackageFragmentRoot进行了封装////////
					IFolder srcFolder = javaProject.getProject().getFolder("src");
					try {
						srcFolder.create(true, true, null);
						// this.createFolder(srcFolder);
						// 创建SourceLibrary
						IClasspathEntry srcClasspathEntry = JavaCore.newSourceEntry(srcFolder.getFullPath());
					
						// 得到旧的build path
						IClasspathEntry[] oldClasspathEntries = javaProject.readRawClasspath();
					 
						// 添加新的
						List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
						list.addAll(Arrays.asList(oldClasspathEntries));
						list.add(srcClasspathEntry);
				
						// 原来存在一个与工程名相同的源文件夹,必须先删除
						IClasspathEntry temp = JavaCore.newSourceEntry(javaProject.getPath());
						if (list.contains(temp)) {
							list.remove(temp);
						}
					
						System.out.println(list.size());
					
						javaProject.setRawClasspath(list.toArray(new IClasspathEntry[list.size()]), null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					//create decision class
					ICompilationUnit compilationUnit;
					
					String javaCode = "package passd.agent;\n\rpublic class Decision{\n\r}";
					IPackageFragmentRoot packageFragmentRoot = javaProject.findPackageFragmentRoot(srcFolder.getFullPath());
					IPackageFragment packageFragment = packageFragmentRoot.createPackageFragment("passd.agent", true, null);
						
					compilationUnit = packageFragment.createCompilationUnit("Decision.java", javaCode, true, new NullProgressMonitor());
						 
				
					Boolean customizeD = mainPage.getDecision().getSelection();
					IFile decisionFile = javaProject.getProject().getFile(compilationUnit.getPath().makeRelativeTo(javaProject.getPath()));
					//System.out.println("decisionFile path is " + compilationUnit.getPath().makeRelativeTo(javaProject.getPath()));
					if(customizeD){
						createCustomizeDecision(decisionFile);
						IDE.openEditor(page, decisionFile, "org.eclipse.jdt.ui.CompilationUnitEditor");
					
					}
					else{
						createDefaultDecision(decisionFile);
					}
					
					//create Auction class
					IFile auctionFile = project.getFile(new Path("/Auction.java"));
					InputStream inputStreamAuction = new ByteArrayInputStream(
							"import com.tju.passd.agent.base.*\n\rpublic class Auction{\n\r\n}".getBytes());
					if(!auctionFile.exists())
						auctionFile.create(inputStreamAuction, false, null);
					
					Boolean customizeA = mainPage.getAuction().getSelection();
					if(customizeA){
						createCustomizeAuction(auctionFile);
						IDE.openEditor(page, auctionFile, "org.eclipse.jdt.ui.CompilationUnitEditor");
					}
					else
						createDefaultAuction(auctionFile);
					
				} finally {
					monitor.done();
				}
			}

			
		};
		workspace.run(create, workspace.getRoot(), 0, null);
		if (!project.isOpen()) {
			project.open(new NullProgressMonitor());
		}
	}
	
	private void createDefaultAuction(IFile auction) {
		// TODO Auto-generated method stub
		
	}

	private void createCustomizeAuction(IFile auction) {
		// TODO Auto-generated method stub
		
	}

	private void createDefaultDecision(IFile decision) {
		// TODO Auto-generated method stub
		
		
	}

	private void createCustomizeDecision(IFile decision) {
		// TODO Auto-generated method stub
		
	}

}
