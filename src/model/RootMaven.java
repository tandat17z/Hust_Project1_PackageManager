package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;




public class RootMaven extends Root{
	final String projectName = "MavenProjectRoot";

	public RootMaven() {
		fileType = "pom.xml";
	}
	
	@Override
	public void create() { 
    	// Tạo thu muc chua maven project
    	File thumuc = new File(rootDir);
    	thumuc.mkdirs();
    	// Tạo maven project
    	String[] command = {
    			"mvn.cmd", "archetype:generate", 
    			"-DgroupId=com.packagemanager", 
    			"-DartifactId="  + projectName, 
    			"-DarchetypeArtifactId=maven-archetype-quickstart", 
    			"-DinteractiveMode=false"
    	};
    	
    	ProcessBuilder processBuilder = new ProcessBuilder(command)
    			.directory(new File(rootDir));
    	try {
			Process process = processBuilder.start();
			int exitCode = process.waitFor();
			System.out.println("Tạo maven Project thành công. exitCode: " + exitCode);
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
//	public static void main(String[] args) {
//		String path = "D:\\Hust_project1_PackageManager\\Project\\maven\\Test\\1.1.1\\";
////		Root.getInstance("maven").saveDependencyTreeToTxt(path);
//		Map<Library, Library> x = Root.getInstance("maven").getDependency(path);
////		getInfo("D:\\Hust_project1_PackageManager\\Library\\maven\\org\\openjfx\\javafx-graphics\\21.0.1\\");
//	}
	
	// Lưu DependencyTree.txt ----------------------------------
	@Override
	public void saveDependencyTreeToTxt(String projectPath) {
		// Thay thế dependency của project vào project Root
//		replaceDependencyInPom(projectPath);
		
		String[] command = {"mvn.cmd", "dependency:tree"};
		
		ProcessBuilder processBuilder = new ProcessBuilder(command)
				.directory(new File(projectPath));
		
		try {
			Process process = processBuilder.start();
			try (InputStream inputStream = process.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(inputStreamReader);
                BufferedWriter writer = new BufferedWriter(new FileWriter(projectPath + "DependencyTree.txt"))) {

               String line;
               while ((line = reader.readLine()) != null) {
                   // Ghi vào tệp
                   writer.write(line);
                   writer.newLine();
               }
            }
			
			
			// Chờ lệnh kết thúc
			int exitCode = process.waitFor();
			
			System.out.println("ProjectRoot: Lưu DependencyText thành công, exitCode: " + exitCode);
//        saveDependency(projectPath); 
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getInfo(String path) {
		try {
            String pomXmlFilePath = path + "pom.xml";
            Model model = readPomXml(pomXmlFilePath);

            String groupId = model.getGroupId();
            String artifactId = model.getArtifactId();
            
            return groupId + ":" + artifactId + ":";
        } catch (IOException | XmlPullParserException e) {
        	System.out.println("ERROR: Lỗi file pom");
//            e.printStackTrace();
        }
		return "------";
	}
	
	 private Model readPomXml(String pomXmlFilePath) throws IOException, XmlPullParserException {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try (FileReader fileReader = new FileReader(pomXmlFilePath)) {
            return reader.read(fileReader);
        }
    }

    // Lấy dependency  ---------------------------------
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
				if(check) {
					while( deep != 0) {
						String str = "\\[INFO\\] .{" + (deep*3 - 1) + "}\\s(.+)";
						Pattern pattern = Pattern.compile(str);
						Matcher matcher = pattern.matcher(data);
						
						if( matcher.find()) {
							String packageName = matcher.group(1);

							String[] info = packageName.split(":");
							String name = info[0] + ":" + info[1];
							String version = info[info.length - 2];
							Library lib = new Library(
									"maven", 
									name, 
									version,
									packageName,
									"no"
							);
							
							tree.put(lib, pa.get(deep -1) );
							
							if( pa.size() <= deep )
								pa.add(lib);
							else
								pa.set(deep, lib);
							
							deep += 1;
							break;
						}
						deep -= 1;
						
					}
				}
				if (deep == 0) break;
				try {
					if( check == false && data.contains("[INFO] " + getInfo(path))) {
//						System.out.println("Bắt đầu tìm cây");
						check = true;
					}
				}
				catch(Exception e) {
//					e.getStackTrace();
				}
				
				if( check == false && data.contains("[INFO] --- dependency:")) {
//					System.out.println("Bắt đầu tìm cây");
					check = true;
					myReader.nextLine();
				}
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return tree;
    }

//    public static void main(String[] args) {
//		Library library = new Library("maven", "com.android.support:appcompat-v7", "28.0.0", "mo ta", "no");
//		(new RootMaven()).downloadConfigFile(library);
//	}
//    
    // Download Pom của library
    @Override
    public void downloadConfigFile(Library library) {
    	String[] info = library.name.split(":"); 
    	String repoMaven = "https://repo1.maven.org/maven2/" 
    			+ library.toString().replace("\\", "/") 
    			+ info[1] + "-" + library.version + ".pom";
    	System.out.println("\nRepoUrl: " + repoMaven);
		HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(repoMaven);

		try {
			HttpResponse response;
			response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String pomContent =  EntityUtils.toString(entity);
				
				if( !pomContent.contains("404 Not Found") ) {
					File file = new File(library.libraryPath + "pom.xml");
					FileWriter writer = new FileWriter(file);
					writer.write(pomContent);
					writer.close();
				}
				else {
					System.out.println("Không truy cập được url or không lấy được file pom");
				}
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    }
}
