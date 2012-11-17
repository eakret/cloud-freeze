package net.ehrenkret.cloudfreeze;

import java.io.File;

import javax.swing.JFileChooser;

public class Cloudfreeze {
  public static void main(String[] args) {
    JFileChooser chooser = new JFileChooser();
    int chooserResult = chooser.showDialog(null, "Upload File");
    if (chooserResult == JFileChooser.APPROVE_OPTION) {
      System.out.println("Upload file: " + chooser.getSelectedFile());
    }
  }
}
