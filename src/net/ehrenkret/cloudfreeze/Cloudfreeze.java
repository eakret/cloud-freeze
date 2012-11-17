package net.ehrenkret.cloudfreeze;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;

public class Cloudfreeze {
  public static void main(String[] args) {
    JFrame root = new JFrame("Cloud Freeze");
    root.setVisible(true);

    AWSCredentials credentials = getCredentials(root);

    System.out.println("Access Key: " + credentials.getAWSAccessKeyId());
    System.out.println("Secret Key: " + credentials.getAWSSecretKey());

    JFileChooser chooser = new JFileChooser();
    int chooserResult = chooser.showDialog(root, "Upload File");
    if (chooserResult == JFileChooser.APPROVE_OPTION) {
      System.out.println("Upload file: " + chooser.getSelectedFile());
    }
    System.exit(0);
  }

  private static AWSCredentials getCredentials(JFrame root) {
    final JDialog dialog = new JDialog(root, "AWS Credentials", true);
    dialog.setMinimumSize(new Dimension(320, 120));
    Box box = Box.createVerticalBox();
    JLabel accessKeyLabel = new JLabel("AWS Access Key");
    box.add(accessKeyLabel);
    JTextField accessKeyField = new JTextField();
    box.add(accessKeyField);
    JLabel secretKeyLabel = new JLabel("AWS Secret Key");
    box.add(secretKeyLabel);
    JTextField secretKeyField = new JTextField();
    box.add(secretKeyField);
    JButton okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dialog.setVisible(false);
      }
    });
    box.add(okButton);
    dialog.add(box);
    dialog.setVisible(true);
    return new BasicAWSCredentials(accessKeyField.getText(),
                                   secretKeyField.getText());
  }
}
