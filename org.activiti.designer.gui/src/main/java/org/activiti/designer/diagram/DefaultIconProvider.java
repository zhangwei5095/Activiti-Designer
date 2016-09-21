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
package org.activiti.designer.diagram;

import java.util.List;

import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.BusinessRuleTask;
import org.activiti.bpmn.model.CallActivity;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.ErrorEventDefinition;
import org.activiti.bpmn.model.EventDefinition;
import org.activiti.bpmn.model.EventGateway;
import org.activiti.bpmn.model.EventSubProcess;
import org.activiti.bpmn.model.ExclusiveGateway;
import org.activiti.bpmn.model.InclusiveGateway;
import org.activiti.bpmn.model.IntermediateCatchEvent;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.ManualTask;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.ReceiveTask;
import org.activiti.bpmn.model.ScriptTask;
import org.activiti.bpmn.model.ServiceTask;
import org.activiti.bpmn.model.SignalEventDefinition;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.bpmn.model.TerminateEventDefinition;
import org.activiti.bpmn.model.ThrowEvent;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.activiti.bpmn.model.UserTask;
import org.activiti.bpmn.model.alfresco.AlfrescoStartEvent;
import org.activiti.designer.Activator;
import org.activiti.designer.PluginImage;
import org.activiti.designer.eclipse.extension.icon.IconProvider;
import org.activiti.designer.util.eclipse.ActivitiUiUtil;
import org.activiti.designer.util.extension.CustomServiceTaskContext;
import org.activiti.designer.util.extension.ExtensionUtil;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.graphiti.ui.internal.GraphitiUIPlugin;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;

/**
 * @author Tiese Barrell
 * 
 */
public class DefaultIconProvider implements IconProvider {

  private static final int PRIORITY = 100;

  /**
   * 
   */
  public DefaultIconProvider() {
    super();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.activiti.designer.eclipse.extension.icon.IconProvider#getPriority()
   */
  @Override
  public Integer getPriority() {
    return PRIORITY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.activiti.designer.eclipse.extension.icon.IconProvider#getIcon(java.
   * lang.Object)
   */
  @Override
  public Image getIcon(final Object context) {
    Image result = null;

    if (context instanceof Process) {
      result = Activator.getImage(PluginImage.IMG_SUBPROCESS_EXPANDED);
    } else if (context instanceof EventSubProcess) {
      result = Activator.getImage(PluginImage.IMG_EVENT_SUBPROCESS);
    } else if (context instanceof SubProcess) {
      result = Activator.getImage(PluginImage.IMG_SUBPROCESS_COLLAPSED);
    } else if (context instanceof Pool) {
      result = Activator.getImage(PluginImage.IMG_POOL);
    } else if (context instanceof ParallelGateway) {
      result = Activator.getImage(PluginImage.IMG_GATEWAY_PARALLEL);
    } else if (context instanceof ExclusiveGateway) {
      result = Activator.getImage(PluginImage.IMG_GATEWAY_EXCLUSIVE);
    } else if (context instanceof InclusiveGateway) {
      result = Activator.getImage(PluginImage.IMG_GATEWAY_INCLUSIVE);
    } else if (context instanceof EventGateway) {
      result = Activator.getImage(PluginImage.IMG_GATEWAY_EVENT);
    } else if (context instanceof Lane) {
      result = Activator.getImage(PluginImage.IMG_LANE);
    } else if (context instanceof ManualTask) {
      result = Activator.getImage(PluginImage.IMG_MANUALTASK);
    } else if (context instanceof UserTask) {
      result = Activator.getImage(PluginImage.IMG_USERTASK);
    } else if (context instanceof ServiceTask) {
      ServiceTask serviceTask = (ServiceTask) context;
      if (ServiceTask.MAIL_TASK.equalsIgnoreCase(serviceTask.getType())) {
        result = Activator.getImage(PluginImage.IMG_MAILTASK);
      } else {
        
        if (ExtensionUtil.isCustomServiceTask(context)) {
          DiagramEditor editor = (DiagramEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
          final List<CustomServiceTaskContext> customServiceTaskContexts = ExtensionUtil.getCustomServiceTaskContexts(
                  ActivitiUiUtil.getProjectFromDiagram(editor.getDiagramTypeProvider().getDiagram()));
          for (CustomServiceTaskContext customServiceTaskContext : customServiceTaskContexts) {
            if (customServiceTaskContext.getServiceTask().getId().equals(serviceTask.getExtensionId())) {
              @SuppressWarnings("restriction")
              final ImageRegistry reg = GraphitiUIPlugin.getDefault().getImageRegistry();
              return reg.get(editor.getDiagramTypeProvider().getProviderId() + "||" + customServiceTaskContext.getSmallImageKey());
            }
          }
        }
        
        result = Activator.getImage(PluginImage.IMG_SERVICETASK);
      }
    } else if (context instanceof ScriptTask) {
      result = Activator.getImage(PluginImage.IMG_SCRIPTTASK);
    } else if (context instanceof ReceiveTask) {
      result = Activator.getImage(PluginImage.IMG_RECEIVETASK);
    } else if (context instanceof BusinessRuleTask) {
      result = Activator.getImage(PluginImage.IMG_BUSINESSRULETASK);
    } else if (context instanceof CallActivity) {
      result = Activator.getImage(PluginImage.IMG_CALLACTIVITY);
    } else if (context instanceof StartEvent) {
      if(context instanceof AlfrescoStartEvent) {
        result = Activator.getImage(PluginImage.IMG_STARTEVENT_NONE);
      } else {
        if(((StartEvent) context).getEventDefinitions().size() > 0) {
          if(((StartEvent) context).getEventDefinitions().get(0) instanceof TimerEventDefinition) {
            result = Activator.getImage(PluginImage.IMG_EVENT_TIMER);
          } else {
            result = Activator.getImage(PluginImage.IMG_EVENT_ERROR);
          }
        } else {
          result = Activator.getImage(PluginImage.IMG_STARTEVENT_NONE);
        }
      }
      
    } else if (context instanceof EndEvent) {
      EndEvent endEvent = (EndEvent) context;
      for (EventDefinition eventDefinition : endEvent.getEventDefinitions()) {
        if (eventDefinition instanceof ErrorEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_ERROR);
        } else if (eventDefinition instanceof TerminateEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_TERMINATE);
        }
      }
      if (result == null) {
        result = Activator.getImage(PluginImage.IMG_ENDEVENT_NONE);
      }
    
    } else if (context instanceof BoundaryEvent) {
      if(((BoundaryEvent) context).getEventDefinitions().size() > 0) {
        EventDefinition definition = ((BoundaryEvent) context).getEventDefinitions().get(0);
        if (definition instanceof ErrorEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_ERROR);
        } else if (definition instanceof SignalEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_SIGNAL);
        } else if (definition instanceof MessageEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_MESSAGE);
        } else {
          result = Activator.getImage(PluginImage.IMG_EVENT_TIMER);
        }
      }
    } else if (context instanceof IntermediateCatchEvent) {
      if(((IntermediateCatchEvent) context).getEventDefinitions().size() > 0) {
        EventDefinition definition = ((IntermediateCatchEvent) context).getEventDefinitions().get(0);
        if(definition instanceof SignalEventDefinition) {
          result = Activator.getImage(PluginImage.IMG_EVENT_SIGNAL);
        } else {
          result = Activator.getImage(PluginImage.IMG_EVENT_TIMER);
        }
      }
    } else if (context instanceof ThrowEvent) {
      if(((ThrowEvent) context).getEventDefinitions().size() > 0) {
        result = Activator.getImage(PluginImage.IMG_EVENT_SIGNAL);
      } else {
        result = Activator.getImage(PluginImage.IMG_EVENT_TIMER);
      }
    } else {
      throw new IllegalArgumentException("This provider has no Icon for the provided context");
    }

    return result;
  }
}
