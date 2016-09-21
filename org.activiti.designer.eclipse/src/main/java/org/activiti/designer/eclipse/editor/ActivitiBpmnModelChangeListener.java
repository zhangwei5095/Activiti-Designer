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
package org.activiti.designer.eclipse.editor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.NotificationFilter;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListener;
import org.eclipse.emf.transaction.RollbackException;
import org.eclipse.graphiti.dt.IDiagramTypeProvider;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.notification.INotificationService;
import org.eclipse.graphiti.platform.IDiagramBehavior;
import org.eclipse.graphiti.platform.IDiagramContainer;
import org.eclipse.swt.widgets.Display;


public class ActivitiBpmnModelChangeListener implements ResourceSetListener {

  private final IDiagramBehavior diagramBehavior;

  public ActivitiBpmnModelChangeListener(final IDiagramBehavior diagramBehavior) {
    super();

    this.diagramBehavior = diagramBehavior;
  }

  @Override
  public NotificationFilter getFilter() {
    return NotificationFilter.NOT_TOUCH;
  }

  @Override
  public boolean isAggregatePrecommitListener() {
    return false;
  }

  @Override
  public boolean isPostcommitOnly() {
    return false;
  }

  @Override
  public boolean isPrecommitOnly() {
    return false;
  }

  @Override
  public void resourceSetChanged(ResourceSetChangeEvent event) {
    final IDiagramTypeProvider provider = diagramBehavior.getDiagramContainer().getDiagramTypeProvider();
    final Diagram diagram = provider.getDiagram();

    if (diagram != null && diagram.getPictogramLinks().isEmpty()) {
      return;
    }

    final Set<EObject> changedBOs = new HashSet<EObject>();
    final List<Notification> notifications = event.getNotifications();

    for (final Notification notification : notifications) {
      final Object notifier = notification.getNotifier();

      if (notifier instanceof EObject) {
        final EObject eNotifier = (EObject) notifier;

        changedBOs.add(eNotifier);
      }
    }

    final INotificationService notificationService = provider.getNotificationService();
    final PictogramElement[] dirtyPEs = notificationService.calculateRelatedPictogramElements(changedBOs.toArray());

    if (dirtyPEs.length > 0) {
      // do an asynchronous update in the UI thread
      Display.getDefault().asyncExec(new Runnable() {

        @Override
        public void run() {
          IDiagramContainer diagramContainer = provider.getDiagramBehavior().getDiagramContainer();
          if (provider.isAutoUpdateAtRuntime() && diagramContainer.isDirty()) {
            notificationService.updatePictogramElements(dirtyPEs);
          } else {
            provider.getDiagramBehavior().refresh();
          }
        }

      });
    }
  }

  @Override
  public Command transactionAboutToCommit(ResourceSetChangeEvent event) throws RollbackException {
    return null;
  }

}
