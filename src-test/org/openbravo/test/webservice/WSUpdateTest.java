/*
 * 
 * Copyright (C) 2001-2008 Openbravo S.L. Licensed under the Apache Software
 * License version 2.0 You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.openbravo.test.webservice;

import java.io.FileNotFoundException;

/**
 * Test webservice for reading, updating and posting. The test cases here
 * require a running Openbravo at http://localhost:8080/openbravo
 * 
 * @author mtaal
 */

public class WSUpdateTest extends BaseWSTest {

    public void testReadUpdateCity() throws Exception {
	final String city = doTestGetRequest("/ws/dal/City/100", null, 200);
	System.err.println(System.currentTimeMillis());
	String newCity;
	if (city.indexOf("<locode>") != -1) { // test already run
	    final int index1 = city.indexOf("<locode>");
	    final int index2 = city.indexOf("</locode>");
	    newCity = city.substring(0, index1) + "<locode>"
		    + ("" + System.currentTimeMillis()).substring(5)
		    + city.substring(index2);
	} else {
	    newCity = city.replaceAll("<locode/>", "<locode>"
		    + ("" + System.currentTimeMillis()).substring(5)
		    + "</locode>");
	}
	final String content = doContentRequest("/ws/dal/City/100", newCity,
		200, "<updated>", "POST");
	assertTrue(content.indexOf("City id=\"100") != -1);
    }

    public void testReadAddDeleteCity() throws Exception {
	final String city = doTestGetRequest("/ws/dal/City/100", null, 200);
	String newCity = city.replaceAll("</name>",
		(System.currentTimeMillis() + "").substring(6) + "</name>");
	final String newName = getTagValue(newCity, "name");

	newCity = newCity.replaceAll("City id=\"", "City id=\"test");
	// and replace the first <id>100</id> with <id>test100</id>
	final int index = newCity.indexOf("<id>");
	newCity = newCity.substring(0, index) + "<id>test"
		+ newCity.substring(index + "<id>".length());
	final String content = doContentRequest("/ws/dal/City", newCity, 200,
		"<inserted>", "POST");
	// System.err.println(content);
	// get the id and check if it is there
	final int index1 = content.indexOf("City id=\"")
		+ "City id=\"".length();
	final int index2 = content.indexOf("\"", index1);
	final String id = content.substring(index1, index2);

	// check if it is there
	doTestGetRequest("/ws/dal/City/" + id, "<City", 200);

	// count the cities
	doTestGetRequest("/ws/dal/City/count", "<result>2</result>", 200);

	// test a simple whereclause
	// first count
	doTestGetRequest("/ws/dal/City/count?where=name='" + newName + "'",
		"<result>1</result>", 200);

	// and then get a result, should only be one City
	final String queriedCities = doTestGetRequest(
		"/ws/dal/City?where=name='" + newName + "'", null, 200);
	final int queryIndex = queriedCities.indexOf("<City");
	assertTrue(queryIndex != -1);
	assertTrue(queriedCities.indexOf("<City", queryIndex + 5) == -1);

	// get all cities
	final String allCities = doTestGetRequest("/ws/dal/City", null, 200);
	// there should be two
	final int indexCity1 = allCities.indexOf("<City") + "<City".length();
	final int indexCity2 = allCities.indexOf("<City", indexCity1);
	assertTrue(indexCity1 != -1);
	assertTrue(indexCity2 != -1);

	// delete it
	doDirectDeleteRequest("/ws/dal/City/" + id, 200);

	// it should not be there!
	try {
	    doTestGetRequest("/ws/dal/City/" + id, "<error>", 404);
	    fail("City " + id + " was not deleted");
	} catch (Exception e) {
	    e.printStackTrace(System.err);
	    assertTrue(e.getCause() instanceof FileNotFoundException);
	}
    }

    public void testReadAddCityWrongMethodError() throws Exception {
	final String city = doTestGetRequest("/ws/dal/City/100", null, 200);
	String newCity = city.replaceAll("</name>",
		(System.currentTimeMillis() + "").substring(6) + "</name>");
	newCity = newCity.replaceAll("id=\"", "id=\"test");
	final int index = newCity.indexOf("<id>");
	newCity = newCity.substring(0, index) + "<id>test"
		+ newCity.substring(index + "<id>".length());
	try {
	    doContentRequest("/ws/dal/City", newCity, 500, "<inserted>", "PUT");
	    fail();
	} catch (Exception e) {
	    assertTrue(e.getMessage().indexOf("500") != -1);
	}
    }
}