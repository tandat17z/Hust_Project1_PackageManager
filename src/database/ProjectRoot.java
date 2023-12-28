package database;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class ProjectRoot{
	String dir = "D:\\Hust_project1_PackageManager\\";
	String mavenProject = "MavenProjectRoot";
	String type;
	
	public static void main(String[] args) {
		ProjectRoot.getInstance("maven").saveDependencyTree("D:\\Hust_project1_PackageManager\\Project\\FirstProject\\1.0.0\\");
	}
	
	public ProjectRoot(String type) {
		this.type = type;
	}
	
	public static ProjectRoot getInstance(String type) {
		return new ProjectRoot(type);
	}
	
	public void create() {
		if( type == "maven") {
			System.out.println("Tạo MavenRoot");
			createMaven();
		}
	}
	
	// Tạo MavenProjectRoot
	private void createMaven() { 
    	// Tạo thu muc chua maven project
    	File thumuc = new File(dir);
    	thumuc.mkdirs();
    	// Tạo maven project
    	String[] command = {
    			"mvn.cmd", "archetype:generate", 
    			"-DgroupId=com.packagemanager", 
    			"-DartifactId="  + mavenProject, 
    			"-DarchetypeArtifactId=maven-archetype-quickstart", 
    			"-DinteractiveMode=false"
    	};
    	
    	ProcessBuilder processBuilder = new ProcessBuilder(command)
    			.directory(new File(dir));
    	try {
			Process process = processBuilder.start();
			System.out.println("Tạo maven Project thành công");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void saveDependencyTree(String projectPath) {
    	replaceDependencyInPom(projectPath);
    	String[] command = {"mvn.cmd", "dependency:tree"};
    	
    	ProcessBuilder processBuilder = new ProcessBuilder(command)
    			.directory(new File(dir + mavenProject));
    	
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
			
// ---------- reader và writer chưa được đóng, có thể gây ra vấn đề 
			
			// Chờ lệnh kết thúc
            int exitCode = process.waitFor();
            System.out.println("ProjectRoot: Lưu DependencyText thành công");
//            saveDependency(projectPath); 
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private Document readXmlFile(String filePath) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filePath));
    }
    
    private void writeXmlFile(Document document, String filePath) throws Exception {
        // Ghi lại file xml với các thay đổi
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }
    
    // Thế dependencies của project với MavenProjectRoot để đọc lấy dependencyTree.txt
    private void replaceDependencyInPom(String projectPath) {
    	String filePath = projectPath + "pom.xml";
    	try {
    		Document oldDocument = readXmlFile(dir + mavenProject + "\\pom.xml");
    		
			NodeList oldDependencyList = oldDocument.getElementsByTagName("dependencies");
			NodeList newDependencyList = readXmlFile(filePath).getElementsByTagName("dependencies");
			
			// Remove old dependencies list
            for (int i = 0; i < oldDependencyList.getLength(); i++) {
                Node nodeToRemove = oldDependencyList.item(i);
                nodeToRemove.getParentNode().removeChild(nodeToRemove);
            }
			
            //add new dependencies List
            Node projectNode = oldDocument.getElementsByTagName("project").item(0);
            for (int i = 0; i < newDependencyList.getLength(); i++) {
                Node importedNode = oldDocument.importNode(newDependencyList.item(i), true);
                projectNode.appendChild(importedNode);
            }
            
            // Lưu lại file pom.xml mới
            try {
				writeXmlFile(oldDocument, dir + mavenProject + "\\pom.xml");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private void saveLibrary(String projectPath) throws FileNotFoundException {
    	File file = new File(projectPath + "\\dependencyTree.txt");
    	int deep = 1;
		Scanner myReader;
		myReader = new Scanner(file);
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
		    			String[] info = matcher.group(1).split(":");
		    			
		    			String name = info[0] + ":" + info[1];
		    			String version = info[info.length - 2];
//		    			Library library = new Library("Maven", name, version);
//		    			library.save();
		    			
		    			deep += 1;
		    			break;
		    		}
		    		deep -= 1;
		    	}
			}
		    if (deep == 0) break;
		    
			if(data.equals("[INFO] com.packagemanager:mavenProject:jar:1.0-SNAPSHOT"))
		    	check = true;
			
		}
		myReader.close();
    }
    
}
