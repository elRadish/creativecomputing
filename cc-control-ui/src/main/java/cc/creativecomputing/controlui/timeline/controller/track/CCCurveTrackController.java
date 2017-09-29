/*  
 * Copyright (c) 2011 Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.controlui.timeline.controller.track;

import java.awt.geom.Point2D;

import cc.creativecomputing.control.timeline.Track;
import cc.creativecomputing.control.timeline.point.ControlPoint;
import cc.creativecomputing.controlui.timeline.controller.TrackContext;
import cc.creativecomputing.controlui.timeline.controller.tools.CCCreateTool;
import cc.creativecomputing.controlui.timeline.controller.tools.CCCurveTool;
import cc.creativecomputing.controlui.timeline.controller.tools.CCTimelineTools;
import cc.creativecomputing.core.logging.CCLog;

/**
 * @author christianriekoff
 *
 */
public abstract class CCCurveTrackController extends CCTrackController{
	
	protected CCCreateTool _myCreateTool;
	protected CCCurveTool _myCurveTool;
	
	protected CCTimelineTools _myActiveToolEnum;
	
	/**
	 * @param theTimelineController
	 * @param theTrack
	 * @param theParent
	 */
	public CCCurveTrackController(
		TrackContext theTrackContext, 
		Track theTrack, 
		CCGroupTrackController theParent
	) {
		super(theTrackContext, theTrack, theParent);
		
		
		_myCreateTool = new CCCreateTool(this);
		_myCurveTool = new CCCurveTool(this);
		
		_myCreateTool.setTool(CCTimelineTools.LINEAR_POINT);
		_myActiveToolEnum = CCTimelineTools.LINEAR_POINT;
		_myActiveTool = _myCreateTool;
		if(theTrack.property() == null)return;
		
		theTrack.property().events().add(theValue -> {
			if(_mySelectedPoints == null || _mySelectedPoints.size() == 0)return;
			if(_myCreateTool.isInDrag())return;
			if(_myTimelineController != null && _myTimelineController.transportController().isPlaying())return;
			if(_myChangedValue){
				_myChangedValue = false;
				return;
			}
			for(ControlPoint myPoint:_mySelectedPoints){
				applyValue(myPoint, null);
			}
			_myTrackView.render();
		});
	}
	
	@Override
	public void setTool(CCTimelineTools theTool) {
		_myActiveToolEnum = theTool;
		_myTrackView.render();
		switch(theTool){
		case BEZIER_POINT:
		case LINEAR_POINT:
		case STEP_POINT:
			CCLog.info(this+":"+_myCreateTool);
			CCLog.info(_myCreateTool + " : " + theTool);
			if(_myCreateTool == null)return;
			_myCreateTool.setTool(theTool);
			_myActiveTool = _myCreateTool;
			break;
		case CURVE:
			clearSelection();
			_myActiveTool = _myCurveTool;
			break;
		default:
			break;
		}
	}
	
	@Override
	public CCTimelineTools activeTool() {
		return _myActiveToolEnum;
	}
	
	@Override
	public ControlPoint draggedPoint() {
		return _myCreateTool.draggedPoint();
	}
	
	public Point2D selectionStart(){
		return _myCreateTool.selectionStart();
	}
	
	public Point2D selectionEnd(){
		return _myCreateTool.selectionEnd();
	}
	
	@Override
	public void writeValue(double theTime) {
		_myCreateTool.createPoint(new ControlPoint(theTime, _myProperty.normalizedValue()));
	}
	
	private boolean _myApplyValue = true;
	
	public void setApplyValue(boolean theApplyValue) {
		_myApplyValue = theApplyValue;
	}
	
	public void applyValue(ControlPoint thePoint, Object theValue) {
		thePoint.value(_myProperty.normalizedValue());
	}
}
