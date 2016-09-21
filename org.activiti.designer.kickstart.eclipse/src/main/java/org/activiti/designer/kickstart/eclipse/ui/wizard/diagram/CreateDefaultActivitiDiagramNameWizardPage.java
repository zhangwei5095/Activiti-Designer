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
package org.activiti.designer.kickstart.eclipse.ui.wizard.diagram;

import org.activiti.designer.kickstart.eclipse.common.KickstartPlugin;
import org.activiti.designer.kickstart.eclipse.common.PluginImage;
import org.activiti.designer.util.ActivitiConstants;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class CreateDefaultActivitiDiagramNameWizardPage extends WizardNewFileCreationPage {

  public static final String PAGE_NAME = "createDefaultActivitiDiagramNameWizardPage";

  private static final String DEFAULT_DIAGRAM_NAME = "MyProcess";

  public CreateDefaultActivitiDiagramNameWizardPage(IStructuredSelection selection) {
    super(PAGE_NAME, selection);
    super.setFileName(DEFAULT_DIAGRAM_NAME);
    setTitle("New Activiti Diagram");
    setImageDescriptor(KickstartPlugin.getImageDescriptor(PluginImage.ACTIVITI_LOGO_64x64));
    setDescription("Create a new Activiti BPMN 2.0 Diagram.");
    setFileExtension(StringUtils.substringAfter(".kickproc", "."));

    // Set selection to the default folder if the project itself was selected
    final IProject project = getProjectFromSelection(selection);
    if (project != null) {
      final IFolder folder = project.getFolder(ActivitiConstants.DIAGRAM_FOLDER);
      if (folder != null && folder.exists()) {
        selection.toList().set(0, folder);
      }
    }
  }
  private IProject getProjectFromSelection(final IStructuredSelection selection) {
    IProject result = null;
    if (selection.size() == 1) {
      final Object firstSelectionElement = selection.getFirstElement();
      if (firstSelectionElement instanceof IJavaProject) {
        final IJavaProject javaProject = (IJavaProject) firstSelectionElement;
        result = javaProject.getProject();
      }
    }
    return result;
  }
  @Override
  public boolean isPageComplete() {
    return getWizard().canFinish();
  }

  @Override
  protected IStatus validateLinkedResource() {
    boolean valid = getWizard().canFinish();
    if (!valid) {
      final String errorMessage = String.format("A file with the name '%s' already exists in the project. Choose a different name for the diagram.",
              getDiagramName());
      this.setErrorMessage(errorMessage);
      return new Status(IStatus.ERROR, KickstartPlugin.PLUGIN_ID, errorMessage);
    }
    return super.validateLinkedResource();
  }

  public String getDiagramName() {
    if (StringUtils.isNotBlank(super.getFileName())) {
      return super.getFileName();
    }
    return null;
  }
}
