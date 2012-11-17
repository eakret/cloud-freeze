package net.ehrenkret.cloudfreeze;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.glacier.AmazonGlacierClient;
import com.amazonaws.services.glacier.TreeHashGenerator;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.glacier.model.CompleteMultipartUploadResult;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.glacier.model.InitiateMultipartUploadResult;
import com.amazonaws.services.glacier.model.UploadMultipartPartRequest;
import com.amazonaws.services.glacier.model.UploadMultipartPartResult;
import com.amazonaws.util.BinaryUtils;

public class Cloudfreeze {
  private static final String VAULT_NAME = "personal-backups";
  private static final int PART_SIZE = 128 * 1024 * 1024;

  public static void main(String[] args) {
    JFrame root = new JFrame("Cloud Freeze");
    root.setVisible(true);

    AWSCredentials credentials = getCredentials(root);

    JFileChooser chooser = new JFileChooser();
    int chooserResult = chooser.showDialog(root, "Upload File");
    if (chooserResult == JFileChooser.APPROVE_OPTION) {
      try {
        uploadFile(root, credentials, chooser.getSelectedFile());
      } catch (Exception e) {
        System.err.println("Exception: " + e);
        System.exit(1);
      }
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

  private static void uploadFile(JFrame root, AWSCredentials credentials, File file) throws IOException {
    AmazonGlacierClient client = new AmazonGlacierClient(credentials);

    String fileName = file.getName();
    String[] fileParts = fileName.split("\\.(?=[^\\.]+$)");
    String archiveName = fileParts[0];

    InitiateMultipartUploadRequest request = new InitiateMultipartUploadRequest(VAULT_NAME, archiveName, String.valueOf(PART_SIZE));
    InitiateMultipartUploadResult result = client.initiateMultipartUpload(request);

    System.out.println("UPLOAD ID: " + result.getUploadId());

    long currentPosition = 0;
    final long fileLength = file.length();
    int currentPart = 0;
    int numParts = (int)(fileLength / PART_SIZE);
    if (fileLength % PART_SIZE != 0) {
      numParts += 1;
    }
    byte[] buffer = new byte[PART_SIZE];
    FileInputStream input = new FileInputStream(file);
    List<byte[]> checksums = new LinkedList<byte[]>();
    while (currentPosition < fileLength) {
      int read = input.read(buffer);
      if (read == -1) {
        break;
      }

      String contentRange = String.format("bytes %s-%s/*", currentPosition, currentPosition + read - 1);
      String checksum = TreeHashGenerator.calculateTreeHash(new ByteArrayInputStream(buffer, 0, read));
      checksums.add(BinaryUtils.fromHex(checksum));

      UploadMultipartPartRequest partRequest = new UploadMultipartPartRequest(VAULT_NAME, result.getUploadId(), checksum, contentRange, new ByteArrayInputStream(buffer, 0, read));
      UploadMultipartPartResult partResult = client.uploadMultipartPart(partRequest);

      System.out.println(String.format("PART %s/%s COMPLETED", currentPosition + 1, numParts));

      currentPosition += read;
      currentPart += 1;
    }
    String checksum = TreeHashGenerator.calculateTreeHash(checksums);

    CompleteMultipartUploadRequest completeRequest = new CompleteMultipartUploadRequest(VAULT_NAME, result.getUploadId(), String.valueOf(fileLength), checksum);
    CompleteMultipartUploadResult completeResult = client.completeMultipartUpload(completeRequest);
    System.out.println("COMPLETED UPLOAD: " + completeResult.getLocation());
  }
}
