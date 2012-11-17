package net.ehrenkret.cloudfreeze;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class Cloudfreeze {
  public static void main(String[] args) {
    AWSCredentials credentials = getCredentials();

    System.out.println("Access Key: " + credentials.getAWSAccessKeyId());
    System.out.println("Secret Key: " + credentials.getAWSSecretKey());

    JFileChooser chooser = new JFileChooser();
    int chooserResult = chooser.showDialog(null, "Upload File");
    if (chooserResult == JFileChooser.APPROVE_OPTION) {
      System.out.println("Upload file: " + chooser.getSelectedFile());
    }
    System.exit(0);
  }

  private static AWSCredentials getCredentials() {
    final JDialog dialog = new JDialog((Frame)null, "AWS Credentials", true);
    dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    JLabel accessKeyLabel = new JLabel("AWS Access Key");
    dialog.add(accessKeyLabel);
    JTextField accessKeyField = new JTextField(80);
    dialog.add(accessKeyField);
    JLabel secretKeyLabel = new JLabel("AWS Secret Key");
    dialog.add(secretKeyLabel);
    JTextField secretKeyField = new JTextField(80);
    dialog.add(secretKeyField);
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    dialog.add(okButton);
    dialog.setVisible(true);
    return new BasicAWSCredentials(accessKeyField.getText(),
                                   secretKeyField.getText());
  }
}
