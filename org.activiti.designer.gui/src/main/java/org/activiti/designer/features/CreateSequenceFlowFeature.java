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
import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.CompensateEventDefinition;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.FlowNode;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.PluginImage;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.editor.ModelHandler;
import org.activiti.designer.util.preferences.Preferences;
import org.activiti.designer.util.preferences.PreferencesUtil;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateConnectionContext;
import org.eclipse.graphiti.features.context.impl.AddConnectionContext;
import org.eclipse.graphiti.mm.pictograms.Anchor;
import org.eclipse.graphiti.mm.pictograms.Connection;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateSequenceFlowFeature extends AbstractCreateBPMNConnectionFeature {

  public static final String FEATURE_ID_KEY = "flow";

  public CreateSequenceFlowFeature(IFeatureProvider fp) {
    // provide name and description for the UI, e.g. the palette
    super(fp, "SequenceFlow", "Create SequenceFlow"); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public boolean canCreate(ICreateConnectionContext context) {
    FlowNode source = getFlowNode(context.getSourceAnchor());
    FlowNode target = getFlowNode(context.getTargetAnchor());
    if (source != null && target != null && source != target) {
      if (source instanceof StartEvent && target instanceof StartEvent) {
        return false;
      } else if (source instanceof EndEvent) {
        // prevent adding outgoing connections from EndEvents
        return false;
      } else {
        for (SequenceFlow flow : source.getOutgoingFlows()) {
          if (flow.getTargetRef().equals(target.getId())) {
            return false;
          }
        }
        
        if (source instanceof BoundaryEvent) {
          BoundaryEvent event = (BoundaryEvent) source;
          if (event.getEventDefinitions().size() > 0 && event.getEventDefinitions().get(0) instanceof CompensateEventDefinition) {
            return false;
          }
        }
        
        BpmnModel bpmnModel = ModelHandler.getModel(EcoreUtil.getURI(getDiagram())).getBpmnModel();
        Process sourceProcess = null;
        Process targetProcess = null;
        for (Process process : bpmnModel.getProcesses()) {
          if (process.getFlowElementRecursive(source.getId()) != null) {
            sourceProcess = process;
          }
          
          if (process.getFlowElementRecursive(target.getId()) != null) {
            targetProcess = process;
          }
        }
        
        if (sourceProcess != null && targetProcess != null && sourceProcess.equals(targetProcess) == false) {
          return false;
        }
        
        return true;
      }
    }
    return false;
  }

  public boolean canStartConnection(ICreateConnectionContext context) {
    // return true if source anchor isn't undefined
    if (getFlowNode(context.getSourceAnchor()) != null) {
      return true;
    }
    return false;
  }

  public Connection create(ICreateConnectionContext context) {
    Connection newConnection = null;

    FlowNode source = getFlowNode(context.getSourceAnchor());
    FlowNode target = getFlowNode(context.getTargetAnchor());

    if (source != null && target != null) {
      // create new business object
      SequenceFlow sequenceFlow = createSequenceFlow(source, target, context);

      // add connection for business object
      AddConnectionContext addContext = new AddConnectionContext(context.getSourceAnchor(), context.getTargetAnchor());
      addContext.setNewObject(sequenceFlow);
      newConnection = (Connection) getFeatureProvider().addIfPossible(addContext);
    }
    return newConnection;
  }

  /**
   * Returns the FlowNode belonging to the anchor, or null if not available.
   */
  private FlowNode getFlowNode(Anchor anchor) {
    if (anchor != null) {
      Object obj = getBusinessObjectForPictogramElement(anchor.getParent());
      if (obj instanceof FlowNode) {
        return (FlowNode) obj;
      }
    }
    return null;
  }

  /**
   * Creates a SequenceFlow between two BaseElements.
   */
  protected SequenceFlow createSequenceFlow(FlowNode source, FlowNode target, ICreateConnectionContext context) {
    SequenceFlow sequenceFlow = new SequenceFlow();

    sequenceFlow.setId(getNextId());
    sequenceFlow.setSourceRef(source.getId());
    sequenceFlow.setTargetRef(target.getId());

    if (PreferencesUtil.getBooleanPreference(Preferences.EDITOR_ADD_LABELS_TO_NEW_SEQUENCEFLOWS, ActivitiPlugin.getDefault())) {
      sequenceFlow.setName(String.format("to %s", target.getName()));
    } else {
      sequenceFlow.setName("");
    }

    ContainerShape targetContainer = null;
    if (source instanceof BoundaryEvent) {
      BoundaryEvent boundaryEvent = (BoundaryEvent) source;
      if (boundaryEvent.getAttachedToRef() != null) {
        Activity attachedActivity = boundaryEvent.getAttachedToRef();
        targetContainer = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(attachedActivity);
      }
    } else {
      targetContainer = (ContainerShape) context.getSourcePictogramElement();
    }
      
    ContainerShape parentContainer = targetContainer.getContainer();
    if (parentContainer instanceof Diagram) {
      ModelHandler.getModel(EcoreUtil.getURI(getDiagram())).getBpmnModel().getMainProcess().addFlowElement(sequenceFlow);

    } else {
      Object parentObject = getBusinessObjectForPictogramElement(parentContainer);
      if (parentObject instanceof SubProcess) {
        ((SubProcess) parentObject).addFlowElement(sequenceFlow);

      } else if (parentObject instanceof Lane) {
        Lane lane = (Lane) parentObject;
        lane.getParentProcess().addFlowElement(sequenceFlow);
      }
    }
    
    source.getOutgoingFlows().add(sequenceFlow);
    target.getIncomingFlows().add(sequenceFlow);
    return sequenceFlow;
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_EREFERENCE.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }

  @Override
  protected Class< ? extends BaseElement> getFeatureClass() {
    return SequenceFlow.class;
  }

}
