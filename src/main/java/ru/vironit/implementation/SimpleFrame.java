package ru.vironit.implementation;

import com.amazonaws.AmazonServiceException;
import org.hibernate.Session;
import org.joda.time.DateTime;
import ru.vironit.connection.HibernateSessionFactory;
import ru.vironit.model.MetadataEntity;
import ru.vironit.other.ToEnglishFileName;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.*;
import org.apache.commons.io.FileUtils;
import ru.vironit.service.AWSS3Service2;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SimpleFrame extends JFrame implements ActionListener {

    AWSS3Service2 awsService;

    File file;
    String path;
    String bucketName;
    JLabel label;
    JTextField textField;
    JTextField objectField;
    JTextArea area;
    JComboBox listBox;
    JComboBox objectBox;

    public SimpleFrame(AWSS3Service2 awsService) {
        this.awsService = awsService;

        setTitle("AWS Connection");

        JLabel createLabel = new JLabel("Create Bucket");
        createLabel.setBounds(25, 25, 200, 25);
        add(createLabel);
        textField = new JTextField();
        textField.setBounds(25, 50, 200, 25);
        add(textField);
        JButton createButton = new JButton("Create Bucket");
        createButton.setBounds(250, 50, 150, 25);
        createButton.addActionListener(this);
        add(createButton);

        JLabel listLabel = new JLabel("List Buckets");
        listLabel.setBounds(25, 100, 200, 25);
        add(listLabel);
        listBox = new JComboBox(listBuckets());
        listBox.setBounds(25, 125, 200, 25);
        add(listBox);
        JButton chooseBucket = new JButton("Choose Bucket");
        chooseBucket.setBounds(250, 125, 150, 25);
        chooseBucket.addActionListener(this);
        add(chooseBucket);
        JButton deleteBucket = new JButton("Delete Bucket");
        deleteBucket.setBounds(425, 125, 150, 25);
        deleteBucket.addActionListener(this);
        add(deleteBucket);

        JLabel directoryLabel = new JLabel("Object Directory");
        directoryLabel.setBounds(25, 175, 200, 25);
        add(directoryLabel);
        objectField = new JTextField();
        objectField.setBounds(25, 200, 200, 25);
        add(objectField);
        JButton uploadFile = new JButton("Upload File");
        uploadFile.setBounds(250, 200, 150, 25);
        uploadFile.addActionListener(this);
        add(uploadFile);
        JButton chooseFile = new JButton("Choose File");
        chooseFile.setBounds(425, 200, 150, 25);
        chooseFile.addActionListener(this);
        add(chooseFile);
        label = new JLabel("No file selected");
        label.setBounds(25,235,600,25);
        add(label);

        JLabel objectLabel = new JLabel("List Objects");
        objectLabel.setBounds(25, 275, 200, 25);
        add(objectLabel);
        objectBox = new JComboBox();
        objectBox.setBounds(25, 300, 200, 25);
        add(objectBox);
        JButton downloadObject = new JButton("Download Object");
        downloadObject.setBounds(250, 300, 150, 25);
        downloadObject.addActionListener(this);
        add(downloadObject);
        JButton deleteObject = new JButton("Delete Object");
        deleteObject.setBounds(425, 300, 150, 25);
        deleteObject.addActionListener(this);
        add(deleteObject);

        area = new JTextArea();
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBounds(25, 350, 585, 200);
        add(scroll);
        scroll.setEnabled(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(650, 600);
        setLayout(null);
        setLocationRelativeTo(null);
//        pack();
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            String command = e.getActionCommand();

            if (command.equals("Create Bucket")) {
                bucketName = textField.getText();
                area.append(createBucket(bucketName));
                listBox.setModel(new DefaultComboBoxModel(listBuckets()));
                textField.setText("");
                for (int i = 0; i < listBox.getItemCount(); i++) {
                    if (listBox.getItemAt(i).equals(bucketName)) {
                        listBox.setSelectedIndex(i);
                    }
                }
                area.append("Bucket " + bucketName + " selected\n");
            }

            if (command.equals("Delete Bucket")) {
                bucketName = Objects.requireNonNull(listBox.getSelectedItem()).toString();
                area.append(deleteBucket(bucketName));
                listBox.setModel(new DefaultComboBoxModel(listBuckets()));
            }

            if (command.equals("Choose Bucket")) {
                bucketName = Objects.requireNonNull(listBox.getSelectedItem()).toString();
                objectBox.setModel(new DefaultComboBoxModel(listObjects()));
                area.append("Bucket " + bucketName + " selected\n");
            }

            if (command.equals("Upload File")) {
                area.append(uploadObject());
                objectBox.setModel(new DefaultComboBoxModel(listObjects()));
            }

            if (command.equals("Choose File")) {
                JFileChooser  chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                chooser.setMultiSelectionEnabled(false);
                if(chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    file = chooser.getSelectedFile();
                    label.setText(file.getName());
                } else {
                    label.setText("No file selected");
                }
            }

            if (command.equals("Delete Object")) {
                area.append(deleteObject());
                objectBox.setModel(new DefaultComboBoxModel(listObjects()));
            }

            if (command.equals("Download Object")) {
                JFileChooser chooser = new JFileChooser(FileSystemView.getFileSystemView().getHomeDirectory());
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                    path = (chooser.getSelectedFile().getAbsolutePath());
                    area.append(downloadObject());
                }
            }
        } catch (Exception ex) {
            area.append(ex.toString() + "\n");
            ex.printStackTrace();
        }
    }

    private String createBucket(String bucketName) {
        awsService.createBucket(bucketName);
        return ("Bucket with name " + bucketName + " created\n");
    }

    private String[] listBuckets() {
        List<Bucket> buckets = awsService.listBuckets();
        return buckets.stream().map(Bucket::name).toArray(String[]::new);
    }

    private String deleteBucket(String bucketName) {
        try {
            awsService.deleteBucket(bucketName);
            return ("Bucket " + bucketName + " deleted\n");
        } catch (AmazonServiceException e) {
            return (e.getErrorMessage()+ "\n");
        }
    }

    private String uploadObject() throws IOException {
        BasicFileAttributes attributes = Files.readAttributes(Path.of(file.getPath()), BasicFileAttributes.class);

        Map<String, String> metadata = new HashMap<>();

        metadata.put("id", Long.toString(attributes.creationTime().to(TimeUnit.MILLISECONDS)));
        metadata.put("file-name", ToEnglishFileName.toEnglish(file.getName()));
        metadata.put("upload-time", DateTime.now().toString());
        metadata.put("size", Long.toString(file.length()));

        awsService.putObject(
                bucketName,
                objectField.getText(),
                Path.of(file.getPath()),
                metadata
        );
        return "Object with name " + objectField.getText() + " added\n";
    }

    private String[] listObjects() {
        List<S3Object> objectListing = awsService.listObjects(bucketName);
        return objectListing.stream().map(S3Object::key).toArray(String[]::new);
    }

    private String downloadObject() throws IOException {
        String directory = Objects.requireNonNull(objectBox.getSelectedItem()).toString();

        HeadObjectResponse headObjectResponse = awsService.getHeadObjectResponse(bucketName, directory);
        Map<String, String> metaMap = headObjectResponse.metadata();

        MetadataEntity metadata = new MetadataEntity(
                Long.parseLong(metaMap.get("id")),
                metaMap.get("file-name"),
                metaMap.get("upload-time"),
                metaMap.get("size")
        );
        Session session = HibernateSessionFactory.getSessionFactory().openSession();
        session.beginTransaction();
        session.saveOrUpdate(metadata);
        session.getTransaction().commit();
        session.close();

        ResponseInputStream<GetObjectResponse> inputStream = awsService.getObject(bucketName, directory);
        path = path + File.separator + metaMap.get("file-name");
        FileUtils.copyInputStreamToFile(inputStream, new File(path));
        return ("File " + directory + " downloaded to " + path + "\n");
    }

    private String deleteObject() {
        awsService.deleteObject(bucketName, Objects.requireNonNull(objectBox.getSelectedItem()).toString());
        return ("Object " + objectBox.getSelectedItem().toString() + " deleted\n");
    }
}
