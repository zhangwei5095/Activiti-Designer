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

import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IRemoveFeature;
import org.eclipse.graphiti.features.context.ICustomContext;
import org.eclipse.graphiti.features.context.IRemoveContext;
import org.eclipse.graphiti.features.context.impl.RemoveContext;
import org.eclipse.graphiti.features.custom.AbstractCustomFeature;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;

public class DeletePoolFeature extends AbstractCustomFeature {

  public DeletePoolFeature(IFeatureProvider fp) {
    super(fp);
  }

  @Override
  public String getName() {
    return "Delete pool"; //$NON-NLS-1$
  }

  @Override
  public String getDescription() {
    return "Delete pool"; //$NON-NLS-1$
  }

  @Override
  public boolean canExecute(ICustomContext context) {
    if(context.getPictogramElements() == null) return false;
    for (PictogramElement pictogramElement : context.getPictogramElements()) {
      if(getBusinessObjectForPictogramElement(pictogramElement) == null) continue;
      Object boObject = getBusinessObjectForPictogramElement(pictogramElement);
      if(boObject instanceof Pool == false) {
        return false;
      }
    }
    return true;
  }

  public void execute(ICustomContext context) {
    if(context.getPictogramElements() == null) return;
    
    for (final PictogramElement pictogramElement : context.getPictogramElements()) {
      if(getBusinessObjectForPictogramElement(pictogramElement) == null) continue;
      final Object boObject = getBusinessObjectForPictogramElement(pictogramElement);
      if(boObject instanceof Pool == true) {
        final Pool pool = (Pool) boObject;
        BpmnMemoryModel model = ModelHandler.getModel(EcoreUtil.getURI(getDiagram()));
        Process process = model.getBpmnModel().getProcess(pool.getId());
        model.getBpmnModel().getProcesses().remove(process);
        model.getBpmnModel().getPools().remove(pool);
        IRemoveContext rc = new RemoveContext(pictogramElement);
        IFeatureProvider featureProvider = getFeatureProvider();
        IRemoveFeature removeFeature = featureProvider.getRemoveFeature(rc);
        if (removeFeature != null) {
          removeFeature.remove(rc);
        }
      }
    }
  }
}
