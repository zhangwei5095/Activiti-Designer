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

import java.util.List;

import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.Pool;
import org.activiti.bpmn.model.Process;
import org.activiti.designer.PluginImage;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.ICreateContext;
import org.eclipse.graphiti.features.context.impl.AddContext;
import org.eclipse.graphiti.features.context.impl.ResizeShapeContext;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.Diagram;

public class CreateLaneFeature extends AbstractCreateBPMNFeature {

  public static final String FEATURE_ID_KEY = "lane";

  public CreateLaneFeature(IFeatureProvider fp) {
    super(fp, "Lane", "Add lane");
  }

  @Override
  public boolean canCreate(ICreateContext context) {
    if (context.getTargetContainer() instanceof Diagram)
      return false;

    Object parentBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetContainer());
    if (parentBo instanceof Pool || parentBo instanceof Lane) {
      return true;
    }

    return false;
  }

  @Override
  public Object[] create(ICreateContext context) {

    BpmnMemoryModel model = ModelHandler.getModel(EcoreUtil.getURI(getDiagram()));
    Object parentBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetContainer());
    Pool parentPool = null;
    if (parentBo instanceof Pool) {
      parentPool = (Pool) parentBo;
    } else {
      Lane lane = (Lane) parentBo;
      for (Pool pool : model.getBpmnModel().getPools()) {
        if (pool.getProcessRef().equals(lane.getParentProcess().getId())) {
          parentPool = pool;
          break;
        }
      }
    }

    if (parentPool == null)
      return null;

    ContainerShape poolShape = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(parentPool);
    Process poolProcess = model.getBpmnModel().getProcess(parentPool.getId());

    if (poolProcess == null)
      return null;

    List<Lane> lanes = poolProcess.getLanes();
    int x = 0;
    int y = 0;
    int width = 0;
    int height = 0;
    if (lanes.size() == 0) {
      x = 20;
      y = 0;
      width = poolShape.getGraphicsAlgorithm().getWidth() - 20;
      height = poolShape.getGraphicsAlgorithm().getHeight();

    } else {
      ContainerShape lastLaneShape = null;
      for (int i = lanes.size() - 1; i >= 0; i--) {
        lastLaneShape = (ContainerShape) getFeatureProvider().getPictogramElementForBusinessObject(lanes.get(i));
        if (lastLaneShape != null) {
          break;
        }
      }
      
      if (lastLaneShape != null) {
        x = lastLaneShape.getGraphicsAlgorithm().getX();
        y = lastLaneShape.getGraphicsAlgorithm().getY() + lastLaneShape.getGraphicsAlgorithm().getHeight();
        width = lastLaneShape.getGraphicsAlgorithm().getWidth();
        height = lastLaneShape.getGraphicsAlgorithm().getHeight();
        
      } else {
        x = 20;
        y = 0;
        width = poolShape.getGraphicsAlgorithm().getWidth() - 20;
        height = poolShape.getGraphicsAlgorithm().getHeight();
      }
    }

    Lane newLane = new Lane();
    newLane.setId(getNextId(newLane));
    newLane.setName("New lane");
    newLane.setParentProcess(poolProcess);
    poolProcess.getLanes().add(newLane);

    ResizeShapeContext resizeContext = new ResizeShapeContext(poolShape);
    resizeContext.setSize(poolShape.getGraphicsAlgorithm().getWidth(), poolShape.getGraphicsAlgorithm().getHeight() + height);
    resizeContext.setLocation(poolShape.getGraphicsAlgorithm().getX(), poolShape.getGraphicsAlgorithm().getY());
    resizeContext.setDirection(ResizeShapeContext.DIRECTION_SOUTH);
    resizeContext.putProperty("org.activiti.designer.lane.create", true);
    getFeatureProvider().getResizeShapeFeature(resizeContext).execute(resizeContext);

    context.putProperty("org.activiti.designer.lane.x", x);
    context.putProperty("org.activiti.designer.lane.y", y);
    context.putProperty("org.activiti.designer.lane.width", width);
    context.putProperty("org.activiti.designer.lane.height", height);

    AddContext addContext = new AddContext();
    addContext.setNewObject(newLane);
    addContext.setLocation(x, y);
    addContext.setSize(width, height);
    addContext.setTargetContainer(poolShape);
    getFeatureProvider().addIfPossible(addContext);

    // return newly created business object(s)
    return new Object[] { newLane };
  }

  @Override
  public String getCreateImageId() {
    return PluginImage.IMG_LANE.getImageKey();
  }

  @Override
  protected String getFeatureIdKey() {
    return FEATURE_ID_KEY;
  }
}
