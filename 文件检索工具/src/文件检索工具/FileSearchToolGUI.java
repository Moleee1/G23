package 文件检索工具;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSearchToolGUI extends JFrame {
    private JTextField directoryTextField;
    private JTextField keywordTextField;
    private JTextArea resultTextArea;

    private List<String> selectedResults;
    private Map<String, List<String>> fileIndex;

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

        directoryPanel.add(directoryLabel);
        directoryPanel.add(directoryTextField);

        keywordPanel.add(keywordLabel);
        keywordPanel.add(keywordTextField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchButton);

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
                if (file.isFile() && file.getName().toLowerCase().endsWith(".txt")) {
                    searchFile(file, keyword);
                } else if (file.isDirectory()) {
                    traverseDirectory(file, keyword);
                }
            }
        }
    }

    private void searchFile(File file, String keyword) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            boolean keywordFound = false;

            while ((line = reader.readLine()) != null) {
                if (line.toLowerCase().contains(keyword.toLowerCase())) {
                    if (!keywordFound) {
                        resultTextArea.append("文件路径：" + getFilePathWithLineNumber(file) + "\n");
                        resultTextArea.append("-----------------------------------\n");
                        keywordFound = true;
                    }
                    resultTextArea.append("行号" + lineNumber + ": " + line + "\n\n");
                }
                lineNumber++;
            }

            if (keywordFound) {
                selectedResults.add(file.getAbsolutePath());
                List<String> lines = fileIndex.getOrDefault(file.getAbsolutePath(), new ArrayList<>());
                lines.add(resultTextArea.getText());
                fileIndex.put(file.getAbsolutePath(), lines);
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "读取文件时出现错误");
        }
    }

    private String getFilePathWithLineNumber(File file) {
        String filePath = file.getAbsolutePath();
        filePath = filePath.replace("\\", "\\\\");
        return filePath;
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
