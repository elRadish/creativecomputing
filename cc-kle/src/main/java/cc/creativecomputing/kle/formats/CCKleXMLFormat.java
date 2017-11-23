package cc.creativecomputing.kle.formats;

import java.nio.file.Path;

import cc.creativecomputing.io.xml.CCDataElement;
import cc.creativecomputing.io.xml.CCXMLIO;
import cc.creativecomputing.kle.CCKleMapping;
import cc.creativecomputing.kle.sequence.CCSequence;
import cc.creativecomputing.kle.sequence.CCSequenceRecorder.CCSequenceElementRecording;

/**
 * 
 * <pre>
 * {@code
 * <ac_kinetic_animation version="2">
 * 	<project type="silksplace_1">
 * 		<name>silksplace</name>
 * 		<baseunit>cm</baseunit>
 * 		<spacing>
 * 			<u>18.00000</u>
 * 			<v>18.00000</v>
 * 		</spacing>
 * 		<resolution>
 * 			<u>12</u>
 * 			<v>14</v>
 * 		</resolution>
 * 		<origin>
 * 			<x>0.00000</x>
 * 			<y>0.00000</y>
 * 			<z>0.00000</z>
 * 		</origin>
 * 		<animation>
 * 			<name>Kinetik Animation</name>
 * 			<framerate>5.00000</framerate>
 * 			<framecount>1951</framecount>
 * 			<grid>
 * 				<motion u="0" v="0">
 * 					<height>365.00000</height>
 * 				</motion>
      }
      </pre>
 */
public class CCKleXMLFormat implements CCKleFormat {

	public CCKleXMLFormat() {
	}

	@Override
	public void save(Path thePath, CCKleMapping<?> theMapping, CCSequence theSequence) {
		CCDataElement myKineticXML = new CCDataElement("ac_kinetic_animation");
		myKineticXML.addAttribute("version", 2);

		CCDataElement myProjectXML = myKineticXML.createChild("project");

		CCDataElement myBaseUnitXML = myProjectXML.createChild("baseunit");
		myBaseUnitXML.addContent("cm");

		CCDataElement myResolutionXML = myProjectXML.createChild("resolution");
		myResolutionXML.createChild("u", theSequence.columns());
		myResolutionXML.createChild("v", theSequence.rows());

		CCDataElement myAnimationXML = myProjectXML.createChild("animation");
		myAnimationXML.createChild("framecount", theSequence.length());

		CCDataElement myGridXML = myAnimationXML.createChild("grid");

		for (int c = 0; c < theSequence.columns(); c++) {
			for (int r = 0; r < theSequence.rows(); r++) {
				CCDataElement myMotionXML = myGridXML.createChild("motion");
				myMotionXML.addAttribute("u", c);
				myMotionXML.addAttribute("v", r);
				for (int f = 0; f < theSequence.length(); f++) {
					myMotionXML.createChild("height", theSequence.frame(f).data()[c][r][0]);
				}
			}
		}
		CCXMLIO.saveXMLElement(myKineticXML, thePath);
	}

	@Override
	public void savePosition(Path thePath, CCSequenceElementRecording theRecording, boolean[] theSave) {

	}

	@Override
	public CCSequence load(Path thePath, CCKleMapping<?> theMapping) {

		CCDataElement myKineticXML = CCXMLIO.createXMLElement(thePath);
		if (myKineticXML == null)
			return null;
		CCDataElement myProjectXML = myKineticXML.child("project");
		if (myProjectXML == null)
			return null;

		String myBaseUnit = myProjectXML.child("baseunit").content();
		if (!myBaseUnit.equals("cm")) {
			throw new RuntimeException("base unit needs to be cm");
		}

		CCDataElement myResolutionXML = myProjectXML.child("resolution");
		if (myResolutionXML == null) {
			throw new RuntimeException("resolution element is missing");
		}

		int myColumns = myResolutionXML.child("u").intContent();
		int myRows = myResolutionXML.child("v").intContent();

		if (myColumns != theMapping.columns() || myRows != theMapping.rows()) {
			throw new RuntimeException("grid resolution of the current data does not match project resolution");
		}

		CCDataElement myAnimationXML = myProjectXML.child("animation");
		if (myAnimationXML == null) {
			throw new RuntimeException("animation element is missing");
		}
		int myFrameCount = myAnimationXML.child("framecount").intContent();

		CCSequence result = new CCSequence(theMapping.columns(), theMapping.rows(), theMapping.depth(), myFrameCount);

		CCDataElement myGridXML = myAnimationXML.child("grid");
		for (CCDataElement myMotionXML : myGridXML) {
			int myColumn = myMotionXML.intAttribute("u");
			int myRow = myMotionXML.intAttribute("v");
			int i = 0;
			for (CCDataElement myHeightXML : myMotionXML) {
				result.frame(i).data()[myColumn][myRow][0] = myHeightXML.doubleContent();
				i++;
			}
		}

		return result;
	}

	@Override
	public String extension() {
		return "xml";
	}

}
