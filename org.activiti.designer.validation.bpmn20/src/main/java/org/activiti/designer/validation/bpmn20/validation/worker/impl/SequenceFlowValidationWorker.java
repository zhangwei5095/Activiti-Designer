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
package org.activiti.designer.validation.bpmn20.validation.worker.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.designer.validation.bpmn20.validation.worker.ProcessValidationWorker;
import org.activiti.designer.validation.bpmn20.validation.worker.ProcessValidationWorkerMarker;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.graphiti.mm.pictograms.Diagram;

/**
 * Validates process according to {@link ValidationCode#VAL_005} and
 * {@link ValidationCode#VAL_006}.
 * 
 * @author Tiese Barrell
 * @since 5.6
 * @version 1
 */
public class SequenceFlowValidationWorker implements ProcessValidationWorker {

  private static final String NO_SOURCE_ACTIVITY_EXCEPTION_MESSAGE_PATTERN = "SequenceFlow '%s' has no source activity";
  private static final String NO_TARGET_ACTIVITY_EXCEPTION_MESSAGE_PATTERN = "SequenceFlow '%s' has no target activity";

  @Override
  public Collection<ProcessValidationWorkerMarker> validate(final Diagram diagram, final Map<String, List<Object>> processNodes) {

    final Collection<ProcessValidationWorkerMarker> result = new ArrayList<ProcessValidationWorkerMarker>();

    final List<Object> sequenceFlows = processNodes.get(SequenceFlow.class.getCanonicalName());

    if (sequenceFlows != null && !sequenceFlows.isEmpty()) {
      for (final Object object : sequenceFlows) {

        final SequenceFlow sequenceFlow = (SequenceFlow) object;
        if (StringUtils.isEmpty(sequenceFlow.getSourceRef())) {
          result.add(new ProcessValidationWorkerMarker(IMarker.SEVERITY_ERROR, String.format(NO_SOURCE_ACTIVITY_EXCEPTION_MESSAGE_PATTERN,
                  sequenceFlow.getName()), sequenceFlow.getId(), ValidationCode.VAL_005));
        }
        if (StringUtils.isEmpty(sequenceFlow.getTargetRef())) {
          result.add(new ProcessValidationWorkerMarker(IMarker.SEVERITY_ERROR, String.format(NO_TARGET_ACTIVITY_EXCEPTION_MESSAGE_PATTERN,
                  sequenceFlow.getName()), sequenceFlow.getId(), ValidationCode.VAL_006));
        }
      }
    }

    return result;
  }
}
