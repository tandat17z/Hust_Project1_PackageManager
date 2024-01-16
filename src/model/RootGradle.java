package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class RootGradle extends Root{
	final String projectName = "GradleProjectRoot";
	
	public RootGradle() {
		fileType = "build.gradle";
	}
	
	@Override
	public void create() { 
    	
    }
	
	
	
	@Override
	public void saveDependencyTreeToTxt(String projectPath) {
		// Thay thế dependency của project vào project Root
		replaceDependencyInPom(projectPath);
		
		String[] command = {"gradle.bat", "dependencies", ">", "DependencyTree.txt"};
		
		ProcessBuilder processBuilder = new ProcessBuilder(command)
				.directory(new File(rootDir + projectName + "\\app"));
		
		try {
			Process process = processBuilder.start();
			// Chờ lệnh kết thúc
			int exitCode = process.waitFor();
			
			// Chuyển file tree tới nơi lưu trữ
			String sourceFilePath = rootDir + projectName + "\\app\\DependencyTree.txt";
			String destinationFolderPath = projectPath;
	        Path sourcePath = Paths.get(sourceFilePath);
	        Path destinationPath = Paths.get(destinationFolderPath, sourcePath.getFileName().toString());
			Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("GradleProjectRoot: Lưu DependencyText thành công, exitCode: " + exitCode);
//        saveDependency(projectPath); 
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void replaceDependencyInPom(String projectPath) {
		String filePath = projectPath + "build.gradle";
		String configFileRoot = rootDir + projectName + "\\app\\build.gradle";
		try {
			// Tìm và thay thế phần dependencies trong file đích
    		String sourceDependencies = readDependenciesFromFile(filePath);
            String targetContent = readFromFile(configFileRoot);

            String replacedContent = targetContent.replaceAll("dependencies\\s*\\{[^}]*\\}", sourceDependencies);

            // Ghi nội dung mới vào file đích
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(configFileRoot))) {
                bw.write(replacedContent);
            }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        System.out.println("Thay thế thành công.");
	}
	
	private String readDependenciesFromFile(String filePath) throws IOException {
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
	
	private String readFromFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
	
	
//	public static void main(String[] args) {
//		String path = "D:\\Hust_project1_PackageManager\\Project\\gradle\\project2\\1.0.1\\";
//		(new RootGradle()).getDependency(path);
//	}
	@Override
	public Map<Library, Library> getDependency(String path){
		File treeFile = new File(path + "DependencyTree.txt");
		
		Map<Library, Library> tree = new TreeMap<>();
		List<Library> pa = new ArrayList<Library>(); // Lưu cha của các mức
		
		pa.add(null);
		int deep = 1;
		Scanner myReader;
		try {
			myReader = new Scanner(treeFile);
			boolean check = false; // Bắt đầu tìm đến đoạn text chứa thông tin cây
			
			while ( myReader.hasNextLine() ) { 
				String data = myReader.nextLine();
				if( check && data.length() == 0) {
					break;
				}
				
				if(check) {
					int idx = findFirstLetterIndex(data);
					if( idx < 5) {
						System.out.println();
						System.out.println("---file tree có đặc biệt---");
						System.out.println();
						continue; // có trường hợp đặc biệt
					}
					deep = idx/5;
					
					String packageName = data.substring(idx).split(" ")[0];
					String[] info = packageName.split(":");
					String name = info[0] + ":" + info[1];
					String version = getVersionGradle(data);
					
					Library lib = new Library(
							"maven", 
							name, 
							version,
							data.substring(idx),
							"no"
					);
//					System.out.println(lib.toString());
					tree.put(lib, pa.get(deep -1) );
					
					if( pa.size() <= deep )
						pa.add(lib);
					else
						pa.set(deep, lib);
				}
				
				if(data.equals("compileClasspath - Compile classpath for source set 'main'."))
					check = true;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tree;
    }
	
	private String getVersionGradle(String data) {
		String[] info = data.split(":");
		boolean check = false;
		for( String str: info[info.length - 1].split(" ") ) {
			if( check ) return str;
			if( str.equals("->")) check = true;
		}
		return info[info.length - 1].split(" ")[0];
	}

	private int findFirstLetterIndex(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.isLetter(text.charAt(i))) {
                return i;
            }
        }
        return -1; // Trả về -1 nếu không tìm thấy chữ cái trong chuỗi.
    }

	
	
	@Override
	public void downloadConfigFile(Library library) {
		
	}
}
