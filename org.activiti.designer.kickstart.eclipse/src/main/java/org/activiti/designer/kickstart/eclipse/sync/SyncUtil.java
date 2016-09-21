/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.designer.kickstart.eclipse.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.activiti.designer.kickstart.eclipse.Logger;
import org.activiti.designer.kickstart.eclipse.common.IoUtils;
import org.activiti.designer.kickstart.eclipse.common.KickstartPlugin;
import org.activiti.designer.kickstart.eclipse.navigator.CmisUtil;
import org.activiti.designer.kickstart.util.KickstartConstants;
import org.activiti.workflow.simple.alfresco.conversion.json.AlfrescoSimpleWorkflowJsonConverter;
import org.activiti.workflow.simple.definition.AbstractConditionStepListContainer;
import org.activiti.workflow.simple.definition.AbstractStepListContainer;
import org.activiti.workflow.simple.definition.FormStepDefinition;
import org.activiti.workflow.simple.definition.ListConditionStepDefinition;
import org.activiti.workflow.simple.definition.ListStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.activiti.workflow.simple.definition.WorkflowDefinition;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author jbarrez
 */
public class SyncUtil {
	
	public static void startProcessSynchronizationBackgroundJob(final Shell shell, final CmisObject destination, 
			final String targetFileName, final boolean warnForExistingFile, final boolean ignoreVersions, final IFile sourceFile) {
		
	  Job uploadJob = new Job("Uploading file") {

	  	protected IStatus run(IProgressMonitor monitor) {
	  		try {
	  			
	  			String nodeId = null;
	  			
	  			// If destination is a folder, upload document to it
	  			// If file already exists in folder, the CmisConstraint exception is thrown (see below)
	  			if (destination instanceof Folder) {
	  			  IFile zipFile = createZipFile(sourceFile, shell);
		  			byte[] content = IoUtils.getBytesFromInputStream(zipFile.getContents());
						nodeId = CmisUtil.uploadDocumentToFolder((Folder) destination, zipFile.getName(), content);
						zipFile.delete(true, null);
	  			} else if (destination instanceof Document) { // If destination is a document, create a new version
	  				if (warnForExistingFile) {
	  					showFileExistsDialog(shell, targetFileName, sourceFile, (Document) destination);
	  					return Status.CANCEL_STATUS;
	  				} else {
	  					
	  					// Verify versions
	  	  			String repoVersionLabel = ((Document) destination).getVersionLabel();
	  	  			String localVersionLabel = retrieveLocalVersionLabel(sourceFile);
	  	  			
	  	  			if (!ignoreVersions && (compareVersions(localVersionLabel, repoVersionLabel) < 0)) { // local version is behind, we need to check
	  	  				showFileExistsDialog(shell, targetFileName, sourceFile, (Document) destination);
	  	  				return Status.CANCEL_STATUS;
	  	  			} else {
	  	  				nodeId = uploadFileToRepository((Document) destination, sourceFile, shell);
	  	  			}
	  				}
	  			}
	  			
	  			Document processDocumentInRepo = (Document) CmisUtil.getCmisObject(nodeId);
	  			updateKickstartProcessJson(sourceFile, processDocumentInRepo);
					showSuccessMessage(shell, destination);
	     
	      } catch (CmisConstraintException cmisConstraintException) {
	      	
	      	// A file already exists (but there was no node id): resolve conflict
	      	
	      	Document targetDocument = null;
	      	if (destination instanceof Document) {
	      		targetDocument = (Document) destination;
	      	} else if (destination instanceof Folder) {
	      		Folder destinationFolder = (Folder) destination;
	      		for (CmisObject child : destinationFolder.getChildren()) {
	      			if (child instanceof Document && child.getName().equals(targetFileName)) {
	      				targetDocument = (Document) child;
	      				break;
	      			}
	      		}
	      	}
	      	
	      	// Show a conflict dialog
	      	showFileExistsDialog(shell, targetFileName, sourceFile, targetDocument);
	      	
	      	return Status.CANCEL_STATUS;
	      	
	      } catch (Exception e) {
	      	Logger.logError(e);
	      	return Status.CANCEL_STATUS;
	      }
	  		return Status.OK_STATUS;
	  	}

			private void showFileExistsDialog(final Shell shell,
          final String targetFileName, final IFile sourceFile, final Document destination) {
	      Display.getDefault().syncExec(new Runnable() {
	      	public void run() {
	      		FileExistsInFolderDialog fileExistsInFolderDialog = 
	      				new FileExistsInFolderDialog(shell, destination, targetFileName, sourceFile);
	      		fileExistsInFolderDialog.open();
	      	}
	      });
      }

	  };
	  uploadJob.setUser(true);
	  uploadJob.schedule();
  }
	
	public static String retrieveLocalVersionLabel(final IFile sourceFile) throws CoreException {
    AlfrescoSimpleWorkflowJsonConverter simpleWorkflowJsonConverter = new AlfrescoSimpleWorkflowJsonConverter();
    WorkflowDefinition workflowDefinition = simpleWorkflowJsonConverter.readWorkflowDefinition(sourceFile.getContents());
    return (String) workflowDefinition.getParameters().get(SyncConstants.VERSION);
  }
	

	private static String uploadFileToRepository(final Document document, final IFile sourceFile, 
	        final Shell shell) throws IOException, CoreException {
	  
	  IFile zipFile = createZipFile(sourceFile, shell);
	  String nodeId = CmisUtil.uploadNewVersion(document, IoUtils.getBytesFromInputStream(zipFile.getContents()), "application/zip");
	  zipFile.delete(true, null);
	  return nodeId;
  }
	
	private static void updateKickstartProcessJson(IFile sourceFile, Document document) throws CoreException, IOException {

		// Read
	  AlfrescoSimpleWorkflowJsonConverter simpleWorkflowJsonConverter = new AlfrescoSimpleWorkflowJsonConverter();
		WorkflowDefinition workflowDefinition = simpleWorkflowJsonConverter.readWorkflowDefinition(sourceFile.getContents());
		
		// Update
		workflowDefinition.getParameters().put(SyncConstants.REPOSITORY_NODE_ID, document.getId());
		workflowDefinition.getParameters().put(SyncConstants.VERSION, document.getVersionLabel());
		
		// Write
		FileWriter writer = new FileWriter(new File(sourceFile.getLocationURI().getPath()));
		simpleWorkflowJsonConverter.writeWorkflowDefinition(workflowDefinition, writer);
		sourceFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	protected static void showSuccessMessage(final Shell shell, final CmisObject cmisObject) {
    Display.getDefault().syncExec(new Runnable() {
    	public void run() {
    		String path = (cmisObject instanceof Folder) ? 
    				((Folder) cmisObject).getPath() : ((Document)cmisObject).getParents().get(0).getPath();
    	  MessageDialog.openInformation(shell, "Process upload successful", 
        		"New version of process is stored at " + path);
    	}
    });
  }
	
	protected static void showSuccessMessageLocalCopy(final Shell shell, final CmisObject cmisObject) {
    Display.getDefault().syncExec(new Runnable() {
      public void run() {
        String path = (cmisObject instanceof Folder) ? 
            ((Folder) cmisObject).getPath() : ((Document)cmisObject).getParents().get(0).getPath();
        MessageDialog.openInformation(shell, "Process download successful", 
            "Copied process from " + path);
      }
    });
  }
	
	public static void copyRepoFileToLocalFile(final Shell shell, final IFile sourceFile, final Document destination) {
		Job downloadJob = new Job("Downloading file") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					
				  downloadZipFile(sourceFile.getProject(), destination, shell, monitor);
				  showSuccessMessageLocalCopy(shell, destination);
					
          return Status.OK_STATUS;
        } catch (Exception e) {
          e.printStackTrace();
          return Status.CANCEL_STATUS;
        }
			}
		};
		downloadJob.setUser(true);
		downloadJob.schedule();
	}
	
	/**
	 * -1 : versionLabel1 < versionLabel2 
	 * 0  : equal
	 * 1  : versionLabel1 > versionLabel2
	 * null: error happened
	 */
	public static Integer compareVersions(String versionLabel1, String versionLabel2) {
		try {
			int majorVersion1 = Integer.valueOf(versionLabel1.split("\\.")[0]);
			int minorVersion1 = Integer.valueOf(versionLabel1.split("\\.")[1]);
			
			int majorVersion2 = Integer.valueOf(versionLabel2.split("\\.")[0]);
			int minorVersion2 = Integer.valueOf(versionLabel2.split("\\.")[1]);
	
			if (majorVersion1 != majorVersion2) {
				return new Integer(majorVersion1).compareTo(new Integer(majorVersion2));
			} else {
				
				// major version is equal
				return new Integer(minorVersion1).compareTo(new Integer(minorVersion2));
				
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	protected static IFile downloadZipFile(IProject project, Document document, final Shell shell, IProgressMonitor monitor) throws Exception {
	  IFolder tempzipFolder = project.getFolder("tempzip");
    if (tempzipFolder.exists()) {
      tempzipFolder.delete(true, monitor);
    }
    
    tempzipFolder.create(true, true, monitor);
    
    IFile file = tempzipFolder.getFile(new Path(document.getName()));
    file.create(CmisUtil.downloadDocument(document), true, null);
    
    IFile openFile = null;
    byte[] buffer = new byte[1024];
    if ("zip".equalsIgnoreCase(file.getFileExtension())) {
      final ZipInputStream zis = new ZipInputStream(new FileInputStream(file.getLocation().toFile()));
      boolean hasMoreEntries = true;
      IFile processFile = null;
      while (hasMoreEntries) {
        ZipEntry entry = zis.getNextEntry();
        if (entry != null) {
          IFile unzippedFile = tempzipFolder.getFile(entry.getName());
          if ("kickproc".equalsIgnoreCase(unzippedFile.getFileExtension())) {
            processFile = unzippedFile;
          }
          String filePath = unzippedFile.getLocationURI().getPath();
          File extractFile = new File(filePath);
          FileOutputStream fos = new FileOutputStream(extractFile);
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }

          fos.close(); 
        } else {
          hasMoreEntries = false;
        }
      }
      zis.close();
      
      tempzipFolder.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
      if (processFile != null) {
        openFile = processWorkflowDefinition(processFile, tempzipFolder, document, shell, monitor);
      }
    }
    
    tempzipFolder.delete(true, monitor);
    project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
    return openFile;
	}
	
	protected static IFile processWorkflowDefinition(IFile sourceFile, IFolder unzippedFolder, Document document, final Shell shell, IProgressMonitor monitor) throws Exception {
    
    if (sourceFile.getProject().findMember(sourceFile.getName()) != null) {
      sourceFile.getProject().findMember(sourceFile.getName()).delete(true, monitor);
    }
    unzippedFolder.getFile(sourceFile.getName()).copy(sourceFile.getProject().getFullPath().append(sourceFile.getName()), true, monitor);
    IFile newProcessFile = sourceFile.getProject().getFile(sourceFile.getName());
    
    String filePath = newProcessFile.getLocationURI().getPath();
    File processFile = new File(filePath);
    FileInputStream fileStream = new FileInputStream(processFile);
    AlfrescoSimpleWorkflowJsonConverter converter = new AlfrescoSimpleWorkflowJsonConverter();
    WorkflowDefinition definition = null;
    try {
      definition = converter.readWorkflowDefinition(fileStream);
    } catch (final Exception e) {
      definition = new WorkflowDefinition();
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          Status errorStatus = null;
          if (e.getCause() != null) {
            errorStatus = new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, e.getCause().getMessage());
          } else {
            errorStatus = new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, e.getMessage());
          }
          ErrorDialog.openError(shell, "Error", "An error occured while reading kickstart process file.", errorStatus);
        }
      });
      return null;
    }
    
    if (definition.getParameters().containsKey(KickstartConstants.PARAMETER_FORM_REFERENCE)) {
      String startFormPath = (String) definition.getParameters().get(KickstartConstants.PARAMETER_FORM_REFERENCE);
      IFile startFormFile = sourceFile.getProject().getFile(new Path(startFormPath));
      if (unzippedFolder.getFile(startFormFile.getName()) != null) {
        IContainer newFolder = makeDirs(startFormPath, sourceFile.getProject(), monitor);
        if (newFolder.findMember(startFormFile.getName()) != null) {
          newFolder.findMember(startFormFile.getName()).delete(true, monitor);
        }
        unzippedFolder.getFile(startFormFile.getName()).copy(newFolder.getFullPath().append(startFormFile.getName()), true, monitor);
      }
    }
    
    walkthroughForms(definition.getSteps(), unzippedFolder, monitor);
    
    // Update the JSON node location
    definition.getParameters().put(SyncConstants.REPOSITORY_NODE_ID, document.getId());
    definition.getParameters().put(SyncConstants.VERSION, document.getVersionLabel());
    
    // Write
    FileWriter writer = new FileWriter(new File(newProcessFile.getLocationURI().getPath()));
    converter.writeWorkflowDefinition(definition, writer);
    newProcessFile.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
    
    return newProcessFile;
  }
  
  protected static void walkthroughForms(List<StepDefinition> stepList, IFolder unzippedFolder, IProgressMonitor monitor) throws Exception {
    for (StepDefinition step : stepList) {
      if (step instanceof FormStepDefinition) {
        FormStepDefinition formStep = (FormStepDefinition) step;

        if (formStep.getParameters().containsKey(KickstartConstants.PARAMETER_FORM_REFERENCE)) {
          String formPath = (String) formStep.getParameters().get(KickstartConstants.PARAMETER_FORM_REFERENCE);
          IFile formFile = unzippedFolder.getProject().getFile(new Path(formPath));
          if (unzippedFolder.getFile(formFile.getName()) != null) {
            IContainer newFolder = makeDirs(formPath, formFile.getProject(), monitor);
            if (newFolder.findMember(formFile.getName()) != null) {
              newFolder.findMember(formFile.getName()).delete(true, monitor);
            }
            unzippedFolder.getFile(formFile.getName()).copy(newFolder.getFullPath().append(formFile.getName()), true, monitor);
          }
        }
      } else if (step instanceof AbstractStepListContainer<?>) {
        List<?> childList = ((AbstractStepListContainer<?>) step).getStepList();
        for (Object object : childList) {
          if (object instanceof ListStepDefinition<?>) {
            walkthroughForms(((ListStepDefinition<?>) object).getSteps(), unzippedFolder, monitor);
          }
        }
      
      } else if (step instanceof AbstractConditionStepListContainer<?>) {
        List<?> childList = ((AbstractConditionStepListContainer<?>) step).getStepList();
        for (Object object : childList) {
          if (object instanceof ListConditionStepDefinition<?>) {
            walkthroughForms(((ListConditionStepDefinition<?>) object).getSteps(), unzippedFolder, monitor);
          }
        }
      }
    }
  }
  
  protected static IContainer makeDirs(String filePath, IProject project, IProgressMonitor monitor) throws Exception {
    String[] folders = filePath.split("/");
    if (folders.length == 1) {
      return project;
    } else {
      IFolder newFolder = project.getFolder(folders[0]);
      if (newFolder.exists() == false) {
        newFolder.create(true, true, monitor);
      }
      for (int i = 1; i < folders.length - 1; i++) {
        IFolder childFolder = newFolder.getFolder(folders[i]);
        if (childFolder.exists() == false) {
          childFolder.create(true, true, monitor);
        }
        newFolder = childFolder;
      }
      return newFolder;
    }
  }
	
	protected static IFile createZipFile(final IFile sourceFile, final Shell shell) throws IOException, CoreException {
	  String filePath = sourceFile.getLocationURI().getPath();
    File processFile = new File(filePath);
    FileInputStream fileStream = new FileInputStream(processFile);
    AlfrescoSimpleWorkflowJsonConverter converter = new AlfrescoSimpleWorkflowJsonConverter();
    WorkflowDefinition definition = null;
    try {
      definition = converter.readWorkflowDefinition(fileStream);
    } catch (final Exception e) {
      definition = new WorkflowDefinition();
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          Status errorStatus = null;
          if (e.getCause() != null) {
            errorStatus = new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, e.getCause().getMessage());
          } else {
            errorStatus = new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, e.getMessage());
          }
          ErrorDialog.openError(shell, "Error", "An error occured while reading kickstart process file.", errorStatus);
        }
      });
      return null;
    }
    
    ArrayList<IFile> zipItemList = new ArrayList<IFile>();
    zipItemList.add(sourceFile);
    
    if (definition.getParameters().containsKey(KickstartConstants.PARAMETER_FORM_REFERENCE)) {
      String startFormPath = (String) definition.getParameters().get(KickstartConstants.PARAMETER_FORM_REFERENCE);
      IFile startFormFile = sourceFile.getProject().getFile(new Path(startFormPath));
      zipItemList.add(startFormFile);
    }
    
    addFormsToList(definition.getSteps(), zipItemList, sourceFile.getProject());
    
    IFolder tempzipFolder = sourceFile.getProject().getFolder("tempzip");
    if (tempzipFolder.exists()) {
      tempzipFolder.delete(true, new NullProgressMonitor());
    }

    tempzipFolder.create(true, true, new NullProgressMonitor());
    
    for (IFile itemResource : zipItemList) {
      String itemFilename = itemResource.getName();
      itemResource.copy(tempzipFolder.getFile(itemFilename).getFullPath(), true, new NullProgressMonitor());
    }
    
    IFolder targetFolder = sourceFile.getProject().getFolder("target");
    if (targetFolder.exists() == false) {
      targetFolder.create(true, true, new NullProgressMonitor());
    }
    
    try {
      compressPackage(targetFolder, tempzipFolder, sourceFile.getName() + ".zip");
    } catch (final Exception e) {
      Display.getDefault().syncExec(new Runnable() {
        public void run() {
          ErrorDialog.openError(shell, "Error", "An error occured while zipping the kickstart process files.", 
              new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, e.getMessage()));
        }
      });
      return null;
    }
    
    try {
      tempzipFolder.delete(true, null);
    } catch(Exception e) {
      // ignore
    }
    
    targetFolder.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
    
    return targetFolder.getFile(sourceFile.getName() + ".zip");
	}
	
	protected static void addFormsToList(List<StepDefinition> stepList, List<IFile> zipItemList, IProject project) {
	  for (StepDefinition step : stepList) {
      if (step instanceof FormStepDefinition) {
        FormStepDefinition formStep = (FormStepDefinition) step;
        if (formStep.getParameters().containsKey(KickstartConstants.PARAMETER_FORM_REFERENCE)) {
          String formPath = (String) formStep.getParameters().get(KickstartConstants.PARAMETER_FORM_REFERENCE);
          IFile formFile = project.getFile(new Path(formPath));
          zipItemList.add(formFile);
        }
      } else if (step instanceof AbstractStepListContainer<?>) {
        List<?> childList = ((AbstractStepListContainer<?>) step).getStepList();
        for (Object object : childList) {
          if (object instanceof ListStepDefinition<?>) {
            addFormsToList(((ListStepDefinition<?>) object).getSteps(), zipItemList, project);
          }
        }
      
      } else if (step instanceof AbstractConditionStepListContainer<?>) {
        List<?> childList = ((AbstractConditionStepListContainer<?>) step).getStepList();
        for (Object object : childList) {
          if (object instanceof ListConditionStepDefinition<?>) {
            addFormsToList(((ListConditionStepDefinition<?>) object).getSteps(), zipItemList, project);
          }
        }
      }
    }
	}

	protected static void compressPackage(final IFolder destination, final IFolder folderToPackage, final String fileName) throws Exception {
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    File base = folderToPackage.getLocation().toFile();
    final IFile archiveFile = workspace.getRoot().getFile(destination.getFile(fileName).getFullPath());
    final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(archiveFile.getLocation().toFile()));
    final String absoluteDirPathToStrip = folderToPackage.getLocation().toFile().getAbsolutePath() + File.separator;
    try {
      zipDirectory(out, base, absoluteDirPathToStrip);
    } finally {
      if (out != null) {
        out.close();
      }
    }
  }
  
	protected static void zipDirectory(final ZipOutputStream out, final File base, final String absoluteDirPathToStrip) throws Exception {
    File[] reportFiles = base.listFiles();
    for (final File file : reportFiles) {
      if (file.isDirectory()) {
        zipDirectory(out, file, absoluteDirPathToStrip);
        continue;
      }
      String entryName = StringUtils.removeStart(file.getAbsolutePath(), absoluteDirPathToStrip);
      entryName = backlashReplace(entryName);
      ZipEntry entry = new ZipEntry(entryName);
      out.putNextEntry(entry);
      if (file.isFile()) {
        FileInputStream fin = new FileInputStream(file);
        byte[] fileContent = new byte[(int)file.length()];
        fin.read(fileContent);
        out.write(fileContent);
        fin.close();
      }
      out.closeEntry();
    }
  }
	
	protected static String backlashReplace(String myStr){
    final StringBuilder result = new StringBuilder();
    final StringCharacterIterator iterator = new StringCharacterIterator(myStr);
    char character =  iterator.current();
    while (character != CharacterIterator.DONE ) {    
      if (character == '\\') {
        result.append("/");
      } else {
        result.append(character);
      }
      character = iterator.next();
    }
    return result.toString();
  }
}
