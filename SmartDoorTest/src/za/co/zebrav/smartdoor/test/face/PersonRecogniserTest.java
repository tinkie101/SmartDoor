package za.co.zebrav.smartdoor.test.face;

import java.util.LinkedList;

import junit.framework.TestCase;
import za.co.zebrav.smartdoor.facerecognition.PersonRecognizer;

public class PersonRecogniserTest extends TestCase
{
	@Override
	protected void setUp() throws Exception
	{
		LinkedList<Integer> ids = new LinkedList<Integer>();
		ids.add(1);
		ids.add(2);
		ids.add(3);
		ids.add(4);
		ids.add(5);
		//PersonRecognizer pr = new PersonRecognizer(5, 2, 160, ids, dataDir);
	}
}
