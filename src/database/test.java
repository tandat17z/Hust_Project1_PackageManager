package database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class test {

    public static void main(String[] args) {
        try {
            String targetFilePath = "D:\\Hust_project1_PackageManager\\GradleProjectRoot\\app\\build.gradle";
            String sourceFilePath = "D:\\Project_gradle\\app\\build.gradle";

            String sourceDependencies = readDependenciesFromFile(sourceFilePath);
            String targetContent = readFromFile(targetFilePath);

            // Tìm và thay thế phần dependencies trong file đích
            String replacedContent = targetContent.replaceAll("dependencies\\s*\\{[^}]*\\}", sourceDependencies);

            // Ghi nội dung mới vào file đích
            writeToFile(targetFilePath, replacedContent);

            System.out.println("Thay thế thành công.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String readFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private static String readDependenciesFromFile(String filePath) throws IOException {
        StringBuilder dependencies = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            boolean inDependenciesBlock = false;
            String line;
            while ((line = br.readLine()) != null) {
                if (line.matches("\\s*dependencies\\s*\\{.*")) {
                    inDependenciesBlock = true;
                }
                if (inDependenciesBlock) {
                    dependencies.append(line).append("\n");
                }
                if (line.matches("\\s*}\\s*")) {
                    inDependenciesBlock = false;
                }
            }
        }
        return dependencies.toString();
    }

    private static void writeToFile(String filePath, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(content);
        }
    }
}
