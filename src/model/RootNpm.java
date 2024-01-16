package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class RootNpm extends Root{
	final String projectName = "GradleProjectRoot";
	
    String[] dpdcType = {
    		"dependencies",
    		"devDependencies",
    		
    };
    
    public RootNpm() {
    	fileType = "package.json";
    }
	@Override
	public void create() { 
    	
    }
	
	// Lưu DependencyTree.txt
	@Override
	public void saveDependencyTreeToTxt(String projectPath) {
		try {
            // Đọc nội dung của package.json
            FileReader reader = new FileReader(projectPath + "package.json");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(reader);

            // Tạo đối tượng FileWriter với đường dẫn file
            FileWriter fileWriter = new FileWriter(projectPath + "DependencyTree.txt");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            
            // Lấy dependencies
            for( String type: this.dpdcType) {
            	System.out.println(type);
            	bufferedWriter.write("-" + type + "-");
            	bufferedWriter.newLine();
            	JSONObject dependencies = (JSONObject) jsonObject.get(type);
            	try {
            		Iterator<Map.Entry> iterator = dependencies.entrySet().iterator();
            		while (iterator.hasNext()) {
            			Map.Entry entry = iterator.next();
            			bufferedWriter.write(entry.getKey() + ":" + entry.getValue());
            			bufferedWriter.newLine();
            		}
            	}
            	catch(Exception e){
            		System.out.println("--ERROR-- Không có phần " + type);
            	}
            }
            bufferedWriter.close();
            System.out.println("Dữ liệu đã được ghi vào file thành công.");


        } catch (Exception e) {
            e.printStackTrace();
        }
	}

	
	// download configfile npm --------------------------------
	@Override
	public void downloadConfigFile(Library library) {
		String githubUrl;
		try {
			githubUrl = getUrlPackage(library.name);
			String filePath = library.libraryPath + "package.json";
			downloadPackageJson(githubUrl, filePath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	private String getUrlPackage(String name){
		String urlNpm = "https://www.npmjs.com/package/" + name;
		String githubUrl = null;
		HttpURLConnection connection;
		StringBuilder NpmPageContent = new StringBuilder();
		try {
			connection = (HttpURLConnection) new URL(urlNpm).openConnection();
			connection.setRequestMethod("GET");

	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
	            String line;
	
	            while ((line = reader.readLine()) != null) {
	            	NpmPageContent.append(line);
	            }
	        }finally {
	            connection.disconnect();
	        }
        
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        Pattern pattern = Pattern.compile("\"repository repository-link\".+?href=\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(NpmPageContent.toString());
        if (matcher.find()) {
        	System.out.println(matcher.group(1));
        	githubUrl =  matcher.group(1);
        }
        else {
        	System.out.println("ERROR: NPM -> lấy githubURL thất bại");
        }
		return githubUrl;
    }
	
	private void downloadPackageJson(String githubUrl, String filePath) throws IOException{
    	Pattern pattern = Pattern.compile("https://github.com/(.+)");
        Matcher matcher = pattern.matcher(githubUrl);
        String githubRepoName = null;
        if (matcher.find()) {
        	githubRepoName =  matcher.group(1);
        }
        
        String downloadUrl = "https://raw.githubusercontent.com/" + githubRepoName + "/master/package.json";
        HttpURLConnection connection = (HttpURLConnection) new URL(downloadUrl).openConnection();
        connection.setRequestMethod("GET");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath)) ) {
            String line;
            while ((line = reader.readLine()) != null) {
            	writer.write(line);
            	writer.newLine();
            }	
            writer.flush(); // Đảm bảo dữ liệu được ghi vào file ngay lập tức
            writer.close();
            System.out.println("NPM -> lưu thành công vào package.json");
        } finally {
            connection.disconnect();
        }
    }

	@Override
	public Map<Library, Library> getDependency(String path) {
		File treeFile = new File(path + "DependencyTree.txt");
		
		Map<Library, Library> tree = new TreeMap<>();
		
		Library pa = null;
		Scanner myReader;
		try {
			myReader = new Scanner(treeFile);
			boolean check = false; // Bắt đầu tìm đến đoạn text chứa thông tin cây
			while ( myReader.hasNextLine() ) { 
				String data = myReader.nextLine();
				if( data.equals("-dependencies-") || data.equals("-devDependencies-")) {
					check = false;
					pa = new Library("npm", data, data, data, "no");
				}
				if(check) {
					String[] info = data.split(":");
					String name = info[0];
					String version = getVersionNpm(info[1]);
					Library lib = new Library("npm", name, version, data, "no");
					tree.put(lib, null);
				}
				check = true;
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tree;
	}
	
	private String getVersionNpm(String ver) {
		for(int i = 0; i<ver.length(); i++) {
			char currentChar = ver.charAt(i);
            if (Character.isDigit(currentChar)) {
                return ver.substring(i); // Trả về vị trí của chữ số đầu tiên
            }
		}
		return ver;
	}
}
