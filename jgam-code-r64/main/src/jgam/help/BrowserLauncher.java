package jgam.help;

import java.io.IOException;
import java.net.*;

import javax.swing.JOptionPane;

/**
 * Find a way to launch a browser from java. This won't work in general but
 * might in Windows and on other systems.
 * 
 * @author Mattias Ulbrich
 * 
 */
public class BrowserLauncher {
        
    private static String command;
    
    static {
        command = System.getProperty("jgam.browsercmd"); //$NON-NLS-1$
        if (command == null) {
            String os = System.getProperty("os.name"); //$NON-NLS-1$
            if (os.startsWith("Windows")) { //$NON-NLS-1$
                command = "rundll32 url.dll,FileProtocolHandler"; //$NON-NLS-1$
            } else if (os.startsWith("Mac")) { //$NON-NLS-1$
                command = "open"; //$NON-NLS-1$
            } else if (os.startsWith("SunOS")) { //$NON-NLS-1$
                command = "/usr/dt/bin/sdtwebclient"; //$NON-NLS-1$
            } else {
                command = "mozilla"; //$NON-NLS-1$
            }
        }

    }

    public static void launch(URL url) {
        try {
            Process p = Runtime.getRuntime().exec(command + " " + url); //$NON-NLS-1$
        } catch(IOException ex) {
            JOptionPane.showMessageDialog(null, HelpFrame.msg.getString("BrowserLauncher.errorHelpFrame.msg")); //$NON-NLS-1$
        }
    }
    
    public static void showConfigWindow() {
        String res = JOptionPane.showInputDialog(null, HelpFrame.msg.getString("BrowserLauncher.entercommand"), command); //$NON-NLS-1$
        if(res != null)
            command = res;
    }
    
    public static void main(String[] args) throws MalformedURLException, IOException {
        showConfigWindow();
        launch(new URL("http://www.hurra.de")); //$NON-NLS-1$
        
    }

}
