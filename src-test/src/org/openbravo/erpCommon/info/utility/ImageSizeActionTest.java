/*
 *************************************************************************
 * The contents of this file are subject to the Openbravo  Public  License
 * Version  1.1  (the  "License"),  being   the  Mozilla   Public  License
 * Version 1.1  with a permitted attribution clause; you may not  use this
 * file except in compliance with the License. You  may  obtain  a copy of
 * the License at http://www.openbravo.com/legal/license.html 
 * Software distributed under the License  is  distributed  on  an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific  language  governing  rights  and  limitations
 * under the License. 
 * The Original Code is Openbravo ERP. 
 * The Initial Developer of the Original Code is Openbravo SLU 
 * All portions are Copyright (C) 2021 Openbravo SLU
 * All Rights Reserved. 
 * Contributor(s):  ______________________________________.
 ************************************************************************
 */
package org.openbravo.erpCommon.info.utility;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openbravo.base.session.OBPropertiesProvider;
import org.openbravo.erpCommon.utility.ImageResizeResult;
import org.openbravo.erpCommon.utility.UnsupportedFileFormatException;
import org.openbravo.erpCommon.utility.Utility;

/**
 * Tests the different standard image size actions that can be applied with
 * {@link Utility#applyImageSizeAction} utility method
 */
@RunWith(Parameterized.class)
public class ImageSizeActionTest {
  private static final String BASE_PATH = OBPropertiesProvider.getInstance()
      .getOpenbravoProperties()
      .getProperty("source.path");

  @Parameters(name = "action: ''{0}''")
  public static Collection<Object[]> params() {
    return Arrays.asList(new Object[][] { //
        { "ALLOWED", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "ALLOWED_MINIMUM", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "ALLOWED_MAXIMUM", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "RECOMMENDED", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "RECOMMENDED_MINIMUM", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "RECOMMENDED_MAXIMUM", 100L, 100L, 333L, 250L, "OB.jpg" }, //
        { "RESIZE_NOASPECTRATIO", 333L, 250L, 100L, 100L, "OB_resized_noaspect.jpg" }, //
        { "RESIZE_ASPECTRATIO", 333L, 250L, 100L, 75L, "OB_resized_aspect.jpg" }, //
        { "RESIZE_ASPECTRATIONL", 333L, 250L, 100L, 75L, "OB_resized_aspect.jpg" } //
    });
  }

  private String imageSizeAction;
  private long oldWidth;
  private long oldHeight;
  private long newWidth;
  private long newHeight;
  private String resultImage;

  public ImageSizeActionTest(String imageSizeAction, long oldWidth, long oldHeight, long newWidth,
      long newHeight, String resultImage) {
    this.imageSizeAction = imageSizeAction;
    this.oldWidth = oldWidth;
    this.oldHeight = oldHeight;
    this.newWidth = newWidth;
    this.newHeight = newHeight;
    this.resultImage = resultImage;
  }

  @Test
  public void testApplyImageResizeAction() throws IOException, UnsupportedFileFormatException {
    byte[] imageData = Files.readAllBytes(Paths.get(BASE_PATH, "src-test/data", "OB.jpg"));
    ImageResizeResult result = Utility.applyImageSizeAction(imageSizeAction, imageData, 100, 100);

    assertThat("expected old width", result.getOldSize()[0], is(oldWidth));
    assertThat("expected old height", result.getOldSize()[1], is(oldHeight));
    assertThat("expected new width", result.getNewSize()[0], is(newWidth));
    assertThat("expected new height", result.getNewSize()[1], is(newHeight));
    assertThat("expected mime type", result.getMimeType(), is("image/jpeg"));
    assertThat("expected image data", result.getImageData(),
        is(Files.readAllBytes(Paths.get(BASE_PATH, "src-test/data", resultImage))));
  }
}
