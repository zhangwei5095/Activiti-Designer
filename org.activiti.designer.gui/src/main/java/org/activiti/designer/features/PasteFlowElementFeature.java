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
/**
 * 
 */
package org.activiti.designer.features;

import java.util.List;

import org.activiti.bpmn.model.FlowElement;
import org.activiti.designer.util.CloneUtil;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IAddFeature;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IPasteContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.AreaContext;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.ui.features.AbstractPasteFeature;

/**
 * Paste feature for flow elements.
 * 
 * @author Tiese Barrell
 * @since 0.6.1
 * @version 1
 * 
 */
public class PasteFlowElementFeature extends AbstractPasteFeature {

  public static final int PASTE_OFFSET = 25;

  public PasteFlowElementFeature(IFeatureProvider fp) {
    super(fp);
  }

  public boolean canPaste(IPasteContext context) {
    // only support pasting directly in the diagram (nothing else selected)
    PictogramElement[] pes = context.getPictogramElements();

    if (pes.length != 1 || !isPasteableContext(pes)) {
      return false;
    }
    
    List<FlowElement> copyList = ModelHandler.getModel(EcoreUtil.getURI(getDiagram())).getClipboard();
    if(copyList.size() > 0) {
    	return true;
    } else {
    	return false;
    }
  }

  private boolean isPasteableContext(PictogramElement[] pes) {
    return (pes[0] instanceof Diagram) || (pes[0] instanceof ContainerShape);
  }

  public void paste(IPasteContext context) {
    // we already verified, that we paste directly in the diagram
  	BpmnMemoryModel model = ModelHandler.getModel(EcoreUtil.getURI(getDiagram()));
  	List<FlowElement> copyList = model.getClipboard();

    for (FlowElement element : copyList) {
      FlowElement clone = CloneUtil.clone(element, getDiagram());
      
      AddContext addContext = new AddContext(new AreaContext(), clone);
			IAddFeature addFeature = getFeatureProvider().getAddFeature(addContext);
			PictogramElement pictogram = getFeatureProvider().getPictogramElementForBusinessObject(element);
      addContext.setLocation(pictogram.getGraphicsAlgorithm().getX() + PASTE_OFFSET, pictogram.getGraphicsAlgorithm().getY() + PASTE_OFFSET);
      addContext.setSize(pictogram.getGraphicsAlgorithm().getWidth(), pictogram.getGraphicsAlgorithm().getHeight());
      addContext.setTargetContainer(getDiagram());
      if(addFeature.canAdd(addContext)) {
	      addFeature.add(addContext);
      }
    }
  }
}
