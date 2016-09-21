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
package org.activiti.designer.util.style;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;
import org.eclipse.graphiti.mm.StyleContainer;
import org.eclipse.graphiti.mm.algorithms.styles.AdaptedGradientColoredAreas;
import org.eclipse.graphiti.mm.algorithms.styles.Color;
import org.eclipse.graphiti.mm.algorithms.styles.GradientColoredArea;
import org.eclipse.graphiti.mm.algorithms.styles.GradientColoredAreas;
import org.eclipse.graphiti.mm.algorithms.styles.LocationType;
import org.eclipse.graphiti.mm.algorithms.styles.Style;
import org.eclipse.graphiti.mm.algorithms.styles.StylesFactory;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.services.IGaService;
import org.eclipse.graphiti.util.ColorConstant;
import org.eclipse.graphiti.util.ColorUtil;
import org.eclipse.graphiti.util.IColorConstant;
import org.eclipse.graphiti.util.IGradientType;
import org.eclipse.graphiti.util.IPredefinedRenderingStyle;

public class StyleUtil {

	private static final IColorConstant BPMN_CLASS_FOREGROUND = new ColorConstant(0, 0, 0);
	
	public static Style getStyleForTask(Diagram diagram) {
    final String styleId = "TASK"; //$NON-NLS-1$

    Style style = findStyle(diagram, styleId);
    if (style == null) { // style not found - create new style
      IGaService gaService = Graphiti.getGaService();
      style = gaService.createStyle(diagram, styleId);
      style.setForeground(gaService.manageColor(diagram, BPMN_CLASS_FOREGROUND));
      gaService.setRenderingStyle(style, getDefaultTaskColor(diagram));
      style.setLineWidth(20);
    }
    return style;
  }
	
	public static Style getStyleForPool(Diagram diagram) {
    final String styleId = "POOL"; //$NON-NLS-1$

    Style style = findStyle(diagram, styleId);
    if (style == null) { // style not found - create new style
      IGaService gaService = Graphiti.getGaService();
      style = gaService.createStyle(diagram, styleId);
      style.setForeground(gaService.manageColor(diagram, BPMN_CLASS_FOREGROUND));
      style.setBackground(gaService.manageColor(diagram, ColorConstant.WHITE));
      style.setLineWidth(20);
    }
    return style;
  }
	
	public static Style getStyleForEvent(Diagram diagram) {
    final String styleId = "EVENT"; //$NON-NLS-1$

    Style style = findStyle(diagram, styleId);
    if (style == null) { // style not found - create new style
      IGaService gaService = Graphiti.getGaService();
      style = gaService.createStyle(diagram, styleId);
      style.setForeground(gaService.manageColor(diagram, BPMN_CLASS_FOREGROUND));
      gaService.setRenderingStyle(style, getDefaultEventColor(diagram));
      style.setLineWidth(20);
    }
    return style;
  }

	public static Style getStyleForPolygon(Diagram diagram) {
		final String styleId = "BPMN-POLYGON-ARROW"; //$NON-NLS-1$

		Style style = findStyle(diagram, styleId);

		if (style == null) { // style not found - create new style
			IGaService gaService = Graphiti.getGaService();
			style = gaService.createStyle(diagram, styleId);
			style.setForeground(gaService.manageColor(diagram, IColorConstant.BLACK));
			style.setBackground(gaService.manageColor(diagram, IColorConstant.BLACK));
			style.setLineWidth(1);
		}
		return style;
	}
	
	public static Style getStyleForMessageFlow(Diagram diagram) {
    final String styleId = "BPMN-MESSAGE-FLOW-ARROW"; //$NON-NLS-1$

    Style style = findStyle(diagram, styleId);

    if (style == null) { // style not found - create new style
      IGaService gaService = Graphiti.getGaService();
      style = gaService.createStyle(diagram, styleId);
      style.setForeground(gaService.manageColor(diagram, IColorConstant.BLACK));
      style.setBackground(gaService.manageColor(diagram, IColorConstant.WHITE));
      style.setLineWidth(1);
    }
    return style;
  }
	
	// find the style with a given id in the style-container, can return null
	private static Style findStyle(StyleContainer styleContainer, String id) {
		// find and return style
		Collection<Style> styles = styleContainer.getStyles();
		if (styles != null) {
			for (Style style : styles) {
				if (id.equals(style.getId())) {
					return style;
				}
			}
		}
		return null;
	}
	
	private static AdaptedGradientColoredAreas getDefaultTaskColor(final Diagram diagram) {
	  final AdaptedGradientColoredAreas agca = StylesFactory.eINSTANCE.createAdaptedGradientColoredAreas();
    agca.setDefinedStyleId("bpmnTaskStyle");
    agca.setGradientType(IGradientType.VERTICAL);
    final GradientColoredAreas defaultGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    defaultGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> gcas = defaultGradientColoredAreas.getGradientColor();
    addGradientColoredArea(gcas, "FAFBFC", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "FFFFCC", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT, defaultGradientColoredAreas);
    
    final GradientColoredAreas primarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    primarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> selectedGcas = primarySelectedGradientColoredAreas.getGradientColor();
    addGradientColoredArea(selectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_PRIMARY_SELECTED,
            primarySelectedGradientColoredAreas);
    
    final GradientColoredAreas secondarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    secondarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> secondarySelectedGcas = secondarySelectedGradientColoredAreas.getGradientColor();
    addGradientColoredArea(secondarySelectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED,
            secondarySelectedGradientColoredAreas);
    return agca;
	}
	
	private static AdaptedGradientColoredAreas getDefaultEventColor(final Diagram diagram) {
    final AdaptedGradientColoredAreas agca = StylesFactory.eINSTANCE.createAdaptedGradientColoredAreas();
    agca.setDefinedStyleId("bpmnEventStyle");
    agca.setGradientType(IGradientType.VERTICAL);
    final GradientColoredAreas defaultGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    defaultGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> gcas = defaultGradientColoredAreas.getGradientColor();
    addGradientColoredArea(gcas, "FAFBFC", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "FAFBFC", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT, defaultGradientColoredAreas);
    
    final GradientColoredAreas primarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    primarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> selectedGcas = primarySelectedGradientColoredAreas.getGradientColor();
    addGradientColoredArea(selectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_PRIMARY_SELECTED,
            primarySelectedGradientColoredAreas);
    
    final GradientColoredAreas secondarySelectedGradientColoredAreas = StylesFactory.eINSTANCE.createGradientColoredAreas();
    secondarySelectedGradientColoredAreas.setStyleAdaption(IPredefinedRenderingStyle.STYLE_ADAPTATION_DEFAULT);
    final EList<GradientColoredArea> secondarySelectedGcas = secondarySelectedGradientColoredAreas.getGradientColor();
    addGradientColoredArea(secondarySelectedGcas, "E5E5C2", 0, LocationType.LOCATION_TYPE_ABSOLUTE_START, "E5E5C2", 0, //$NON-NLS-1$ //$NON-NLS-2$
        LocationType.LOCATION_TYPE_ABSOLUTE_END, diagram);
    
    agca.getAdaptedGradientColoredAreas().add(IPredefinedRenderingStyle.STYLE_ADAPTATION_SECONDARY_SELECTED,
            secondarySelectedGradientColoredAreas);
    return agca;
  }
	
	public static void addGradientColoredArea(EList<GradientColoredArea> gcas, String colorStart, int locationValueStart,
	        LocationType locationTypeStart, String colorEnd, int locationValueEnd, LocationType locationTypeEnd
	        , Diagram diagram) {
    final GradientColoredArea gca = StylesFactory.eINSTANCE.createGradientColoredArea();
    gcas.add(gca);
    gca.setStart(StylesFactory.eINSTANCE.createGradientColoredLocation());
    
    IGaService gaService = Graphiti.getGaService();
    
    final Color startColor = gaService.manageColor(diagram, ColorUtil.getRedFromHex(colorStart)
                          , ColorUtil.getGreenFromHex(colorStart)
                          , ColorUtil.getBlueFromHex(colorStart));
    gca.getStart().setColor(startColor);
    gca.getStart().setLocationType(locationTypeStart);
    gca.getStart().setLocationValue(locationValueStart);
    gca.setEnd(StylesFactory.eINSTANCE.createGradientColoredLocation());
    
    final Color endColor = gaService.manageColor(diagram, ColorUtil.getRedFromHex(colorEnd)
      , ColorUtil.getGreenFromHex(colorEnd)
      , ColorUtil.getBlueFromHex(colorEnd));
    
    gca.getEnd().setColor(endColor);
    gca.getEnd().setLocationType(locationTypeEnd);
    gca.getEnd().setLocationValue(locationValueEnd);
  }
}
