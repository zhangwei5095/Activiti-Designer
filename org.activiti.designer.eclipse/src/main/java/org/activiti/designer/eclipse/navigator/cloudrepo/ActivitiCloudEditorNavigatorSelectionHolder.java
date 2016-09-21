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
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.designer.eclipse.navigator.cloudrepo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;


/**
 * @author Joram Barrez
 */
public class ActivitiCloudEditorNavigatorSelectionHolder {
	
	private static volatile ActivitiCloudEditorNavigatorSelectionHolder INSTANCE = new ActivitiCloudEditorNavigatorSelectionHolder();
	
	private List<JsonNode> selectedObjects = Collections.synchronizedList(new ArrayList<JsonNode>());
	
	public static ActivitiCloudEditorNavigatorSelectionHolder getInstance() {
		return INSTANCE;
	}

	public List<JsonNode> getSelectedObjects() {
		return selectedObjects;
	}

	public void setSelectedObjects(List<JsonNode> selectedObjects) {
		this.selectedObjects.clear();
		this.selectedObjects.addAll(selectedObjects);
	}
	
}
