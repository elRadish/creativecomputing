package cc.creativecomputing.kle.sequence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cc.creativecomputing.core.logging.CCLog;
import cc.creativecomputing.math.CCMath;
import cc.creativecomputing.math.CCMatrix2;
import cc.creativecomputing.math.CCVector2;
import cc.creativecomputing.math.interpolate.CCInterpolators;

public class CCSequence extends ArrayList<CCMatrix2>{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4154710977769868741L;

	public static class CCSequenceSubIterator implements Iterator<CCMatrix2>{
		
		private Iterator<CCMatrix2> _myIterator;
		private int _myStartColumn;
		private int _myEndColumn;
		private int _myStartRow;
		private int _myEndRow;
		
		private CCSequenceSubIterator(List<CCMatrix2> theFrames, int theStartColumn, int theEndColumn, int theStartRow, int theEndRow){
			_myIterator = theFrames.iterator();
			_myStartColumn = theStartColumn;
			_myEndColumn = theEndColumn;
			_myStartRow = theStartRow;
			_myEndRow = theEndRow;
		}

		@Override
		public boolean hasNext() {
			return _myIterator.hasNext();
		}

		@Override
		public CCMatrix2 next() {
			return _myIterator.next().subMatrix(_myStartColumn, _myEndColumn, _myStartRow, _myEndRow);
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private int _myColumns;
	private int _myRows;
	private int _myDepth;
	
	public CCSequence(int theColumns, int theRows, int theDepth){
		_myColumns = theColumns;
		_myRows = theRows;
		_myDepth = theDepth;
	}
	
	public CCSequence(int theColumns, int theRows, int theDepth, int frames){
		this(theColumns, theRows, theDepth);
		addFrames(frames);
	}
	
	public CCSequence clone(){
		CCSequence myResult = new CCSequence(_myColumns, _myRows,_myDepth);
		for(CCMatrix2 myFrame:this){
			myResult.add(myFrame.clone());
		}
		return myResult;
	}
	
	public int columns(){
		return _myColumns;
	}
	
	public int rows(){
		return _myRows;
	}
	
	public int depth(){
		return _myDepth;
	}
	
	public void addFrames(int theFrames){
		for(int i = 0; i < theFrames;i++){
			addEmptyFrame();
		}
	}
	
	public void addEmptyFrame(){
		add(new CCMatrix2(_myColumns, _myRows, _myDepth));
	}
	
	public CCMatrix2 frame(int theIndex){
		return get(theIndex);
	}
	
	public CCMatrix2 frame(double theFrame){
		double myBlend = theFrame - (int)theFrame;
		CCMatrix2 myLower = frame((int)theFrame);
		CCMatrix2 myUpper = frame(CCMath.min((int)theFrame + 1, size() - 1));
		
		CCMatrix2 myResult = new CCMatrix2(_myColumns, _myRows, _myDepth);

		for (int c = 0; c < _myColumns; c++) {
			for (int r = 0; r < _myRows; r++) {
				for (int d = 0; d < _myDepth; d++) {
					myResult.data()[c][r][d] = CCMath.blend(myLower.data()[c][r][d], myUpper.data()[c][r][d], myBlend);
				}
			}
		}

		return myResult;
	}
	
	public double value(double theFrame, int theColumn, int theRow, int theDepth){
		double myBlend = theFrame - (int)theFrame;
		CCMatrix2 myLower = frame((int)theFrame);
		CCMatrix2 myUpper = frame(CCMath.min((int)theFrame + 1, size() - 1));
		return CCMath.blend(myLower.data()[theColumn][theRow][theDepth], myUpper.data()[theColumn][theRow][theDepth], myBlend);
	}
	
	public double value(CCInterpolators theInterpolator, double theFrame, int theColumn, int theRow, int theDepth){
		double myBlend = theFrame - (int)theFrame;
		CCMatrix2 myF0 = frame(CCMath.clamp((int)theFrame - 1, 0, size() - 1));
		CCMatrix2 myF1 = frame(CCMath.clamp((int)theFrame + 0, 0, size() - 1));
		CCMatrix2 myF2 = frame(CCMath.clamp((int)theFrame + 1, 0, size() - 1));
		CCMatrix2 myF3 = frame(CCMath.clamp((int)theFrame + 2, 0, size() - 1));
		
		return theInterpolator.blend(
			myF0.data()[theColumn][theRow][theDepth], 
			myF1.data()[theColumn][theRow][theDepth], 
			myF2.data()[theColumn][theRow][theDepth], 
			myF3.data()[theColumn][theRow][theDepth], 
			myBlend
		);
		
	}
	
	public CCVector2 minMax(CCVector2 theStore){
		if(theStore == null)theStore = new CCVector2(Double.MAX_VALUE,-Double.MAX_VALUE);
		for(CCMatrix2 myFrame:this){
			myFrame.minMax(theStore);
		}
		return theStore;
	}
	
	public CCVector2 minMax(){
		return minMax(null);
	}
	
	public void normalize(CCVector2 theMinMax){
		if(theMinMax == null)theMinMax = minMax();
		CCLog.info(theMinMax);
		for(CCMatrix2 myFrame:this){
			myFrame.normalize(theMinMax);
		}
	}
	
	public void normalize(){
		normalize(null);
	}
	
	public int length(){
		return size();
	}
	
	public CCSequenceSubIterator iterator(int col, int row){
	    return new CCSequenceSubIterator(this, col, col+1, row, row+1);
	}
	
	public CCSequenceSubIterator iterator(int startCol, int endCol, int startRow, int endRow){
	    return new CCSequenceSubIterator(this, startCol, endCol, startRow, endRow);
	}
	
	@Override
	public boolean equals(Object theO) {
		if(!(theO instanceof CCSequence))return false;
		CCSequence mySequence = (CCSequence)theO;
		
		if(mySequence.size() != size())return false;
		
		for(int i = 0; i < size();i++){
			if(!get(i).equals(mySequence.get(i)))return false;
		}
		return true;
	}
}
