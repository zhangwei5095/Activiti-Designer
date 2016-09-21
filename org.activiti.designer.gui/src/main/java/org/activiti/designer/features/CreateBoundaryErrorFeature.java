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
package org.activiti.designer.features;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.PluginImage;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateBoundaryErrorFeature extends AbstractCreateBPMNFeature {

  public static final String FEATURE_ID_KEY = "boundaryerror";

  public CreateBoundaryErrorFeature(IFeatureProvider fp) {
    // set name and description of the creation feature
    super(fp, "ErrorBoundaryEvent", "Add error boundary event");
  }

  public boolean canCreate(ICreateContext context) {
    Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    if (parentObject instanceof SubProcess == true || parentObject instanceof CallActivity == true || 
        parentObject instanceof ServiceTask == true || parentObject instanceof ScriptTask == true) {
      return true;
    }
    return false;
  }

  public Object[] create(ICreateContext context) {
    BoundaryEvent boundaryEvent = new BoundaryEvent();
    ErrorEventDefinition errorEvent = new ErrorEventDefinition();
    boundaryEvent.getEventDefinitions().add(errorEvent);

    Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    ((Activity) parentObject).getBoundaryEvents().add(boundaryEvent);
    boundaryEvent.setAttachedToRef((Activity) parentObject);
    
    addObjectToContainer(context, boundaryEvent, "Error");

    // return newly created business object(s)
    return new Object[] { boundaryEvent };
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_EVENT_ERROR.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }
}
