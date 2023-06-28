package 文件检索工具;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class FileSearchToolGUI extends JFrame {
    private JTextField directoryTextField;
    private JTextField keywordTextField;
    private JTextArea resultTextArea;

    private List<String> selectedResults;
    private Map<String, List<String>> fileIndex;
    private Connection connection;

    public FileSearchToolGUI() {
        setTitle("文件检索工具");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLayout(new BorderLayout());

        JPanel directoryPanel = new JPanel(new FlowLayout());
        JLabel directoryLabel = new JLabel("目录路径:");
        directoryTextField = new JTextField(20);

        JPanel keywordPanel = new JPanel(new FlowLayout());
        JLabel keywordLabel = new JLabel("关键字:");
        keywordTextField = new JTextField(20);

        JButton searchButton = new JButton("检索");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performSearch();
            }
        });

        JButton saveButton = new JButton("保存");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSelectedResults();
            }
        });

        directoryPanel.add(directoryLabel);
        directoryPanel.add(directoryTextField);

        keywordPanel.add(keywordLabel);
        keywordPanel.add(keywordTextField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchButton);
        buttonPanel.add(saveButton);

        JPanel resultPanel = new JPanel(new BorderLayout());
        JLabel resultLabel = new JLabel("检索结果:");
        resultTextArea = new JTextArea();
        resultTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultTextArea);

        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(3, 1));
        inputPanel.add(directoryPanel);
        inputPanel.add(keywordPanel);

        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
        add(resultPanel, BorderLayout.CENTER);

        selectedResults = new ArrayList<>();
        fileIndex = new HashMap<>();

        // 连接数据库
        String url = "jdbc:mysql://localhost:3306/mysql";
        String user = "root";
        String password = "";

        try {
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "连接数据库时出现错误");
           
        }
    }

private void performSearch() {
        String directoryPath = directoryTextField.getText();
        String keyword = keywordTextField.getText();

        resultTextArea.setText("");
        selectedResults.clear();

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            JOptionPane.showMessageDialog(this, "目录不存在或不是一个有效的文件夹路径");
            return;
        }

        traverseDirectory(directory, keyword);
    }

    private void traverseDirectory(File directory, String keyword) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".docx")) {
                    searchFile(file, keyword);
                } else if (file.isDirectory()) {
                    traverseDirectory(file, keyword);
                }
            }
        }
    }

 private void searchFile(File file, String keyword) {
        try (InputStream inputStream = new FileInputStream(file)) {
            XWPFDocument document = new XWPFDocument(inputStream);
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            boolean keywordFound = false;

            for (XWPFParagraph paragraph : paragraphs) {
                String text = paragraph.getText();
                if (text.toLowerCase().contains(keyword.toLowerCase())) {
                    if (!keywordFound) {
                        resultTextArea.append("文件路径：" + getFilePath(file) + "\n");
                        resultTextArea.append("-----------------------------------\n");
                        keywordFound = true;
                    }
                    resultTextArea.append(text + "\n\n");
                }
            }

            if (keywordFound) {
                selectedResults.add(getFilePath(file));
                List<String> content = new ArrayList<>();
                for (XWPFParagraph paragraph : paragraphs) {
                    content.add(paragraph.getText());
                }
                fileIndex.put(getFilePath(file), content);

                // 将结果存储到数据库
                try {
                    PreparedStatement statement = connection.prepareStatement("INSERT INTO files (path, content) VALUES (?, ?)");
                    statement.setString(1, getFilePath(file));
                    statement.setString(2, String.join("\n", content));
                    statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, "存储结果到数据库时出现错误");
                    // 可能需要在这里添加一些逻辑来处理存储失败的情况
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取文件时出现错误");
        }
    }

    private String getFilePath(File file) {
        return file.getAbsolutePath();
    }

    private void saveSelectedResults() {
        if (selectedResults.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有选定的结果需要保存");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("保存选定结果");
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(selectedFile)) {
                for (String filePath : selectedResults) {
                    writer.println("文件路径: " + filePath);
                    writer.println("-----------------------------------");
                    List<String> content = fileIndex.get(filePath);
                    for (String line : content) {
                        writer.println(line);
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(this, "保存成功");
            } catch (FileNotFoundException e) {
                JOptionPane.showMessageDialog(this, "保存文件时出现错误");
            }
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                FileSearchToolGUI gui = new FileSearchToolGUI();
                gui.setVisible(true);
            }
        });
    }
}
