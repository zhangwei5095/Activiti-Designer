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
package org.activiti.designer.kickstart.form.features;

import org.activiti.designer.kickstart.form.diagram.KickstartFormFeatureProvider;
import org.activiti.designer.kickstart.form.diagram.layout.KickstartFormLayouter;
import org.activiti.designer.util.editor.KickstartFormMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinitionContainer;
import org.activiti.workflow.simple.definition.form.FormPropertyGroup;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.ICustomUndoableFeature;
import org.eclipse.graphiti.features.context.IContext;
import org.eclipse.graphiti.features.context.IDeleteContext;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.ui.features.DefaultDeleteFeature;

public class DeleteFormComponentFeature extends DefaultDeleteFeature implements ICustomUndoableFeature {

  protected Object deletedObject;
  protected FormPropertyDefinitionContainer definitionContainer;
  
  public DeleteFormComponentFeature(KickstartFormFeatureProvider fp) {
    super(fp);
  }
  
  @Override
  public boolean canDelete(IDeleteContext context) {
    boolean canDelete = false;
    Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
    if(bo != null) {
      // Any object can be deleted, apart from the "info" group
      canDelete = !(bo instanceof FormPropertyGroup) || (bo instanceof FormPropertyGroup && 
          !KickstartFormMemoryModel.INFO_GROUP_ID.equals(((FormPropertyGroup) bo).getId()));
    }
    return canDelete;
  }
  
  
  @Override
  public void delete(IDeleteContext context) {
    ContainerShape parent = null;
    if(context.getPictogramElement() instanceof ContainerShape) {
      parent = ((ContainerShape)context.getPictogramElement()).getContainer();
      
      definitionContainer = (FormPropertyDefinitionContainer) getBusinessObjectForPictogramElement(parent);
      deletedObject = getBusinessObjectForPictogramElement(context.getPictogramElement());
      
      super.delete(context);
      
      // Call redo, which contains the model-update only
      redo(context);
      
      // When deleting, force a re-layout of the parent container after shape has been removed
      if(context.getPictogramElement() instanceof ContainerShape) {
        getFormLayouter().relayout(parent);
      }
    } else {
      super.delete(context);
    }
  }
  
  protected KickstartFormLayouter getFormLayouter() {
    return ((KickstartFormFeatureProvider)getFeatureProvider()).getFormLayouter(); 
  }

  @Override
  public void undo(IContext context) {
    KickstartFormMemoryModel model = (ModelHandler.getKickstartFormMemoryModel(EcoreUtil.getURI(getDiagram())));
    if(model != null && model.isInitialized() && deletedObject != null) {
      // Just add the definition to the model at the end, relayouting of the container
      // will cause the right order to be restored as it was before the delete 
      if(deletedObject instanceof FormPropertyDefinition) {
        definitionContainer.addFormProperty((FormPropertyDefinition) deletedObject);
      } else if(deletedObject instanceof FormPropertyGroup) {
        model.getFormDefinition().getFormGroups().add((FormPropertyGroup) deletedObject);
      }
    }
    
    if(((IDeleteContext)context).getPictogramElement() instanceof ContainerShape) {
      // Perform the re-layout as part of the transaction
      getFormLayouter().relayout((ContainerShape) ((IDeleteContext)context).getPictogramElement());
    }
  }

  @Override
  public boolean canRedo(IContext context) {
    return deletedObject != null;
  }

  @Override
  public void redo(IContext context) {
    KickstartFormMemoryModel model = (ModelHandler.getKickstartFormMemoryModel(EcoreUtil.getURI(getDiagram())));
    if(model != null && model.isInitialized()) {
      if(deletedObject instanceof FormPropertyDefinition) {
        definitionContainer.removeFormProperty((FormPropertyDefinition) deletedObject);
      } else if(deletedObject instanceof FormPropertyGroup) {
        model.getFormDefinition().getFormGroups().remove(deletedObject);
      }
    }
  }
}
