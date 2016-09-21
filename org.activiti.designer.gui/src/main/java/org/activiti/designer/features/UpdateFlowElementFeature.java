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

import java.util.Iterator;

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.MultiInstanceLoopCharacteristics;
import org.activiti.designer.PluginImage;
import org.activiti.designer.controller.TaskShapeController;
import org.activiti.designer.eclipse.common.ActivitiPlugin;
import org.activiti.designer.util.TextUtil;
import org.activiti.designer.util.bpmn.BpmnExtensionUtil;
import org.apache.commons.lang.StringUtils;
import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.IReason;
import org.eclipse.graphiti.features.context.IUpdateContext;
import org.eclipse.graphiti.features.impl.AbstractUpdateFeature;
import org.eclipse.graphiti.features.impl.Reason;
import org.eclipse.graphiti.mm.algorithms.Ellipse;
import org.eclipse.graphiti.mm.algorithms.GraphicsAlgorithm;
import org.eclipse.graphiti.mm.algorithms.Image;
import org.eclipse.graphiti.mm.algorithms.MultiText;
import org.eclipse.graphiti.mm.algorithms.Text;
import org.eclipse.graphiti.mm.algorithms.styles.LineStyle;
import org.eclipse.graphiti.mm.pictograms.ConnectionDecorator;
import org.eclipse.graphiti.mm.pictograms.ContainerShape;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.services.IPeCreateService;

public class UpdateFlowElementFeature extends AbstractUpdateFeature {

	public UpdateFlowElementFeature(IFeatureProvider fp) {
		super(fp);
	}

	public boolean canUpdate(IUpdateContext context) {
	  Object bo = getBusinessObjectForPictogramElement(context.getPictogramElement());
    return (bo instanceof FlowElement);
	}

  public IReason updateNeeded(IUpdateContext context) {
		String pictogramName = null;
		PictogramElement pictogramElement = context.getPictogramElement();
    Object bo = getBusinessObjectForPictogramElement(pictogramElement);
    
    FlowElement flowElement = (FlowElement) bo;
    boolean hasMiSequentialImage = false;
    boolean hasMiParallelImage = false;
    boolean hasCompensationImage = false;
    
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			
			if (bo instanceof BoundaryEvent && cs.getGraphicsAlgorithm() instanceof Ellipse) {
			  BoundaryEvent event = (BoundaryEvent) bo;
			  Iterator<GraphicsAlgorithm> itGraph = cs.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().iterator();
			  while (itGraph.hasNext()) {
			    GraphicsAlgorithm childGraph = itGraph.next();
			    if (childGraph instanceof Ellipse) {
			      Ellipse ellipse = (Ellipse) childGraph;
			      if (ellipse.getLineStyle() == LineStyle.SOLID && event.isCancelActivity() == false) {
			        return Reason.createTrueReason();
			      
			      } else if (ellipse.getLineStyle() == LineStyle.DOT && event.isCancelActivity()) {
			        return Reason.createTrueReason();
			      }
			    }
			  }
			}
			
			for (Shape shape : cs.getChildren()) {
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					pictogramName = text.getValue();
					
				} else if (shape.getGraphicsAlgorithm() instanceof MultiText) {
				  MultiText text = (MultiText) shape.getGraphicsAlgorithm();
          pictogramName = text.getValue();
        
				} else if (shape.getGraphicsAlgorithm() instanceof Image) {
				  Image image = (Image) shape.getGraphicsAlgorithm();
				  if (image.getId().endsWith(PluginImage.IMG_MULTIINSTANCE_SEQUENTIAL.getImageKey())) {
				    hasMiSequentialImage = true;
				  
				  } else if (image.getId().endsWith(PluginImage.IMG_MULTIINSTANCE_PARALLEL.getImageKey())) {
				    hasMiParallelImage = true;
				  
				  } else if (image.getId().endsWith(PluginImage.IMG_ACTIVITY_COMPENSATION.getImageKey())) {
            hasCompensationImage = true;
          }
				}
			}
		} else if (pictogramElement instanceof FreeFormConnection) {
		
      EList<ConnectionDecorator> decoratorList = ((FreeFormConnection) pictogramElement).getConnectionDecorators();
      for (ConnectionDecorator decorator : decoratorList) {
        if (decorator.getGraphicsAlgorithm() instanceof MultiText) {
          MultiText text = (MultiText) decorator.getGraphicsAlgorithm();
          pictogramName = text.getValue();
        }
      }
		}
		
		if (bo instanceof Activity) {
		  
      Activity activity = (Activity) bo;
      MultiInstanceLoopCharacteristics multiInstanceObject = null;
      
      if (activity.getLoopCharacteristics() != null) {
      
        if (StringUtils.isNotEmpty(activity.getLoopCharacteristics().getLoopCardinality()) ||
            StringUtils.isNotEmpty(activity.getLoopCharacteristics().getInputDataItem()) ||
            StringUtils.isNotEmpty(activity.getLoopCharacteristics().getCompletionCondition())) {
          
          multiInstanceObject = activity.getLoopCharacteristics();
        }
      }
      
      if (multiInstanceObject != null) {
        if (multiInstanceObject.isSequential() && hasMiParallelImage) {
          return Reason.createTrueReason();
        
        } else if (multiInstanceObject.isSequential() == false && hasMiSequentialImage) {
          return Reason.createTrueReason();
        }
      
      } else if (hasMiParallelImage || hasMiSequentialImage) {
        return Reason.createTrueReason();
      }
      
      if (activity.isForCompensation() && hasCompensationImage == false) {
        return Reason.createTrueReason();
        
      } else if (activity.isForCompensation() == false && hasCompensationImage) {
        return Reason.createTrueReason();
      }
    }

		String businessName = BpmnExtensionUtil.getFlowElementName(flowElement, ActivitiPlugin.getDefault());

		// update needed, if names are different
		boolean updateNameNeeded = ((pictogramName == null && businessName != null) || 
				(pictogramName != null && !pictogramName.equals(businessName)));
		
		if (updateNameNeeded) {
			return Reason.createTrueReason(); //$NON-NLS-1$
		} else {
			return Reason.createFalseReason();
		}
	}

	public boolean update(IUpdateContext context) {
		// retrieve name from business model
		String businessName = null;
		PictogramElement pictogramElement = context.getPictogramElement();
		Object bo = getBusinessObjectForPictogramElement(pictogramElement);
		if (bo instanceof FlowElement) {
			FlowElement flowElement = (FlowElement) bo;
			businessName = BpmnExtensionUtil.getFlowElementName(flowElement, ActivitiPlugin.getDefault());
		}
		
		boolean updated = false;

		// Set name in pictogram model
		if (pictogramElement instanceof ContainerShape) {
			ContainerShape cs = (ContainerShape) pictogramElement;
			
			if (bo instanceof BoundaryEvent && cs.getGraphicsAlgorithm() instanceof Ellipse) {
			  BoundaryEvent event = (BoundaryEvent) bo;
        Iterator<GraphicsAlgorithm> itGraph = cs.getGraphicsAlgorithm().getGraphicsAlgorithmChildren().iterator();
        while (itGraph.hasNext()) {
          GraphicsAlgorithm childGraph = itGraph.next();
          if (childGraph instanceof Ellipse) {
            Ellipse ellipse = (Ellipse) childGraph;
            if (event.isCancelActivity()) {
              ellipse.setLineStyle(LineStyle.SOLID);
            } else {
              ellipse.setLineStyle(LineStyle.DOT);
            }
            
            Iterator<GraphicsAlgorithm> itChildGraph = childGraph.getGraphicsAlgorithmChildren().iterator();
            while (itChildGraph.hasNext()) {
              GraphicsAlgorithm innerChildGraph = itChildGraph.next();
              if (innerChildGraph instanceof Ellipse) {
                Ellipse innerEllipse = (Ellipse) innerChildGraph;
                if (event.isCancelActivity()) {
                  innerEllipse.setLineStyle(LineStyle.SOLID);
                } else {
                  innerEllipse.setLineStyle(LineStyle.DOT);
                }
              }
            }
            
            updated = true;
          }
        }
      }
			
			Iterator<Shape> itShape = cs.getChildren().iterator();
			while (itShape.hasNext()) {
			  Shape shape = itShape.next();
				if (shape.getGraphicsAlgorithm() instanceof Text) {
					Text text = (Text) shape.getGraphicsAlgorithm();
					text.setValue(businessName);
					updated = true;
					
				} else if (shape.getGraphicsAlgorithm() instanceof MultiText) {
					MultiText text = (MultiText) shape.getGraphicsAlgorithm();
					text.setValue(businessName);
					updated = true;
				
				} else if (shape.getGraphicsAlgorithm() instanceof Image) {
          Image image = (Image) shape.getGraphicsAlgorithm();
          if (image.getId().endsWith(PluginImage.IMG_MULTIINSTANCE_SEQUENTIAL.getImageKey())) {
            itShape.remove();
            updated = true;
          
          } else if (image.getId().endsWith(PluginImage.IMG_MULTIINSTANCE_PARALLEL.getImageKey())) {
            itShape.remove();
            updated = true;
          
          } else if (image.getId().endsWith(PluginImage.IMG_ACTIVITY_COMPENSATION.getImageKey())) {
            itShape.remove();
            updated = true;
          }
				}
			}
			
			if (bo instanceof Activity) {
			  Activity activity = (Activity) bo;
			  MultiInstanceLoopCharacteristics multiInstanceObject = activity.getLoopCharacteristics();
			  
			  final IPeCreateService peCreateService = Graphiti.getPeCreateService();
        final IGaService gaService = Graphiti.getGaService();
        int imageX = (cs.getGraphicsAlgorithm().getWidth() - TaskShapeController.MI_IMAGE_SIZE) / 2;
			  
	      if (multiInstanceObject != null) {
	      
	        if (StringUtils.isNotEmpty(multiInstanceObject.getLoopCardinality()) ||
	            StringUtils.isNotEmpty(multiInstanceObject.getInputDataItem()) ||
	            StringUtils.isNotEmpty(multiInstanceObject.getCompletionCondition())) {
	          
	          if (multiInstanceObject.isSequential()) {
	            final Shape miShape = peCreateService.createShape(cs, false);
	            final Image miImage = gaService.createImage(miShape, PluginImage.IMG_MULTIINSTANCE_SEQUENTIAL.getImageKey());
	            gaService.setLocationAndSize(miImage, imageX, 
	                    (cs.getGraphicsAlgorithm().getHeight() - TaskShapeController.MI_IMAGE_SIZE) - 2, TaskShapeController.MI_IMAGE_SIZE, TaskShapeController.MI_IMAGE_SIZE);
	          
	          } else {
              final Shape miShape = peCreateService.createShape(cs, false);
              final Image miImage = gaService.createImage(miShape, PluginImage.IMG_MULTIINSTANCE_PARALLEL.getImageKey());
              gaService.setLocationAndSize(miImage, imageX, 
                      (cs.getGraphicsAlgorithm().getHeight() - TaskShapeController.MI_IMAGE_SIZE) - 2, TaskShapeController.MI_IMAGE_SIZE, TaskShapeController.MI_IMAGE_SIZE);
	          }
	        }
	      }
	      
	      if (activity.isForCompensation()) {
	        final Shape compensationShape = peCreateService.createShape(cs, false);
          final Image compensationImage = gaService.createImage(compensationShape, PluginImage.IMG_ACTIVITY_COMPENSATION.getImageKey());
          gaService.setLocationAndSize(compensationImage, imageX + TaskShapeController.MI_IMAGE_SIZE + 5, 
                  (cs.getGraphicsAlgorithm().getHeight() - TaskShapeController.MI_IMAGE_SIZE) - 2, TaskShapeController.MI_IMAGE_SIZE, TaskShapeController.MI_IMAGE_SIZE);
	      }
			
			} else if (bo instanceof BoundaryEvent) {
			  BoundaryEvent event = (BoundaryEvent) bo;
			  if (event.isCancelActivity() == false) {
			    
			  }
			}
		
		} else if (pictogramElement instanceof FreeFormConnection) {
    
      EList<ConnectionDecorator> decoratorList = ((FreeFormConnection) pictogramElement).getConnectionDecorators();
      for (ConnectionDecorator decorator : decoratorList) {
        if (decorator.getGraphicsAlgorithm() instanceof MultiText) {
          MultiText text = (MultiText) decorator.getGraphicsAlgorithm();
          text.setValue(businessName);
          TextUtil.setTextSize(text);
          updated = true;
        }
      }
    }
		
		return updated;
	}
}
