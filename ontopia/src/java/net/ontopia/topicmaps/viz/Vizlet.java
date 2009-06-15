
// $Id: Vizlet.java,v 1.28 2007/09/12 09:25:54 eirik.opland Exp $

package net.ontopia.topicmaps.viz;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JApplet;
import net.ontopia.Ontopia;
import net.ontopia.utils.CmdlineUtils;
import net.ontopia.utils.OntopiaRuntimeException;
import net.ontopia.utils.PropertyUtils;
import net.ontopia.utils.StreamUtils;

/**
 * PUBLIC: Visualization applet. To learn how to use this applet, consult the
 * The Vizigator User's Guide.
 */
public class Vizlet extends JApplet {
  private ParsedMenuFile enabledMenuItems;
  private boolean parsedMenuItems;

  public String getAppletInfo() {
    return "Ontopia Vizlet";
  }

  public void init() {
    // FIXME: This logging should only go into the instrumented version of
    // Vizlet to find out what slows it down.
    VizDebugUtils.resetTimer();
    VizDebugUtils.instrumentedDebug("Vizlet.init() starting. Time: "
        + VizDebugUtils.getTimeDelta());
    
    // set up logging
    try {
      CmdlineUtils.initializeLogging();
      CmdlineUtils.setLoggingPriority("ERROR");
    } catch (Exception e) {
      e.printStackTrace();
    }
    outputVersionInfo();
    // get panel off the ground
    try {
      String tmrapParameter = getParameter("tmrap");
      if (tmrapParameter == null) {
        throw new VizigatorReportException("The required \"tmrap\" parameter " +
            "has not been set.");
      }
      // set the ui language
      Messages.setLanguage(getParameter("lang"));
      // create vizigator panel
      VizPanel vpanel = new VizPanel(this);
      getContentPane().add(vpanel);
      vpanel.configureDynamicMenus(new DynamicMenuListener(vpanel));
    } catch (VizigatorReportException e) {
      ErrorDialog.showMessage(this, e);
      throw e;
    } catch (Exception e) {
      ErrorDialog.showError(this, e);
      throw new OntopiaRuntimeException(e);
    }
  }

  public boolean getDefaultControlsVisible() {
    return PropertyUtils.isTrue(getParameter("controlsVisible"), true);
  }

  public int getDefaultLocality() {
    int locality = PropertyUtils.getInt(getParameter("locality"), 1);
    VizDebugUtils.debug("getDefaultLocality - locality: " + locality);
    return locality;
  }

  protected class DynamicMenuListener implements ActionListener {
    protected VizPanel vpanel;
    
    protected DynamicMenuListener(VizPanel vpanel) {
      this.vpanel = vpanel;
    }
    
    public void actionPerformed(ActionEvent actionEvent) {
      vpanel.configureDynamicMenus(this);
    }
  }

  // --- Helpers

  /**
   * INTERNAL: Just output version info to sysout.
   */
  private void outputVersionInfo() {
    System.out.println(Ontopia.getInfo());
  }

  /**
   * INTERNAL: Resolves the URI relative to the applet's codebase URI.
   */
  protected String resolve(String base) throws MalformedURLException {
    return new URL(getCodeBase(), base).toExternalForm();
  }

  public String getResolvedParameter(String param) {
    try {
      String paramValue = getParameter(param);
      if (paramValue == null)
        return null;
      return resolve(getParameter(param));
    } catch (MalformedURLException e) {
      throw new OntopiaRuntimeException(e);
    }
  }

  public int getMaxLocality() {
    int maxLocality = PropertyUtils.getInt(getParameter("max-locality"), 5);
    VizDebugUtils.debug("getMaxLocality - getParameter(\"max-locality\"): " + 
        getParameter("max-locality"));
    VizDebugUtils.debug("getMaxLocality - locality: " + maxLocality);
    return maxLocality;
  }

  /**
   * Process the menu file and get the enabled item ids from it.
   */
  public ParsedMenuFile getEnabledItemIds() {
    if (parsedMenuItems)
      return enabledMenuItems;
      
    String fileString = getParameter("menufile");
    if (fileString == null)
      return new ParsedMenuFile(null);

    URL codeBase = getCodeBase();
    String urlString = codeBase.toExternalForm() + fileString;
    MenuFileParser menuFileParser = new MenuFileParser(urlString);
    enabledMenuItems = menuFileParser.parse();
    parsedMenuItems = true;
    return enabledMenuItems;
  }
}
