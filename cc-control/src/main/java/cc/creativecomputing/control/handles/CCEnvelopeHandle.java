package cc.creativecomputing.control.handles;

import cc.creativecomputing.control.CCEnvelope;
import cc.creativecomputing.control.timeline.TrackData;
import cc.creativecomputing.core.CCProperty;
import cc.creativecomputing.core.util.CCReflectionUtil.CCMember;
import cc.creativecomputing.io.data.CCDataObject;

public class CCEnvelopeHandle extends CCPropertyHandle<CCEnvelope>{
	
	protected CCEnvelopeHandle(CCObjectPropertyHandle theParent, CCMember<CCProperty> theMember) {
		super(theParent, theMember);
	}
	
	@Override
	public void value(CCEnvelope theValue, boolean theOverWrite) {
		if(theValue == null)return;
		if(theOverWrite)_myPresetValue = theValue.clone();
		if(theValue == _myValue)return;
		_myValue.set(theValue);
		_myUpdateMember = true;
	}
	
	@Override
	public CCDataObject data() {
		CCDataObject myResult = super.data();
		CCEnvelope myEnvelope = value();
		TrackData myCurve = myEnvelope.curve();
		myResult.put("curve", myCurve.data());
		return myResult;
	}
	
	@Override
	public void data(CCDataObject theData) {
		CCEnvelope myEnvelope = new CCEnvelope();
		myEnvelope.curve().clear();
		myEnvelope.curve().data(theData.getObject("curve"));
		value(myEnvelope, true);
	}
	
	@Override
	public double normalizedValue() {
		return 0;
	}

	@Override
	public String valueString() {
		return null;
	}
}