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

/**
 * @author Tijs Rademakers
 */

import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.designer.PluginImage;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;

public class CreateSignalStartEventFeature extends AbstractCreateBPMNFeature {

  public static final String FEATURE_ID_KEY = "signalstartevent";

  public CreateSignalStartEventFeature(IFeatureProvider fp) {
    // set name and description of the creation feature
    super(fp, "SignalStartEvent", "Add signal start event");
  }

  public boolean canCreate(ICreateContext context) {
    Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    if (parentObject instanceof EventSubProcess)
      return true;
    return super.canCreate(context);
  }

  public Object[] create(ICreateContext context) {
    StartEvent startEvent = new StartEvent();
    SignalEventDefinition signalEvent = new SignalEventDefinition();
    startEvent.getEventDefinitions().add(signalEvent);
    addObjectToContainer(context, startEvent, "Signal start");

    // return newly created business object(s)
    return new Object[] { startEvent };
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_EVENT_SIGNAL.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }
}
