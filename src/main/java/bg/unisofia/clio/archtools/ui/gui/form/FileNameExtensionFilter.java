package bg.unisofia.clio.archtools.ui.gui.form;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class FileNameExtensionFilter extends FileFilter {
    private String extensions[];

    private String description;

    public FileNameExtensionFilter(String description, String extension) {
      this(description, new String[] { extension });
    }

    public FileNameExtensionFilter(String description, String extensions[]) {
      this.description = description;
      this.extensions = (String[]) extensions.clone();
    }

    public boolean accept(File file) {
      if (file.isDirectory()) {
        return true;
      }
      int count = extensions.length;
      String path = file.getAbsolutePath();
      for (int i = 0; i < count; i++) {
        String ext = extensions[i];
        if (path.endsWith(ext)
            && (path.charAt(path.length() - ext.length()) == '.')) {
          return true;
        }
      }
      return false;
    }

    public String getDescription() {
      return (description == null ? extensions[0] : description);
    }
  }