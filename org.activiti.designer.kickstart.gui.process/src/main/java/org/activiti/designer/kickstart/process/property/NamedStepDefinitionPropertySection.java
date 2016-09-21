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
package org.activiti.designer.kickstart.process.property;

import org.activiti.workflow.simple.definition.NamedStepDefinition;
import org.activiti.workflow.simple.definition.StepDefinition;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * Section containing all properties in the main {@link StepDefinition}.
 * 
 * @author Frederik Hereman
 */
public class NamedStepDefinitionPropertySection extends AbstractKickstartProcessPropertySection {

  protected Text nameControl;
  protected Text descriptionControl;
  
  @Override
  protected void createFormControls(TabbedPropertySheetPage aTabbedPropertySheetPage) {
    nameControl = createTextControl(false);
    createLabel("Name", nameControl);
    descriptionControl = createTextControl(true);
    createLabel("Description", descriptionControl);
  }

  @Override
  protected Object getModelValueForControl(Control control, Object businessObject) {
    NamedStepDefinition stepDefinition = (NamedStepDefinition) businessObject;
    if(control == nameControl) {
      return stepDefinition.getName() != null ? stepDefinition.getName() : "";
    } else if(control == descriptionControl) {
      return stepDefinition.getDescription() != null ? stepDefinition.getDescription() : "";
    }
    return null;
  }

  @Override
  protected void storeValueInModel(Control control, Object businessObject) {
    NamedStepDefinition stepDefinition = (NamedStepDefinition) businessObject;
    if(control == nameControl) {
      stepDefinition.setName(nameControl.getText());
    } else if(control == descriptionControl) {
      stepDefinition.setDescription(descriptionControl.getText());
    }
  }
  

}
