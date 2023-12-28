package model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import database.Database;
import database.ProjectRoot;
import javafx.scene.control.TreeItem;

public class Version {
	String versionDir;
	
	int versionId;
	
	Project project;
	String version;
	String description;
	String time;
	
	File configFile;
	
//	public static void main(String[] args) {
////		Project project = new Project(
////				"FirstProject",
////				"maven",
////				"first project",
////				"2023:12:24 12:12:12"
////			);
//		Version version = new Version(
//				"FirstProject",
//				"1.0.0"
//			);
//		
//		version.update("hello hello");
//	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return project.toString() + ":" + version;
	}
	
	public Version(Project project, String version, String description, String time) {
		this.project = project;
		this.version = version;
		this.description = description;
		this.time = time;
		
		versionDir = project.projectDir + version + "\\";
		this.configFile = new File(versionDir + "pom.xml");
	}
	
	public Version(String projectName, String version) {
		String sql = "select * from VERSION "
				+ "where project = ? and version = ?";
		String [] arg = {projectName, version};
		
		Connection connection = Database.getInstance().getConnection();
		PreparedStatement statement;
		try {
			statement = connection.prepareStatement(sql);
			int i = 1;
			for( String a: arg) {
				statement.setString(i, a);
				i++;
			}
			ResultSet resultSet = statement.executeQuery();
			if( resultSet.next()) {
				this.versionId = resultSet.getInt("version_id");
				this.project = new Project(projectName);
				this.version = version;
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
			}
			else {
				System.out.println("Không tìm thầy version model trong db");
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		versionDir = project.projectDir + version + "\\";
		this.configFile = new File(versionDir + "pom.xml");
	}
	
	public Version(int version_id) {
		String sql = "select * from VERSION "
				+ "where version_id = ?";
		
		try {
			Connection connection = Database.getInstance().getConnection();
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setInt(1,  version_id);
			ResultSet resultSet = statement.executeQuery();
			
			if( resultSet.next()) {
				this.versionId = resultSet.getInt("version_id");
				this.project = new Project(resultSet.getString("project"));
				this.version = resultSet.getString("version");
				this.description = resultSet.getString("description");
				this.time = resultSet.getString("time");
			}
			else {
				System.out.println("Không tìm thầy version model trong db");
			}
			resultSet.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		versionDir = project.projectDir + version + "\\";
		this.configFile = new File(versionDir + "pom.xml");
	}
	public void save(File configFile) {
		this.configFile = configFile;
		saveDb();
		saveLocal();
		versionId = (new Version(project.getName(), version)).getVersionId();
		System.out.println("Successful - Version.save: " + toString());
	}

	private void saveDb() {
		String sqlChk = "SELECT * FROM VERSION "
				+ "WHERE project = ? and version = ?";
		String[] argChk = {project.name, version};
		try {
			Boolean check = Database.getInstance().sqlQuery(sqlChk, argChk);
			if( check ) {
				System.out.println("--" + toString() + "existed in Db");
				return;
			}
			
			String sql = "INSERT INTO VERSION(project, version, description, time) "
					+ "VALUES (?, ?, ?, ?);";
			String[] arg = {project.name, version, description, time};
			Database.getInstance().sqlModify(sql, arg);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Version.saveDb");
			e.printStackTrace();
		}
	}
	
	private void saveLocal() {
		File dir = new File(versionDir);
		if( dir.exists() ) {
        	System.out.println("--" +  toString() + " existed trong Local");
        	return;
		}
		dir.mkdirs();
		//Sao chép config file vào thư mục lưu trữ
    	try {
			Files.copy(
					configFile.toPath(), 
					new File(versionDir, "pom.xml").toPath(),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	// Tạo file txt lưu dependency tree
    	ProjectRoot.getInstance(project.type).saveDependencyTree(versionDir);
    	try {
    		System.out.println("---- SAVE DEPENDENCY ----");
			saveDependency();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("Error: Version.saveDependency");
			e.printStackTrace();
		}
	}
	
	private void saveDependency() throws FileNotFoundException {
		File treeFile = new File(versionDir + "DependencyTree.txt");
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
							String[] info = matcher.group(1).split(":");
							
							String name = info[0] + ":" + info[1];
							String version = info[info.length - 2];
							
			    			Library library = new Library(project.type, name, version, "Maven Repository", "no");
			    			library.save();
							
							deep += 1;
							break;
						}
						deep -= 1;
					}
				}
				if (deep == 0) break;
				
				if(data.equals("[INFO] com.packagemanager:MavenProjectRoot:jar:1.0-SNAPSHOT"))
					check = true;
				
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<Library> getDependency(){
		List<Library> dependencyList = new ArrayList<Library>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(configFile);
			
			NodeList dependencies = document.getElementsByTagName("dependency");
			
			for (int i = 0; i < dependencies.getLength(); i++) {
                if (dependencies.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element dependencyElement = (Element) dependencies.item(i);

                    String groupId = dependencyElement.getElementsByTagName("groupId").item(0).getTextContent();
                    String artifactId = dependencyElement.getElementsByTagName("artifactId").item(0).getTextContent();
                    String version = dependencyElement.getElementsByTagName("version").item(0).getTextContent();
                    
                    dependencyList.add(new Library("maven", groupId + ":" + artifactId, version, "", "no"));
//                    System.out.println("GroupID: " + groupId);
//                    System.out.println("ArtifactID: " + artifactId);
//                    System.out.println("Version: " + version);
////                    System.out.println("Scope: " + scope);
//                    System.out.println("------");
                }
            }
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dependencyList;
	}
	
	public void update(String newDescription) {
		String sql = "UPDATE VERSION "
				+ "SET description = ? "
				+ "WHERE version_id = ?";
		Connection connection = Database.getInstance().getConnection();
		try {
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, newDescription);
			statement.setInt(2,  getVersionId());
			statement.executeUpdate();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public Project getProject() {
		return project;
	}
	
	public String getVersionDir() {
		return versionDir;
	}
	
	public String getVersion() {
		return version;
	}
	
	public String getTime() {
		return time;
	}
	
	public String  getDescription() {
		return description;
	}
	public int getVersionId() {
		// TODO Auto-generated method stub
		return versionId;
	}
}
