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

import org.activiti.bpmn.model.Event;
import org.activiti.bpmn.model.TimerEventDefinition;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

public class PropertyIntermediateCatchTimerSection extends ActivitiPropertySection implements ITabbedPropertyConstants {

	private Text timeDurationText;
	private Text timeDateText;
	private Text timeCycleText;
	
	@Override
  public void createFormControls(TabbedPropertySheetPage aTabbedPropertySheetPage) {
    timeDurationText = createTextControl(false);
    createLabel("Time duration", timeDurationText);
    timeDateText = createTextControl(false);
    createLabel("Time date (ISO 8601)", timeDateText);
    timeCycleText = createTextControl(false);
    createLabel("Time cycle", timeCycleText);
  }

  @Override
  protected Object getModelValueForControl(Control control, Object businessObject) {
    Event event = (Event) businessObject;
    TimerEventDefinition timerDefinition = (TimerEventDefinition) event.getEventDefinitions().get(0);
    
    if (control == timeDurationText) {
      return timerDefinition.getTimeDuration();
      
    } else if (control == timeDateText) {
      return timerDefinition.getTimeDate();
      
    } else if (control == timeCycleText) {
      return timerDefinition.getTimeCycle();
    }
    return null;
  }

  @Override
  protected void storeValueInModel(Control control, Object businessObject) {
    Event event = (Event) businessObject;
    TimerEventDefinition timerDefinition = (TimerEventDefinition) event.getEventDefinitions().get(0);
    
    if (control == timeDurationText) {
      timerDefinition.setTimeDuration(timeDurationText.getText());
    
    } else if (control == timeDateText) {
      timerDefinition.setTimeDate(timeDateText.getText());
    
    } else if (control == timeCycleText) {
      timerDefinition.setTimeCycle(timeCycleText.getText());
    }
  }
}
