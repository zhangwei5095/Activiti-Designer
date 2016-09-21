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
package org.activiti.designer.property;

import java.util.Collection;
import java.util.Iterator;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.Message;
import org.activiti.bpmn.model.MessageEventDefinition;
import org.activiti.designer.util.eclipse.ActivitiUiUtil;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.platform.IDiagramContainer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;

public class MessagePropertyUtil {
	
  public static String[] fillMessageCombo(Combo messageCombo, SelectionListener selectionListener, Diagram diagram) {
    messageCombo.removeSelectionListener(selectionListener);
    BpmnMemoryModel memoryModel = ModelHandler.getModel(EcoreUtil.getURI(diagram));
    BpmnModel model = memoryModel.getBpmnModel();
    String[] messageArray = new String[model.getMessages().size()];
    Collection<Message> messages = model.getMessages();
    int counter = 0;
    Iterator<Message> itMessage = messages.iterator();
    while (itMessage.hasNext()) {
      Message message = itMessage.next();
      messageArray[counter] = message.getName() + " (" + message.getId() + ")";
      counter++;
    }
    messageCombo.setItems(messageArray);
    messageCombo.select(0);
    messageCombo.addSelectionListener(selectionListener);
    return messageArray;
  }


  public static String getMessageValue(final Event event, final Diagram diagram, final IDiagramContainer diagramContainer) {
    if (event.getEventDefinitions().get(0) != null) {
      final MessageEventDefinition messageDefinition = (MessageEventDefinition) event.getEventDefinitions().get(0);
      final BpmnMemoryModel memoryModel = ModelHandler.getModel(EcoreUtil.getURI(diagram));
      final BpmnModel model = memoryModel.getBpmnModel();
      if (StringUtils.isNotEmpty(messageDefinition.getMessageRef())) {
        for (Message message : model.getMessages()) {
          if (message.getId() != null && message.getId().equals(messageDefinition.getMessageRef())) {
            return message.getName() + " (" + message.getId() + ")";
          }
        }
        
      } else {
        if (model.getMessages().size() > 0) {
          final Runnable runnable = new Runnable() {
            public void run() {
              Message message = model.getMessages().iterator().next();
              messageDefinition.setMessageRef(message.getId());
            }
          };
          
          TransactionalEditingDomain editingDomain = diagramContainer.getDiagramBehavior().getEditingDomain();
          ActivitiUiUtil.runModelChange(runnable, editingDomain, "Model Update");
          
        }
      }
    }
    return null;
  }

  public static void storeMessageValue(Combo messageCombo, Event event, Diagram diagram) {
    MessageEventDefinition messageDefinition = (MessageEventDefinition) event.getEventDefinitions().get(0);
    String messageKey = messageCombo.getItem(messageCombo.getSelectionIndex());
    messageDefinition.setMessageRef(messageKey.substring(messageKey.lastIndexOf("(") + 1, messageKey.length() - 1));
  }
}
