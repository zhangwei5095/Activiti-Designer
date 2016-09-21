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

import org.activiti.bpmn.model.Activity;
import org.activiti.bpmn.model.Artifact;
import org.activiti.bpmn.model.Association;
import org.activiti.bpmn.model.BoundaryEvent;
import org.activiti.bpmn.model.FlowElement;
import org.activiti.bpmn.model.Lane;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.SubProcess;
import org.activiti.designer.util.editor.BpmnMemoryModel;
import org.activiti.designer.util.editor.ModelHandler;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.graphiti.datatypes.ILocation;
import org.eclipse.graphiti.features.IFeatureProvider;
import org.eclipse.graphiti.features.context.IMoveShapeContext;
import org.eclipse.graphiti.features.impl.DefaultMoveShapeFeature;
import org.eclipse.graphiti.mm.algorithms.styles.Point;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.FreeFormConnection;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.mm.pictograms.Shape;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;

/**
 * This move feature takes care, that for all activity types, attached boundary events will move 
 * when the activity itself is moved.
 * 
 * @author bardioc
 */
public class MoveActivityFeature extends DefaultMoveShapeFeature {

  protected ILocation shapeLocationBeforeMove;
  
	/** Creates the feature */
	public MoveActivityFeature(IFeatureProvider fp) {
		super(fp);
	}
	
	@Override
  public boolean canMoveShape(IMoveShapeContext context) {
	  Object parentObject = getBusinessObjectForPictogramElement(context.getTargetContainer());
    return (context.getTargetContainer() instanceof Diagram || 
            parentObject instanceof SubProcess || parentObject instanceof Lane);
  }

	
	
  @Override
  protected void preMoveShape(IMoveShapeContext context) {
    final Shape shape = context.getShape();
    shapeLocationBeforeMove = Graphiti.getLayoutService().getLocationRelativeToDiagram(shape);
    super.preMoveShape(context);
  }
  
  

  @Override
  public void moveShape(IMoveShapeContext context) {
    preMoveShape(context);
    internalMove(context);
    postMoveShape(context);
  }

  /**
	 * Makes sure attached boundary events will be moved too, in case the shape itself is moved. 
	 * Determines the amount of pixels the shape moved, finds out all boundary events of the shape 
	 * and moves them the same delta.
	 * 
	 * @param context the context of the move
	 */
	@Override
	protected void postMoveShape(IMoveShapeContext context) {
		final Shape shape = context.getShape();

		// get the activity itself to determine its boundary events
		final Activity activity = (Activity) getBusinessObjectForPictogramElement(shape);
		ILocation shapeLocationAfterMove = Graphiti.getLayoutService().getLocationRelativeToDiagram(shape);
		moveActivityChilds(activity, shapeLocationAfterMove);
		
		BpmnMemoryModel model = ModelHandler.getModel(EcoreUtil.getURI(getDiagram()));
		
		if (context.getSourceContainer() != context.getTargetContainer()) {
		  if (context.getSourceContainer() instanceof Diagram == false) {
		    Object containerBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getSourceContainer());
		    if (containerBo instanceof SubProcess) {
		      SubProcess subProcess = (SubProcess) containerBo;
		      subProcess.removeFlowElement(activity.getId());
		      for (SequenceFlow flow : activity.getOutgoingFlows()) {
		        subProcess.removeFlowElement(flow.getId());
		      }
		      for (BoundaryEvent event : activity.getBoundaryEvents()) {
		        subProcess.removeFlowElement(event.getId());
		      }
		    } else if (containerBo instanceof Lane) {
		      Lane lane = (Lane) containerBo;
          lane.getFlowReferences().remove(activity.getId());
          lane.getParentProcess().removeFlowElement(activity.getId());
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            lane.getParentProcess().removeFlowElement(flow.getId());
          }
          for (BoundaryEvent event : activity.getBoundaryEvents()) {
            lane.getParentProcess().removeFlowElement(event.getId());
          }
        }
		  } else {
		    if (model.getBpmnModel().getMainProcess() != null) {
  		    model.getBpmnModel().getMainProcess().removeFlowElement(activity.getId());
  		    for (SequenceFlow flow : activity.getOutgoingFlows()) {
  		      model.getBpmnModel().getMainProcess().removeFlowElement(flow.getId());
          }
  		    for (BoundaryEvent event : activity.getBoundaryEvents()) {
  		      model.getBpmnModel().getMainProcess().removeFlowElement(event.getId());
          }
		    }
		  }
		  
		  if (context.getTargetContainer() instanceof Diagram == false) {
        Object containerBo = getFeatureProvider().getBusinessObjectForPictogramElement(context.getTargetContainer());
        if (containerBo instanceof SubProcess) {
          SubProcess subProcess = (SubProcess) containerBo;
          subProcess.addFlowElement(activity);
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            subProcess.addFlowElement(flow);
          }
          for (BoundaryEvent event : activity.getBoundaryEvents()) {
            subProcess.addFlowElement(event);
          }
        } else if (containerBo instanceof Lane) {
          Lane lane = (Lane) containerBo;
          lane.getFlowReferences().add(activity.getId());
          lane.getParentProcess().addFlowElement(activity);
          for (SequenceFlow flow : activity.getOutgoingFlows()) {
            lane.getParentProcess().addFlowElement(flow);
          }
          for (BoundaryEvent event : activity.getBoundaryEvents()) {
            lane.getParentProcess().addFlowElement(event);
          }
        }
      } else {
        if (model.getBpmnModel().getMainProcess() == null) {
          model.addMainProcess();
        }
        model.getBpmnModel().getMainProcess().addFlowElement(activity);
        for (SequenceFlow flow : activity.getOutgoingFlows()) {
          model.getBpmnModel().getMainProcess().addFlowElement(flow);
        }
        for (BoundaryEvent event : activity.getBoundaryEvents()) {
          model.getBpmnModel().getMainProcess().addFlowElement(event);
        }
      }
		}
	}
	
	protected void moveActivityChilds(Activity activity, ILocation shapeLocationAfterMove) {
		// get all boundary events of the activity
		final List<BoundaryEvent> boundaryEvents = activity.getBoundaryEvents();
		moveBoundaryEvents(boundaryEvents, shapeLocationAfterMove);
		
		// also move all boundary events and bendpoints in the sub process
		if (activity instanceof SubProcess) {
		  SubProcess subProcess = (SubProcess) activity;
			for (FlowElement subElement : subProcess.getFlowElements()) {
	      if (subElement instanceof Activity) {
	      	moveActivityChilds((Activity) subElement, shapeLocationAfterMove);
	      } else if (subElement instanceof SequenceFlow) {
	        moveSequenceFlowBendpoints((SequenceFlow) subElement, shapeLocationAfterMove);
	      }
      }
			
			for (Artifact artifact : subProcess.getArtifacts()) {
			  if (artifact instanceof Association) {
			    moveAssociationBendpoints((Association) artifact, shapeLocationAfterMove);
			  }
			}
		}
	}
	
	protected void moveBoundaryEvents(final List<BoundaryEvent> boundaryEvents, ILocation shapeLocationAfterMove) {
		final IGaService gaService = Graphiti.getGaService();
		for (final BoundaryEvent boundaryEvent : boundaryEvents) {
			
			// get all pictogram elements. Actually this should be only a single element, however
			// Graphiti allows multiple elements. The loop itself will not really harm in this case
			final PictogramElement picto = getFeatureProvider().getPictogramElementForBusinessObject(boundaryEvent);
			
			// get the current position of the boundary event 
			int x = picto.getGraphicsAlgorithm().getX();
			int y = picto.getGraphicsAlgorithm().getY();
			
			int deltaX = shapeLocationAfterMove.getX() - shapeLocationBeforeMove.getX();
			int deltaY = shapeLocationAfterMove.getY() - shapeLocationBeforeMove.getY();

			// move it the same delta in both directions, the activity itself has been moved
			gaService.setLocation(picto.getGraphicsAlgorithm(), x + deltaX, y + deltaY);
			Graphiti.getPeService().sendToFront((Shape) picto);
		}
	}
	
	protected void moveSequenceFlowBendpoints(SequenceFlow sequenceFlow, ILocation shapeLocationAfterMove) {
    FreeFormConnection freeFormConnection = (FreeFormConnection) getFeatureProvider().getPictogramElementForBusinessObject(sequenceFlow);
    moveBendpoints(freeFormConnection, shapeLocationAfterMove);
  }
	
	protected void moveAssociationBendpoints(Association association, ILocation shapeLocationAfterMove) {
    FreeFormConnection freeFormConnection = (FreeFormConnection) getFeatureProvider().getPictogramElementForBusinessObject(association);
    moveBendpoints(freeFormConnection, shapeLocationAfterMove);
  }
	
	protected void moveBendpoints(FreeFormConnection freeFormConnection, ILocation shapeLocationAfterMove) {
    if (freeFormConnection != null && freeFormConnection.getBendpoints() != null && freeFormConnection.getBendpoints().size() > 0) {
      
      int deltaX = shapeLocationAfterMove.getX() - shapeLocationBeforeMove.getX();
      int deltaY = shapeLocationAfterMove.getY() - shapeLocationBeforeMove.getY();
      
      for (Point point : freeFormConnection.getBendpoints()) {
        point.setX(point.getX() + deltaX);
        point.setY(point.getY() + deltaY);
      }
    }
  }
}